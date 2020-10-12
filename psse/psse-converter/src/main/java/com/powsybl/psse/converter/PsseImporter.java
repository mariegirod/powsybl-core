/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.psse.model.*;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
@AutoService(Importer.class)
public class PsseImporter implements Importer {

    private static final String FORMAT = "PSS/E";

    private static final String[] EXTS = {"raw", "RAW", "rawx", "RAWX"};

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("psse.import.ignore-base-voltage",
            ParameterType.BOOLEAN,
            "Ignore base voltage specified in the file",
            Boolean.FALSE);

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(IGNORE_BASE_VOLTAGE_PARAMETER);
    }

    @Override
    public String getComment() {
        return "PSS/E Format to IIDM converter";
    }

    private String findExtension(ReadOnlyDataSource dataSource, boolean throwException) throws IOException {
        for (String ext : EXTS) {
            if (dataSource.exists(null, ext)) {
                return ext;
            }
        }
        if (throwException) {
            throw new PsseException("File " + dataSource.getBaseName()
                    + "." + String.join("|", EXTS) + " not found");
        }
        return null;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return checkCaseIdentificationModel(dataSource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean checkCaseIdentificationModel(ReadOnlyDataSource dataSource) throws IOException {
        String ext = findExtension(dataSource, false);
        if (ext == null) {
            return false;
        }
        if (psseFileFormatFromExtension(ext) == PsseFileFormat.FORMAT_RAW) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
                return new PsseRawReader().checkCaseIdentification(reader);
            }
        } else {
            String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
            return new PsseRawReader().checkCaseIdentificationx(jsonFile);
        }
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            String ext = findExtension(fromDataSource, false);
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null, ext, false)) {
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);

        try {

            PsseRawModel psseModel = importPsseModel(dataSource);

            // set date and time
            // TODO

            // build container to fit IIDM requirements
            ContainersMapping containersMapping = defineContainersMappging(psseModel);

            boolean ignoreBaseVoltage = ConversionParameters.readBooleanParameter(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);
            PerUnitContext perUnitContext = new PerUnitContext(psseModel.getCaseIdentification().getSbase(),
                ignoreBaseVoltage);

            // The map gives access to PsseBus object with the int bus Number
            Map<Integer, PsseBus> busNumToPsseBus = new HashMap<>();

            // create buses
            createBuses(psseModel, containersMapping, perUnitContext, network, busNumToPsseBus);

            // Create loads
            for (PsseLoad psseLoad : psseModel.getLoads()) {
                new LoadConverter(psseLoad, containersMapping, network).create();
            }

            // Create fixed shunts
            for (PsseFixedShunt psseShunt : psseModel.getFixedShunts()) {
                new FixedShuntCompensatorConverter(psseShunt, containersMapping, network).create();
            }

            // Create switched shunts
            Map<PsseSwitchedShunt, ShuntBlockTab> stoBlockiTab = createSwitchedShuntBlockMap(psseModel);
            for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
                new SwitchedShuntCompensatorConverter(psseSwShunt, containersMapping, network).create(stoBlockiTab);
            }

            for (PsseGenerator psseGen : psseModel.getGenerators()) {
                new GeneratorConverter(psseGen, containersMapping, network).create(busNumToPsseBus.get(psseGen.getI()));
            }

            for (PsseNonTransformerBranch psseLine : psseModel.getNonTransformerBranches()) {
                new LineConverter(psseLine, containersMapping, perUnitContext, network).create();
            }

            for (PsseTransformer psseTfo : psseModel.getTransformers()) {
                new TransformerConverter(psseTfo, containersMapping, perUnitContext, network, busNumToPsseBus, psseModel.getCaseIdentification().getSbase()).create();
            }

            // Attach a slack bus
            new SlackConverter(psseModel.getAreas(), containersMapping, network).create();

            return network;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private PsseRawModel importPsseModel(ReadOnlyDataSource dataSource) throws IOException {
        PsseRawModel psseModel;
        String ext = findExtension(dataSource, true);
        if (psseFileFormatFromExtension(ext) == PsseFileFormat.FORMAT_RAW) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
                psseModel = new PsseRawReader().read(reader); // parse file
            }
        } else {
            String jsonFile = new String(ByteStreams.toByteArray(dataSource.newInputStream(null, ext)), StandardCharsets.UTF_8);
            psseModel = new PsseRawReader().readx(jsonFile);
        }

        // check version
        if (!ArrayUtils.contains(PsseConstants.SUPPORTED_VERSIONS, psseModel.getCaseIdentification().getRev())) {
            throw new PsseException("PSS/E version " + psseModel.getCaseIdentification().getRev()
                + " not supported. Supported Versions " + ArrayUtils.toString(PsseConstants.SUPPORTED_VERSIONS));
        }
        if (psseModel.getCaseIdentification().getIc() == 1) {
            throw new PsseException("Incremental load of PSS/E data option (IC = 1) not supported");
        }

        return psseModel;
    }

    private PsseFileFormat psseFileFormatFromExtension(String extension) {
        if (extension.contains("x") || extension.contains("X")) {
            return PsseFileFormat.FORMAT_RAWX;
        }
        return PsseFileFormat.FORMAT_RAW;
    }

    private ContainersMapping defineContainersMappging(PsseRawModel psseModel) {
        List<Object> branches = ImmutableList.builder()
            .addAll(psseModel.getNonTransformerBranches())
            .addAll(psseModel.getTransformers())
            .build();

        ToIntFunction<Object> branchToNum1 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getI() : ((PsseTransformer) branch).getI();
        ToIntFunction<Object> branchToNum2 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getJ() : ((PsseTransformer) branch).getJ();
        ToIntFunction<Object> branchToNum3 = branch -> branch instanceof PsseNonTransformerBranch ? 0 : ((PsseTransformer) branch).getK();
        ToDoubleFunction<Object> branchToResistance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getR() : ((PsseTransformer) branch).getR12();
        ToDoubleFunction<Object> branchToReactance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getX() : ((PsseTransformer) branch).getX12();
        Predicate<Object> branchToIsTransformer = branch -> branch instanceof PsseTransformer;

        ContainersMapping containersMapping = ContainersMapping.create(psseModel.getBuses(), branches, PsseBus::getI, branchToNum1,
            branchToNum2, branchToNum3, branchToResistance, branchToReactance, branchToIsTransformer,
            busNums -> "VL" + busNums.iterator().next(), substationNum -> "S" + substationNum++);

        return containersMapping;
    }

    private static void createBuses(PsseRawModel psseModel, ContainersMapping containerMapping,
        PerUnitContext perUnitContext, Network network, Map<Integer, PsseBus> busNumToPsseBus) {
        for (PsseBus psseBus : psseModel.getBuses()) {

            Substation substation = new SubstationConverter(psseBus, containerMapping, network).create();
            VoltageLevel voltageLevel = new VoltageLevelConverter(psseBus, containerMapping, perUnitContext, network).create(substation);
            new BusConverter(psseBus, containerMapping, network).create(voltageLevel);

            busNumToPsseBus.put(psseBus.getI(), psseBus);
        }
    }

    private Map<PsseSwitchedShunt, ShuntBlockTab> createSwitchedShuntBlockMap(PsseRawModel psseModel) {
        Map<PsseSwitchedShunt, ShuntBlockTab> stoBlockiTab = new HashMap<>();

        /* Creates a map between the PSSE switched shunt and the blocks info of this shunt
        A switched shunt may contain up to 8 blocks and each block may contain up to 9 steps of the same value (in MVAR)
        A block may be capacitive or inductive */
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            ShuntBlockTab sbt = new ShuntBlockTab();

            int[] ni = {
                    psseSwShunt.getN1(), psseSwShunt.getN2(), psseSwShunt.getN3(), psseSwShunt.getN4(),
                    psseSwShunt.getN5(), psseSwShunt.getN6(), psseSwShunt.getN7(), psseSwShunt.getN8()
            };

            double[] bi = {
                    psseSwShunt.getB1(), psseSwShunt.getB2(), psseSwShunt.getB3(), psseSwShunt.getB4(),
                    psseSwShunt.getB5(), psseSwShunt.getB6(), psseSwShunt.getB7(), psseSwShunt.getB8()
            };

            int i = 0;
            while (i <= 7 && ni[i] > 0) {
                sbt.add(i + 1, ni[i], bi[i]);
                i++;
            }

            stoBlockiTab.put(psseSwShunt, sbt);
        }

        return stoBlockiTab;
    }

    public static final class ShuntBlockTab {

        private final Map<Integer, Integer> ni = new HashMap<>();
        private final Map<Integer, Double> bi = new HashMap<>();

        private void add(int i, int nni, double bni) {
            ni.put(i, nni);
            bi.put(i, bni);
        }

        private int getNi(int i) {
            return ni.get(i);
        }

        private double getBi(int i) {
            return bi.get(i);
        }

        public int getSize() {
            return ni.size();
        }
    }

    public static class PerUnitContext {
        private final double sb;
        private final boolean ignoreBaseVoltage;

        PerUnitContext(double sb, boolean ignoreBaseVoltage) {
            this.sb = sb;
            this.ignoreBaseVoltage = ignoreBaseVoltage;
        }

        public double getSb() {
            return sb;
        }

        public boolean isIgnoreBaseVoltage() {
            return ignoreBaseVoltage;
        }
    }
}
