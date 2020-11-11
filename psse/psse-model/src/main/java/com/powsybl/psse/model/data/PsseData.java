/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseConstants;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseRawModel35;
import com.powsybl.psse.model.data.JsonModel.ArrayData;
import com.powsybl.psse.model.data.JsonModel.JsonNetwork;
import com.powsybl.psse.model.data.JsonModel.TableData;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseData {

    // Check

    public boolean checkCase(BufferedReader reader) throws IOException {

        // CaseIdentification does not change, so it is read using version 33
        PsseVersion version = PsseVersion.VERSION_33;

        // just check the first record if this file is in PSS/E format
        PsseCaseIdentification caseIdentification;
        try {
            caseIdentification = new CaseIdentificationData(version).read(reader);
        } catch (PsseException e) {
            return false; // invalid PSS/E content
        }
        return checkCaseIdentification(caseIdentification);
    }

    public boolean checkCasex(String jsonFile) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode networkNode = rootNode.get("network");

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version, format).readx(networkNode);
        return checkCaseIdentification(caseIdentification);
    }

    private boolean checkCaseIdentification(PsseCaseIdentification caseIdentification) {
        int ic = caseIdentification.getIc();
        double sbase = caseIdentification.getSbase();
        int rev = caseIdentification.getRev();
        double basfrq = caseIdentification.getBasfrq();

        return ic == 0 && sbase > 0. && ArrayUtils.contains(PsseConstants.SUPPORTED_VERSIONS, rev) && basfrq > 0.0;
    }

    // Read

    public PsseRawModel read(BufferedReader reader, PsseContext context) throws IOException {

        // CaseIdentification does not change, so it is read using version 33
        PsseVersion version = PsseVersion.VERSION_33;
        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version).read(reader, context);

        if (caseIdentification.getRev() == 33) {
            return read33(reader, caseIdentification, context);
        } else if (caseIdentification.getRev() == 35) {
            return read35(reader, caseIdentification, context);
        } else {
            throw new PsseException("Psse: unexpected version: " + caseIdentification.getRev());
        }
    }

    public PsseRawModel read33(BufferedReader reader,  PsseCaseIdentification caseIdentification, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_33;
        PsseRawModel model = new PsseRawModel(caseIdentification);

        readBlocksA(reader, model, version, context);
        readBlocksB(reader, model, version, context);

        // q record (nothing to do)
        BlockData.readDiscardedRecordBlock(reader);

        return model;
    }

    public PsseRawModel read35(BufferedReader reader, PsseCaseIdentification caseIdentification, PsseContext context) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseRawModel model = new PsseRawModel(caseIdentification);

        // System-Wide data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        readBlocksA(reader, model, version, context);

        // System Switching device data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        readBlocksB(reader, model, version, context);

        // Substation data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        // q record (nothing to do)
        BlockData.readDiscardedRecordBlock(reader);

        return model;
    }

    private void readBlocksA(BufferedReader reader, PsseRawModel model, PsseVersion version, PsseContext context)
        throws IOException {

        model.addBuses(new BusData(version).read(reader, context));
        model.addLoads(new LoadData(version).read(reader, context));
        model.addFixedShunts(new FixedBusShuntData(version).read(reader, context));
        model.addGenerators(new GeneratorData(version).read(reader, context));
        model.addNonTransformerBranches(new NonTransformerBranchData(version).read(reader, context));
    }

    private void readBlocksB(BufferedReader reader, PsseRawModel model, PsseVersion version, PsseContext context)
        throws IOException {

        model.addTransformers(new TransformerData(version).read(reader, context));
        model.addAreas(new AreaInterchangeData(version).read(reader, context));
        model.addTwoTerminalDcTransmissionLines(new TwoTerminalDcTransmissionLineData(version).read(reader, context));
        model.addVoltageSourceConverterDcTransmissionLines(new VoltageSourceConverterDcTransmissionLineData(version).read(reader, context));

        // impedance correction data
        model.addTransformerImpedanceCorrections(new TransformerImpedanceCorrectionTablesData(version).read(reader));

        // multi-terminal DC data
        BlockData.readDiscardedRecordBlock(reader); // TODO

        model.addLineGrouping(new MultiSectionLineGroupingData(version).read(reader));
        model.addZones(new ZoneData(version).read(reader, context));
        model.addInterareaTransfer(new InterareaTransferData(version).read(reader));
        model.addOwners(new OwnerData(version).read(reader, context));
        model.addFacts(new FactsDeviceData(version).read(reader, context));
        model.addSwitchedShunts(new SwitchedShuntData(version).read(reader, context));
        model.addGneDevice(new GneDeviceData(version).read(reader, context));

        // Induction Machine data
        BlockData.readDiscardedRecordBlock(reader); // TODO
    }

    // Readx

    public PsseRawModel readx(String jsonFile, PsseContext context) throws IOException {

        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode networkNode = rootNode.get("network");

        PsseCaseIdentification caseIdentification = new CaseIdentificationData(version, format).readx(networkNode, context);
        PsseRawModel model = new PsseRawModel35(caseIdentification);

        model.addBuses(new BusData(version, format).readx(networkNode, context));
        model.addLoads(new LoadData(version, format).readx(networkNode, context));
        model.addFixedShunts(new FixedBusShuntData(version, format).readx(networkNode, context));
        model.addGenerators(new GeneratorData(version, format).readx(networkNode, context));
        model.addNonTransformerBranches(new NonTransformerBranchData(version, format).readx(networkNode, context));
        model.addTransformers(new TransformerData(version, format).readx(networkNode, context));

        model.addAreas(new AreaInterchangeData(version, format).readx(networkNode, context));
        model.addTwoTerminalDcTransmissionLines(new TwoTerminalDcTransmissionLineData(version, format).readx(networkNode, context));
        model.addVoltageSourceConverterDcTransmissionLines(new VoltageSourceConverterDcTransmissionLineData(version, format).readx(networkNode, context));
        model.addTransformerImpedanceCorrections(new TransformerImpedanceCorrectionTablesData(version, format).readx(networkNode, context));
        model.addLineGrouping(new MultiSectionLineGroupingData(version, format).readx(networkNode, context));
        model.addZones(new ZoneData(version, format).readx(networkNode, context));
        model.addInterareaTransfer(new InterareaTransferData(version, format).readx(networkNode, context));
        model.addOwners(new OwnerData(version, format).readx(networkNode, context));
        model.addFacts(new FactsDeviceData(version, format).readx(networkNode, context));
        model.addSwitchedShunts(new SwitchedShuntData(version, format).readx(networkNode, context));
        model.addGneDevice(new GneDeviceData(version, format).readx(networkNode, context));

        return model;
    }

    // write

    public void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        if (model.getCaseIdentification().getRev() == 33) {
            write33(model, context, outputStream);
        } else if (model.getCaseIdentification().getRev() == 35) {
            write35(model, context, outputStream);
        } else {
            throw new PsseException("Psse: unexpected version: " + model.getCaseIdentification().getRev());
        }
    }

    public void write33(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        PsseVersion version = PsseVersion.VERSION_33;

        new CaseIdentificationData(version).write(model, context, outputStream);
        writeBlocksA(model, version, context, outputStream);
        writeBlocksB(model, version, context, outputStream);

        BlockData.writeEndOfBlockAndComment("END OF INDUCTION MACHINE DATA", outputStream);
        BlockData.writeQrecord(outputStream);
    }

    public void write35(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        PsseVersion version = PsseVersion.VERSION_35;

        new CaseIdentificationData(version).write(model, context, outputStream);

        BlockData.writeEndOfBlockAndComment("END OF SYSTEM-WIDE DATA, BEGIN BUS DATA", outputStream);

        writeBlocksA(model, version, context, outputStream);

        BlockData.writeEndOfBlockAndComment("END OF SYSTEM SWITCHING DEVICE DATA, BEGIN TRANSFORMER DATA", outputStream);

        writeBlocksB(model, version, context, outputStream);

        BlockData.writeEndOfBlockAndComment("END OF INDUCTION MACHINE DATA, BEGIN SUBSTATION DATA", outputStream);
        BlockData.writeEndOfBlockAndComment("END OF SUBSTATION DATA", outputStream);
        BlockData.writeQrecord(outputStream);
    }

    public void writeBlocksA(PsseRawModel model, PsseVersion version, PsseContext context, OutputStream outputStream) {
        new BusData(version).write(model, context, outputStream);
        new LoadData(version).write(model, context, outputStream);
        new FixedBusShuntData(version).write(model, context, outputStream);
        new GeneratorData(version).write(model, context, outputStream);
        new NonTransformerBranchData(version).write(model, context, outputStream);
    }

    public void writeBlocksB(PsseRawModel model, PsseVersion version, PsseContext context, OutputStream outputStream) {
        new TransformerData(version).write(model, context, outputStream);
        new AreaInterchangeData(version).write(model, context, outputStream);
        new TwoTerminalDcTransmissionLineData(version).write(model, context, outputStream);
        new VoltageSourceConverterDcTransmissionLineData(version).write(model, context, outputStream);

        new TransformerImpedanceCorrectionTablesData(version).write(model, context, outputStream);
        BlockData.writeEndOfBlockAndComment("END OF MULTI-TERMINAL DC DATA, BEGIN MULTI-SECTION LINE DATA", outputStream);
        new MultiSectionLineGroupingData(version).write(model, context, outputStream);

        new ZoneData(version).write(model, context, outputStream);
        new InterareaTransferData(version).write(model, context, outputStream);
        new OwnerData(version).write(model, context, outputStream);
        new FactsDeviceData(version).write(model, context, outputStream);
        new SwitchedShuntData(version).write(model, context, outputStream);
        new GneDeviceData(version).write(model, context, outputStream);
    }

    // writex

    public void writex(PsseRawModel model, PsseContext context, OutputStream outputStream) throws IOException {
        PsseVersion version = PsseVersion.VERSION_35;
        PsseFileFormat format = PsseFileFormat.FORMAT_RAWX;

        JsonNetwork network = new JsonNetwork();

        ArrayData arrayData = new CaseIdentificationData(version, format).writex(model, context);
        if (!arrayDataIsEmpty(arrayData)) {
            network.setCaseid(arrayData);
        }
        TableData tableData = new BusData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setBus(tableData);
        }
        tableData = new LoadData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setLoad(tableData);
        }
        tableData = new FixedBusShuntData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setFixshunt(tableData);
        }
        tableData = new GeneratorData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setGenerator(tableData);
        }
        tableData = new NonTransformerBranchData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setAcline(tableData);
        }
        tableData = new TransformerData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setTransformer(tableData);
        }

        tableData = new AreaInterchangeData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setArea(tableData);
        }
        tableData = new TwoTerminalDcTransmissionLineData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setTwotermdc(tableData);
        }
        tableData = new VoltageSourceConverterDcTransmissionLineData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setVscdc(tableData);
        }
        tableData = new TransformerImpedanceCorrectionTablesData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setImpcor(tableData);
        }
        tableData = new MultiSectionLineGroupingData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setMsline(tableData);
        }
        tableData = new ZoneData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setZone(tableData);
        }
        tableData = new InterareaTransferData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setIatransfer(tableData);
        }
        tableData = new OwnerData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setOwner(tableData);
        }
        tableData = new FactsDeviceData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setFacts(tableData);
        }
        tableData = new SwitchedShuntData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setSwshunt(tableData);
        }
        tableData = new GneDeviceData(version, format).writex(model, context);
        if (!tableDataIsEmpty(tableData)) {
            network.setGne(tableData);
        }
        JsonModel jsonModel = new JsonModel(network);
        String json = BlockData.writexJsonModel(jsonModel);
        String adjustedJson = StringUtils.replaceEach(json, new String[] {"\"[", "]\"", "\\\""}, new String[] {"[", "]", "\""});
        outputStream.write(adjustedJson.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    private boolean arrayDataIsEmpty(ArrayData arrayData) {
        return arrayData.getQuotedFields() == null || arrayData.getData() == null
            || arrayData.getQuotedFields().isEmpty() || arrayData.getData().isEmpty();
    }

    private boolean tableDataIsEmpty(TableData tableData) {
        return tableData.getQuotedFields() == null || tableData.getData() == null
            || tableData.getQuotedFields().isEmpty() || tableData.getData().isEmpty();
    }
}
