/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseValidation {

    public PsseValidation(PsseRawModel model) {
        Objects.requireNonNull(model);
        warnings = new ArrayList<>();
        validCase = true;

        validate(model);
        writeWarnings();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isValidCase() {
        return validCase;
    }

    private void writeWarnings() {
        LOGGER.warn("PSS/E Validation ...");
        warnings.forEach(LOGGER::warn);
        LOGGER.warn("PSS/E Validation end. ValidCase {}", validCase);
    }

    private void validate(PsseRawModel model) {

        Map<Integer, List<Integer>> buses = generateBuses(model.getBuses());

        validateCaseIdentification(model.getCaseIdentification());
        validateBuses(model.getBuses(), buses);
        validateLoads(model.getLoads(), buses);
        validateFixedShunts(model.getFixedShunts(), buses);
        validateGenerators(model.getBuses(), model.getGenerators(), buses);
        validateNonTransformerBranches(model.getNonTransformerBranches(), buses);
        validateTransformers(model.getTransformers(), buses);
    }

    private static Map<Integer, List<Integer>> generateBuses(List<PsseBus> psseBuses) {
        Map<Integer, List<Integer>> buses = new HashMap<>();
        for (int i = 0; i < psseBuses.size(); i++) {
            buses.computeIfAbsent(psseBuses.get(i).getI(), k -> new ArrayList<>()).add(i);
        }
        return buses;
    }

    private void validateCaseIdentification(PsseCaseIdentification caseIdentification) {
        if (caseIdentification.getSbase() <= 0.0) {
            warnings.add(String.format(Locale.US, "CaseIdentification: Unexpected Sbase: %.2f", caseIdentification.getSbase()));
            validCase = false;
        }
        if (caseIdentification.getBasfrq() <= 0.0) {
            warnings.add(String.format(Locale.US, "CaseIdentification: Unexpected Basfrq: %.2f", caseIdentification.getBasfrq()));
            validCase = false;
        }
    }

    private void validateBuses(List<PsseBus> psseBuses, Map<Integer, List<Integer>> buses) {
        for (Map.Entry<Integer, List<Integer>> entry : buses.entrySet()) {
            if (entry.getValue().size() != 1) {
                warnings.add(String.format("Bus: %d defined multiple times (%d)", entry.getKey(), entry.getValue().size()));
                validCase = false;
            }
        }

        for (int i = 0; i < psseBuses.size(); i++) {
            PsseBus psseBus = psseBuses.get(i);
            if (psseBus.getI() < 1) {
                warnings.add(String.format("Bus: Unexpected I: %d", psseBus.getI()));
                validCase = false;
            }
            if (psseBus.getBaskv() < 0.0) {
                warnings.add(String.format(Locale.US, "Bus: %d Unexpected Baskv: %.2f", psseBus.getI(), psseBus.getBaskv()));
                validCase = false;
            }
        }
    }

    private void validateLoads(List<PsseLoad> loads, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesLoads = new HashMap<>();

        for (int i = 0; i < loads.size(); i++) {
            PsseLoad load = loads.get(i);
            if (!buses.containsKey(load.getI())) {
                warnings.add(String.format("Load: Unexpected I: %d", load.getI()));
                validCase = false;
            }
            addBusesMap(busesLoads, load.getI(), load.getId());
        }

        Map<String, List<String>> duplicatedBusesLoads = getDuplicates(busesLoads);
        if (!duplicatedBusesLoads.isEmpty()) {
            duplicatedBusesLoads.forEach(
                (key, value) -> warnings.add(String.format("Load: Multiple loads (%d) at bus %d with the same Id %s",
                    value.size(), Integer.valueOf(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateFixedShunts(List<PsseFixedShunt> fixedShunts, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesFixedShunts = new HashMap<>();

        for (int i = 0; i < fixedShunts.size(); i++) {
            PsseFixedShunt fixedShunt = fixedShunts.get(i);
            if (!buses.containsKey(fixedShunt.getI())) {
                warnings.add(String.format("FixedShunt: Unexpected I: %d", fixedShunt.getI()));
                validCase = false;
            }
            addBusesMap(busesFixedShunts, fixedShunt.getI(), fixedShunt.getId());
        }

        Map<String, List<String>> duplicatedBusesFixedShunts = getDuplicates(busesFixedShunts);
        if (!duplicatedBusesFixedShunts.isEmpty()) {
            duplicatedBusesFixedShunts.forEach((key, value) -> warnings
                .add(String.format("FixedShunt: Multiple fixed shunts (%d) at bus %d with the same Id %s", value.size(),
                    Integer.valueOf(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateGenerators(List<PsseBus> psseBuses, List<PsseGenerator> generators, Map<Integer, List<Integer>> buses) {

        Map<String, List<String>> busesGenerators = new HashMap<>();

        for (int i = 0; i < generators.size(); i++) {
            PsseGenerator generator = generators.get(i);
            if (!buses.containsKey(generator.getI())) {
                warnings.add(String.format("Generator: Unexpected I: %d", generator.getI()));
                validCase = false;
            }
            if (generator.getQt() < generator.getQb()) {
                warnings.add(String.format(Locale.US, "Generator: %d %s Unexpected Qmin: %.2f Qmax: %.2f", generator.getI(), generator.getId(), generator.getQb(), generator.getQt()));
                validCase = false;
            }
            if (generator.getIreg() != 0 && !buses.containsKey(generator.getIreg())) {
                warnings.add(String.format("Generator: %d %s Unexpected IReg: %d", generator.getI(), generator.getId(), generator.getIreg()));
                validCase = false;
            }
            if (generator.getPt() < generator.getPb()) {
                warnings.add(String.format(Locale.US, "Generator: %d %s Unexpected Pmin: %.2f Pmax: %.2f", generator.getI(), generator.getId(), generator.getPb(), generator.getPt()));
                validCase = false;
            }
            validateGeneratorRegulatingBus(psseBuses, buses, generator);

            addBusesMap(busesGenerators, generator.getI(), generator.getId());
        }

        Map<String, List<String>> duplicatedBusesGenerators = getDuplicates(busesGenerators);
        if (!duplicatedBusesGenerators.isEmpty()) {
            duplicatedBusesGenerators.forEach((key, value) -> warnings
                .add(String.format("Generator: Multiple generators (%d) at bus %d with the same Id %s", value.size(),
                    Integer.valueOf(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateGeneratorRegulatingBus(List<PsseBus> psseBuses,  Map<Integer, List<Integer>> buses, PsseGenerator generator) {
        PsseBus regulatingBus = getRegulatingBus(psseBuses, buses, generator.getIreg(), generator.getI());
        if (regulatingBus != null
            && (regulatingBus.getIde() == 1 || regulatingBus.getIde() == 2 || regulatingBus.getIde() == 3)
            && generator.getVs() <= 0.0) {
            warnings.add(String.format(Locale.US, "Generator: %d %s Unexpected Voltage setpoint: %.2f", generator.getI(), generator.getId(), generator.getVs()));
            validCase = false;
        }
    }

    private void validateNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesNonTransformerBranches = new HashMap<>();

        for (int i = 0; i < nonTransformerBranches.size(); i++) {
            PsseNonTransformerBranch nonTransformerBranch = nonTransformerBranches.get(i);
            if (!buses.containsKey(nonTransformerBranch.getI())) {
                warnings.add(String.format("NonTransformerBranch: Unexpected I: %d", nonTransformerBranch.getI()));
                validCase = false;
            }
            if (!buses.containsKey(nonTransformerBranch.getJ())) {
                warnings.add(String.format("NonTransformerBranch: Unexpected J: %d", nonTransformerBranch.getJ()));
                validCase = false;
            }
            if (nonTransformerBranch.getX() == 0.0) {
                warnings.add(String.format(Locale.US, "NonTransformerBranch: %d %d %s Unexpected X: %.5f", nonTransformerBranch.getI(), nonTransformerBranch.getJ(), nonTransformerBranch.getCkt(), nonTransformerBranch.getX()));
                validCase = false;
            }
            addBusesMap(busesNonTransformerBranches, nonTransformerBranch.getI(), nonTransformerBranch.getJ(), nonTransformerBranch.getCkt());
        }

        Map<String, List<String>> duplicatedBusesNonTransformerBranches = getDuplicates(busesNonTransformerBranches);
        if (!duplicatedBusesNonTransformerBranches.isEmpty()) {
            duplicatedBusesNonTransformerBranches.forEach((key,
                value) -> warnings.add(String.format(
                    "NonTransformerBranch: Multiple branches (%d) between buses %d and %d with the same Id %s",
                    value.size(), firstBus(key), secondBus(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateTransformers(List<PsseTransformer> transformers, Map<Integer, List<Integer>> buses) {
        List<PsseTransformer> twoWinddingsTransformers = transformers.parallelStream()
            .filter(transformer -> transformer.getK() == 0).collect(Collectors.toList());
        validateTwoWindingsTransformers(twoWinddingsTransformers, buses);

        List<PsseTransformer> threeWinddingsTransformers = transformers.parallelStream()
            .filter(transformer -> transformer.getK() != 0).collect(Collectors.toList());
        validateThreeWindingsTransformers(threeWinddingsTransformers, buses);
    }

    private void validateTwoWindingsTransformers(List<PsseTransformer> transformers, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesTransformers = new HashMap<>();

        for (int i = 0; i < transformers.size(); i++) {
            PsseTransformer transformer = transformers.get(i);
            validateTransformerBus(buses, transformer.getI(), "I");
            validateTransformerBus(buses, transformer.getJ(), "J");

            String id = String.format("%d %d %s", transformer.getI(), transformer.getJ(), transformer.getCkt());
            validateTransformerX(id, transformer.getX12(), "X12");
            validateTransformerRatio(id, transformer.getWindingRecord1().getWindv(), "ratio");
            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase12(), "sbase12");
            validateTransformerWindingNomV(id, transformer.getCw(), transformer.getCm(), transformer.getWindingRecord1().getNomv(), "winding1 nomV");
            validateTransformerWindingVmiVma(id, transformer.getWindingRecord1().getCod(), transformer.getWindingRecord1().getVmi(), transformer.getWindingRecord1().getVma(), "winding1 Vmi Vma");
            validateTransformerWindingRmiRma(id, transformer.getWindingRecord1().getCod(), transformer.getWindingRecord1().getRmi(), transformer.getWindingRecord1().getRma(), "winding1 Rmi Rma");
            validateTransformerWindingCont(buses, id, transformer.getWindingRecord1().getCod(), transformer.getWindingRecord1().getCont(), "winding1 Cont");

            addBusesMap(busesTransformers, transformer.getI(), transformer.getJ(), transformer.getCkt());
        }

        Map<String, List<String>> duplicatedBusesTransformers = getDuplicates(busesTransformers);
        if (!duplicatedBusesTransformers.isEmpty()) {
            duplicatedBusesTransformers.forEach((key,
                value) -> warnings.add(String.format(
                    "Transformer: Multiple branches (%d) between buses %d and %d with the same Id %s",
                    value.size(), firstBus(key), secondBus(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateThreeWindingsTransformers(List<PsseTransformer> transformers, Map<Integer, List<Integer>> buses) {
        Map<String, List<String>> busesTransformers = new HashMap<>();

        for (int i = 0; i < transformers.size(); i++) {
            PsseTransformer transformer = transformers.get(i);
            validateTransformerBus(buses, transformer.getI(), "I");
            validateTransformerBus(buses, transformer.getJ(), "J");
            validateTransformerBus(buses, transformer.getK(), "K");

            String id = String.format("%d %d %d %s", transformer.getI(), transformer.getJ(), transformer.getK(), transformer.getCkt());
            validateTransformerX(id, transformer.getX12(), "X12");
            validateTransformerX(id, transformer.getX31(), "X31");
            validateTransformerX(id, transformer.getX23(), "X23");

            validateTransformerRatio(id, transformer.getWindingRecord1().getWindv(), "winding1 ratio");
            validateTransformerRatio(id, transformer.getWindingRecord2().getWindv(), "winding2 ratio");
            validateTransformerRatio(id, transformer.getWindingRecord3().getWindv(), "winding3 ratio");

            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase12(), "sbase12");
            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase23(), "sbase23");
            validateTransformerSbase(id, transformer.getCz(), transformer.getCm(), transformer.getSbase31(), "sbase31");

            validateTransformerWindingNomV(id, transformer.getCz(), transformer.getCm(), transformer.getWindingRecord1().getNomv(), "winding1 nomV");
            validateTransformerWindingNomV(id, transformer.getCz(), transformer.getCm(), transformer.getWindingRecord2().getNomv(), "winding2 nomV");
            validateTransformerWindingNomV(id, transformer.getCz(), transformer.getCm(), transformer.getWindingRecord3().getNomv(), "winding3 nomV");

            validateTransformerWindingVmiVma(id, transformer.getWindingRecord1().getCod(), transformer.getWindingRecord1().getVmi(), transformer.getWindingRecord1().getVma(), "winding1 Vmi Vma");
            validateTransformerWindingVmiVma(id, transformer.getWindingRecord2().getCod(), transformer.getWindingRecord2().getVmi(), transformer.getWindingRecord2().getVma(), "winding2 Vmi Vma");
            validateTransformerWindingVmiVma(id, transformer.getWindingRecord3().getCod(), transformer.getWindingRecord3().getVmi(), transformer.getWindingRecord3().getVma(), "winding3 Vmi Vma");

            validateTransformerWindingRmiRma(id, transformer.getWindingRecord1().getCod(), transformer.getWindingRecord1().getRmi(), transformer.getWindingRecord1().getRma(), "winding1 Rmi Rma");
            validateTransformerWindingRmiRma(id, transformer.getWindingRecord2().getCod(), transformer.getWindingRecord2().getRmi(), transformer.getWindingRecord2().getRma(), "winding2 Rmi Rma");
            validateTransformerWindingRmiRma(id, transformer.getWindingRecord3().getCod(), transformer.getWindingRecord3().getRmi(), transformer.getWindingRecord3().getRma(), "winding3 Rmi Rma");

            validateTransformerWindingCont(buses, id, transformer.getWindingRecord1().getCod(), transformer.getWindingRecord1().getCont(), "winding1 Cont");
            validateTransformerWindingCont(buses, id, transformer.getWindingRecord2().getCod(), transformer.getWindingRecord2().getCont(), "winding2 Cont");
            validateTransformerWindingCont(buses, id, transformer.getWindingRecord3().getCod(), transformer.getWindingRecord3().getCont(), "winding3 Cont");

            addBusesMap(busesTransformers, transformer.getI(), transformer.getJ(), transformer.getK(), transformer.getCkt());
        }

        Map<String, List<String>> duplicatedBusesTransformers = getDuplicates(busesTransformers);
        if (!duplicatedBusesTransformers.isEmpty()) {
            duplicatedBusesTransformers.forEach((key,
                value) -> warnings.add(String.format(
                    "Transformer: Multiple branches (%d) between buses %d, %d and %d with the same Id %s",
                    value.size(), firstBus(key), secondBus(key), thirdBus(key), value.get(0))));
            validCase = false;
        }
    }

    private void validateTransformerBus(Map<Integer, List<Integer>> buses, int bus, String busTag) {
        if (!buses.containsKey(bus)) {
            warnings.add(String.format("Transformer: Unexpected %s: %d", busTag, bus));
            validCase = false;
        }
    }

    private void validateTransformerX(String id, double x, String xTag) {
        if (x == 0.0) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f", id, xTag, x));
            validCase = false;
        }
    }

    private void validateTransformerRatio(String id, double ratio, String ratioTag) {
        if (ratio <= 0.0) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f", id, ratioTag, ratio));
            validCase = false;
        }
    }

    private void validateTransformerSbase(String id, int cz, int cm, double sbase, String sbaseTag) {
        if ((cz == 2 || cz == 3 || cm == 2) && sbase <= 0.0) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f", id, sbaseTag, sbase));
            validCase = false;
        }
    }

    private void validateTransformerWindingNomV(String id, int cw, int cm, double windingNomV, String windingNomVTag) {
        if ((cw == 3 || cm == 2) && windingNomV <= 0.0) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f", id, windingNomVTag, windingNomV));
            validCase = false;
        }
    }

    private void validateTransformerWindingVmiVma(String id, int cod, double windingVmi, double windingVma, String windingVmiVmaTag) {
        if (Math.abs(cod) == 1 && (windingVmi <= 0.0 || windingVma <= 0.0 || windingVma < windingVmi)) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f %.5f", id, windingVmiVmaTag, windingVmi, windingVma));
            validCase = false;
        }
        if ((Math.abs(cod) == 2 || Math.abs(cod) == 3 || Math.abs(cod) == 5) &&  windingVma < windingVmi) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f %.5f", id, windingVmiVmaTag, windingVmi, windingVma));
            validCase = false;
        }
    }

    private void validateTransformerWindingRmiRma(String id, int cod, double windingRmi, double windingRma, String windingRmiRmaTag) {
        if ((Math.abs(cod) == 1 || Math.abs(cod) == 2) && (windingRmi <= 0.0 || windingRma <= 0.0 || windingRma < windingRmi)) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f %.5f", id, windingRmiRmaTag, windingRmi, windingRma));
            validCase = false;
        }
        if ((Math.abs(cod) == 3 || Math.abs(cod) == 5) &&  windingRma < windingRmi) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %.5f %.5f", id, windingRmiRmaTag, windingRmi, windingRma));
            validCase = false;
        }
    }

    private void validateTransformerWindingCont(Map<Integer, List<Integer>> buses, String id, int cod, int windingCont, String windingContTag) {
        if (Math.abs(cod) == 1 && (windingCont == 0 || !buses.containsKey(Math.abs(windingCont)))) {
            warnings.add(String.format(Locale.US, "Transformer: %s Unexpected %s: %d", id, windingContTag, windingCont));
            validCase = false;
        }
    }

    private static PsseBus getRegulatingBus(List<PsseBus> psseBuses, Map<Integer, List<Integer>> buses, int ireg, int i) {
        int regulatingId = i;
        if (ireg != 0) {
            regulatingId = ireg;
        }

        if (buses.containsKey(regulatingId)) {
            return psseBuses.get(buses.get(regulatingId).get(0));
        }

        return null;
    }

    private static Map<String, List<String>> getDuplicates(Map<String, List<String>> busesMap) {
        Map<String, List<String>> duplicatedBusMap = new HashMap<>();

        busesMap.forEach((key, value) -> value.stream().collect(Collectors.groupingBy(s -> s))
            .entrySet()
            .stream()
            .filter(e -> e.getValue().size() > 1)
            .forEach(e -> duplicatedBusMap.put(key, e.getValue())));

        return duplicatedBusMap;
    }

    private static void addBusesMap(Map<String, List<String>> busesMap, int busI, String id) {
        String busString = String.format("%06d", busI);
        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(id);
    }

    private static void addBusesMap(Map<String, List<String>> busesMap, int busI, int busJ, String ckt) {
        String busString;
        if (busI < busJ) {
            busString = String.format("%06d-%06d", busI, busJ);
        } else {
            busString = String.format("%06d-%06d", busJ, busI);
        }

        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(ckt);
    }

    private static void addBusesMap(Map<String, List<String>> busesMap, int busI, int busJ, int busK, String ckt) {
        String busString;
        List<Integer> buses = new ArrayList<>();
        buses.add(busI);
        buses.add(busJ);
        buses.add(busK);
        Collections.sort(buses);
        busString = String.format("%06d-%06d-%06d", buses.get(0), buses.get(1), buses.get(2));

        busesMap.computeIfAbsent(busString, k -> new ArrayList<>()).add(ckt);
    }

    private static int firstBus(String busKey) {
        String[] tokens = busKey.split("-");
        return Integer.valueOf(tokens[0]);
    }

    private static int secondBus(String busKey) {
        String[] tokens = busKey.split("-");
        return Integer.valueOf(tokens[1]);
    }

    private static int thirdBus(String busKey) {
        String[] tokens = busKey.split("-");
        return Integer.valueOf(tokens[2]);
    }

    private List<String> warnings;
    private boolean validCase;
    private static final Logger LOGGER = LoggerFactory.getLogger(PsseValidation.class);
}
