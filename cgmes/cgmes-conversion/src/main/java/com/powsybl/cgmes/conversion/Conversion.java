/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.*;
import com.powsybl.cgmes.conversion.elements.hvdc.CgmesDcConversion;
import com.powsybl.cgmes.conversion.elements.transformers.ThreeWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasAdder;
import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasImpl.ControlArea;
import com.powsybl.cgmes.conversion.extensions.CgmesSshMetadataAdder;
import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadataAdder;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.Conversion.Config.StateProfile.SSH;

/**
 * TwoWindingsTransformer Interpretation
 * <p>
 * Ratio and Phase Interpretation (Xfmr2RatioPhaseInterpretationAlternative) <br>
 * END1. All tapChangers (ratioTapChanger and phaseTapChanger) are considered at end1 (before transmission impedance) <br>
 * END2. All tapChangers (ratioTapChanger and phaseTapChanger) are considered at end2 (after transmission impedance) <br>
 * END1_END2. TapChangers (ratioTapChanger and phaseTapChanger) are considered at the end where they are defined in Cgmes <br>
 * X. If x1 == 0 all tapChangers (ratioTapChanger and phaseTapChanger) are considered at the end1 otherwise they are considered at end2
 * <p>
 * Shunt Admittance Interpretation (Xfmr2ShuntInterpretationAlternative) <br>
 * END1. All shunt admittances to ground (g, b) at end1 (before transmission impedance) <br>
 * END2. All shunt admittances to ground (g, b) at end2 (after transmission impedance) <br>
 * END1_END2. Shunt admittances to ground (g, b) at the end where they are defined in Cgmes model <br>
 * SPLIT. Split shunt admittances to ground (g, b) between end1 and end2. <br>
 * <p>
 * Structural Ratio (Xfmr2StructuralRatioInterpretationAlternative) <br>
 * END1. Structural ratio always at end1 (before transmission impedance) <br>
 * END2. Structural ratio always at end2 (after transmission impedance) <br>
 * X. If x1 == 0 structural ratio at end1, otherwise at end2
 * <p>
 * ThreeWindingsTransformer Interpretation.
 * <p>
 * Ratio and Phase Interpretation.  (Xfmr3RatioPhaseInterpretationAlternative) <br>
 * NETWORK_SIDE. All tapChangers (ratioTapChanger and phaseTapChanger) at the network side. <br>
 * STAR_BUS_SIDE. All tapChangers (ratioTapChanger and phaseTapChanger) at the star bus side.
 * <p>
 * Shunt Admittance Interpretation (Xfmr3ShuntInterpretationAlternative) <br>
 * NETWORK_SIDE. Shunt admittances to ground at the network side (end1 of the leg) <br>
 * STAR_BUS_SIDE. Shunt admittances to ground at the start bus side (end2 of the leg) <br>
 * SPLIT. Split shunt admittances to ground between two ends of the leg
 * <p>
 * Structural Ratio Interpretation (Xfmr3StructuralRatioInterpretationAlternative) <br>
 * STAR_BUS_SIDE. Structural ratio at the star bus side of all legs and RatedU0 = RatedU1 <br>
 * NETWORK_SIDE. Structural ratio at the network side of all legs. RatedU0 = 1 kv <br>
 * END1. Structural ratio at the network side of legs 2 and 3. RatedU0 = RatedU1 <br>
 * END2. Structural ratio at the network side of legs 1 and 3. RatedU0 = RatedU2 <br>
 * END3. Structural ratio at the network side of legs 1 and 2. RatedU0 = RatedU2 <br>
 * <p>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 *
 */
public class Conversion {

    public enum Xfmr2RatioPhaseInterpretationAlternative {
        END1, END2, END1_END2, X
    }

    public enum Xfmr2ShuntInterpretationAlternative {
        END1, END2, END1_END2, SPLIT
    }

    public enum Xfmr2StructuralRatioInterpretationAlternative {
        END1, END2, X
    }

    public enum Xfmr3RatioPhaseInterpretationAlternative {
        NETWORK_SIDE, STAR_BUS_SIDE
    }

    public enum Xfmr3ShuntInterpretationAlternative {
        NETWORK_SIDE, STAR_BUS_SIDE, SPLIT
    }

    public enum Xfmr3StructuralRatioInterpretationAlternative {
        NETWORK_SIDE, STAR_BUS_SIDE, END1, END2, END3
    }

    public Conversion(CgmesModel cgmes) {
        this(cgmes, new Config());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config) {
        this(cgmes, config, Collections.emptyList());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        this(cgmes, config, postProcessors, NetworkFactory.findDefault());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors,
                      NetworkFactory networkFactory) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.postProcessors = Objects.requireNonNull(postProcessors);
        this.networkFactory = Objects.requireNonNull(networkFactory);
    }

    public void report(Consumer<String> out) {
        new ReportTapChangers(cgmes, out).report();
    }

    public Network convert() {
        if (LOG.isDebugEnabled() && cgmes.baseVoltages() != null) {
            LOG.debug(cgmes.baseVoltages().tabulate());
        }
        // Check that at least we have an EquipmentCore profile
        if (!cgmes.hasEquipmentCore()) {
            throw new CgmesModelException("Data source does not contain EquipmentCore data");
        }
        Network network = createNetwork();
        network.setProperty("CGMES_topology", cgmes.isNodeBreaker() ? "NODE_BREAKER" : "BUS_BRANCH");
        if (cgmes instanceof CgmesModelTripleStore) {
            network.setProperty("CIM_version", String.valueOf(((CgmesModelTripleStore) cgmes).getCimVersion()));
        }
        Context context = createContext(network);
        assignNetworkProperties(context);
        addCgmesSvMetadata(network);
        addCgmesSshMetadata(network);
        addCgmesSshControlAreas(network);

        Function<PropertyBag, AbstractObjectConversion> convf;

        cgmes.terminals().forEach(p -> context.terminalMapping().buildTopologicalNodesMapping(p));
        cgmes.regulatingControls().forEach(p -> context.regulatingControlMapping().cacheRegulatingControls(p));

        convert(cgmes.substations(), s -> new SubstationConversion(s, context));
        convert(cgmes.voltageLevels(), vl -> new VoltageLevelConversion(vl, context));
        PropertyBags nodes = context.nodeBreaker()
                ? cgmes.connectivityNodes()
                : cgmes.topologicalNodes();
        String nodeTypeName = context.nodeBreaker()
                ? "ConnectivityNode"
                : "TopologicalNode";
        convert(nodes, n -> new NodeConversion(nodeTypeName, n, context));
        if (!context.config().createBusbarSectionForEveryConnectivityNode()) {
            convert(cgmes.busBarSections(), bbs -> new BusbarSectionConversion(bbs, context));
        }

        convert(cgmes.energyConsumers(), ec -> new EnergyConsumerConversion(ec, context));
        convert(cgmes.energySources(), es -> new EnergySourceConversion(es, context));
        convf = eqi -> new EquivalentInjectionConversion(eqi, context);
        convert(cgmes.equivalentInjections(), convf);
        convf = eni -> new ExternalNetworkInjectionConversion(eni, context);
        convert(cgmes.externalNetworkInjections(), convf);
        convert(cgmes.shuntCompensators(), sh -> new ShuntConversion(sh, context));
        convert(cgmes.equivalentShunts(), es -> new EquivalentShuntConversion(es, context));
        convf = svc -> new StaticVarCompensatorConversion(svc, context);
        convert(cgmes.staticVarCompensators(), convf);
        convf = asm -> new AsynchronousMachineConversion(asm, context);
        convert(cgmes.asynchronousMachines(), convf);
        convert(cgmes.synchronousMachines(), sm -> new SynchronousMachineConversion(sm, context));

        // We will delay the conversion of some lines/switches that have an end at boundary
        // They have to be processed after all lines/switches have been reviewed
        // FIXME(Luma) store delayedBoundaryNodes in context
        Set<String> delayedBoundaryNodes = new HashSet<>();
        convertSwitches(context, delayedBoundaryNodes);
        convertACLineSegmentsToLines(context, delayedBoundaryNodes);
        delayedBoundaryNodes.forEach(node -> convertEquipmentAtBoundaryNode(context, node));

        convert(cgmes.equivalentBranches(), eqb -> new EquivalentBranchConversion(eqb, context));
        convert(cgmes.seriesCompensators(), sc -> new SeriesCompensatorConversion(sc, context));

        convertTransformers(context);

        CgmesDcConversion cgmesDcConversion = new CgmesDcConversion(cgmes, context);
        cgmesDcConversion.convert();

        convert(cgmes.operationalLimits(), l -> new OperationalLimitConversion(l, context));
        context.currentLimitsMapping().addAll();

        if (config.convertSvInjections()) {
            convert(cgmes.svInjections(), si -> new SvInjectionConversion(si, context));
        }

        clearUnattachedHvdcConverterStations(network, context); // in case of faulty CGMES files, remove HVDC Converter Stations without HVDC lines
        voltageAngles(nodes, context);

        // set all regulating controls
        context.regulatingControlMapping().setAllRegulatingControls(network);
        if (context.config().debugTopology()) {
            debugTopology(context);
        }

        if (config.storeCgmesModelAsNetworkExtension()) {
            // Store a reference to the original CGMES model inside the IIDM network
            // CgmesUpdate will add a listener to Network changes
            CgmesUpdate cgmesUpdater = new CgmesUpdate(network);
            network.newExtension(CgmesModelExtensionAdder.class).withModel(cgmes).withUpdate(cgmesUpdater).add();
        }

        // apply post-processors
        for (CgmesImportPostProcessor postProcessor : postProcessors) {
            // FIXME generic cgmes models may not have an underlying triplestore
            postProcessor.process(network, cgmes.tripleStore());
        }

        if (config.storeCgmesConversionContextAsNetworkExtension()) {
            // Store the terminal mapping in an extension for external validation
            network.newExtension(CgmesConversionContextExtensionAdder.class).withContext(context).add();
        }

        return network;
    }

    private void convert(
            PropertyBags elements,
            Function<PropertyBag, AbstractObjectConversion> f) {
        String conversion = null;

        for (PropertyBag element : elements) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(element.tabulateLocals());
            }
            AbstractObjectConversion c = f.apply(element);
            if (conversion == null) {
                conversion = c.getClass().getName();
                conversion = conversion.substring(conversion.lastIndexOf('.') + 1);
                conversion = conversion.replace("Conversion", "");
            }
            if (c.insideBoundary()) {
                c.convertInsideBoundary();
            } else if (c.valid()) {
                c.convert();
            }
        }
    }

    private Network createNetwork() {
        String networkId = cgmes.modelId();
        String sourceFormat = "CGMES";
        Network network = networkFactory.createNetwork(networkId, sourceFormat);
        return network;
    }

    private Context createContext(Network network) {
        Context context = new Context(cgmes, config, network);
        context.substationIdMapping().build();
        context.dc().initialize();
        context.loadRatioTapChangers();
        context.loadPhaseTapChangers();
        context.loadRatioTapChangerTables();
        context.loadPhaseTapChangerTables();
        context.loadReactiveCapabilityCurveData();
        return context;
    }

    private void assignNetworkProperties(Context context) {
        context.network().setProperty(NETWORK_PS_CGMES_MODEL_DETAIL,
                context.nodeBreaker()
                        ? NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER
                        : NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH);
        DateTime modelScenarioTime = cgmes.scenarioTime();
        DateTime modelCreated = cgmes.created();
        long forecastDistance = new Duration(modelCreated, modelScenarioTime).getStandardMinutes();
        context.network().setForecastDistance(forecastDistance >= 0 ? (int) forecastDistance : 0);
        context.network().setCaseDate(modelScenarioTime);
        LOG.info("cgmes scenarioTime       : {}", modelScenarioTime);
        LOG.info("cgmes modelCreated       : {}", modelCreated);
        LOG.info("network caseDate         : {}", context.network().getCaseDate());
        LOG.info("network forecastDistance : {}", context.network().getForecastDistance());
    }

    private void addCgmesSvMetadata(Network network) {
        PropertyBags svDescription = cgmes.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());
        if (svDescription != null && !svDescription.isEmpty()) {
            CgmesSvMetadataAdder adder = network.newExtension(CgmesSvMetadataAdder.class)
                    .setScenarioTime(svDescription.get(0).getId("scenarioTime"))
                    .setDescription(svDescription.get(0).getId("description"))
                    .setSvVersion(svDescription.get(0).asInt("version"))
                    .setModelingAuthoritySet(svDescription.get(0).getId("modelingAuthoritySet"));
            svDescription.pluckLocals("DependentOn").forEach(adder::addDependency);
            adder.add();
        }
    }

    private void addCgmesSshMetadata(Network network) {
        PropertyBags sshDescription = cgmes.fullModel(CgmesSubset.STEADY_STATE_HYPOTHESIS.getProfile());
        if (sshDescription != null && !sshDescription.isEmpty()) {
            CgmesSshMetadataAdder adder = network.newExtension(CgmesSshMetadataAdder.class)
                    .setScenarioTime(sshDescription.get(0).getId("scenarioTime"))
                    .setDescription(sshDescription.get(0).getId("description"))
                    .setSshVersion(sshDescription.get(0).asInt("version"))
                    .setModelingAuthoritySet(sshDescription.get(0).getId("modelingAuthoritySet"));
            sshDescription.pluckLocals("DependentOn").forEach(adder::addDependency);
            adder.add();
        }
    }

    private void addCgmesSshControlAreas(Network network) {
        PropertyBags sshControlAreas = cgmes.controlAreas();
        if (sshControlAreas != null && !sshControlAreas.isEmpty()) {
            CgmesSshControlAreasAdder adder = network.newExtension(CgmesSshControlAreasAdder.class);

            sshControlAreas.forEach(sshControlArea -> {
                String id = sshControlArea.getId("ControlArea");
                double netInterchange = sshControlArea.asDouble("netInterchange");
                double pTolerance = sshControlArea.asDouble("pTolerance");
                ControlArea controlArea = new ControlArea(id, netInterchange, pTolerance);
                adder.addControlArea(controlArea);
            });
            adder.add();
        }
    }

    private void convertACLineSegmentsToLines(Context context, Set<String> delayedBoundaryNodes) {
        Iterator<PropertyBag> k = cgmes.acLineSegments().iterator();
        while (k.hasNext()) {
            PropertyBag line = k.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(line.tabulateLocals("ACLineSegment"));
            }
            ACLineSegmentConversion c = new ACLineSegmentConversion(line, context);
            if (c.valid()) {
                String node = c.boundaryNode();
                if (node != null) {
                    context.boundary().addEquipmentAtNode(line, node);
                    delayedBoundaryNodes.add(node);
                } else {
                    c.convert();
                }
            }
        }
    }

    private void convertSwitches(Context context, Set<String> delayedBoundaryNodes) {
        Iterator<PropertyBag> k = cgmes.switches().iterator();
        while (k.hasNext()) {
            PropertyBag sw = k.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(sw.tabulateLocals("Switch"));
            }
            SwitchConversion c = new SwitchConversion(sw, context);
            if (c.valid()) {
                String node = c.boundaryNode();
                if (node != null) {
                    context.boundary().addEquipmentAtNode(sw, node);
                    delayedBoundaryNodes.add(node);
                } else {
                    c.convert();
                }
            }
        }
    }

    private void convertEquipmentAtBoundaryNode(Context context, String node) {
        // At least each delayed boundary node should have one equipment attached to it
        // Currently supported equipment at boundary are lines and switches
        List<PropertyBag> beqs = context.boundary().equipmentAtNode(node);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delayed boundary node {} with {} equipment at it", node, beqs.size());
            beqs.forEach(beq -> {
                LOG.debug(beq.tabulateLocals("EquipmentAtBoundary")); });
        }
        if (beqs.size() > 2) {
            context.invalid(node, "Too many equipment at boundary node");
            return;
        }
        // FIXME(Luma) Supported conversions:
        // Only one line (--> create dangling line)
        // Only one switch (--> create dangling line with z0)
        // Two lines (--> merge both lines and replace by equivalent)
        // Line and switch (--> switch to z0 line and merge both lines)
        if (beqs.size() == 1) {
            // FIXME(Luma) check it is a line
            PropertyBag beq = beqs.get(0);
            String lineId = beq.getId(CgmesNames.AC_LINE_SEGMENT);
            String switchId = beq.getId("Switch");
            if (lineId != null) {
                new ACLineSegmentConversion(beq, context).convert();
            } else if (switchId != null) {
                new SwitchConversion(beq, context).convert();
            } else {
                // Should have been a line or a switch
                context.invalid(node, "Unexpected equipment at boundary node. Expected ACLineSegment or Switch");
            }
        } else {
            // Exactly two equipment at boundary node
            String lineId0 = beqs.get(0).getId(CgmesNames.AC_LINE_SEGMENT);
            String lineId1 = beqs.get(1).getId(CgmesNames.AC_LINE_SEGMENT);
            String switchId0 = beqs.get(0).getId("Switch");
            String switchId1 = beqs.get(1).getId("Switch");
            if (lineId0 != null && lineId1 != null) {
                PropertyBag line0 = beqs.get(0);
                PropertyBag line1 = beqs.get(1);
                new ACLineSegmentConversion(line0, context).convertMergedLinesAtNode(line1, node);
            } else if (switchId0 != null && switchId1 != null) {
                context.ignored(node, "Found two Switches at boundary node. Boundary configuration not supported");
            } else {
                // Must be a switch and a line
                PropertyBag line;
                PropertyBag sw;
                if (lineId0 != null && switchId1 != null) {
                    line = beqs.get(0);
                    sw = beqs.get(1);
                } else if (lineId1 != null && switchId0 != null) {
                    line = beqs.get(1);
                    sw = beqs.get(0);
                } else {
                    throw new PowsyblException("Not supported ???");
                }
                throw new PowsyblException("Implementation pending convertLineAndSwitchAtNode " + line + "," + sw);
                // FIXME(Luma) implement
                // new ACLineSegmentConversion(line, context).convertLineAndSwitchAtNode(sw, node);
            }
        }
    }

    private void convertTransformers(Context context) {
        cgmes.groupedTransformerEnds().forEach((t, ends) -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transformer {}, {}-winding", t, ends.size());
                ends.forEach(e -> LOG.debug(e.tabulateLocals("TransformerEnd")));
            }
            AbstractConductingEquipmentConversion c = null;
            if (ends.size() == 2) {
                c = new TwoWindingsTransformerConversion(ends, context);
            } else if (ends.size() == 3) {
                c = new ThreeWindingsTransformerConversion(ends, context);
            } else {
                String what = "PowerTransformer " + t;
                Supplier<String> reason = () -> String.format("Has %d ends. Only 2 or 3 ends are supported", ends.size());
                context.invalid(what, reason);
            }
            if (c != null && c.valid()) {
                c.convert();
            }
        });
    }

    private void voltageAngles(PropertyBags nodes, Context context) {
        if (context.nodeBreaker()) {
            // TODO(Luma): we create again one conversion object for every node
            // In node-breaker conversion,
            // set (voltage, angle) values after all nodes have been created and connected
            for (PropertyBag n : nodes) {
                NodeConversion nc = new NodeConversion("ConnectivityNode", n, context);
                if (!nc.insideBoundary()) {
                    nc.setVoltageAngleNodeBreaker();
                }
            }
        }
    }

    private void clearUnattachedHvdcConverterStations(Network network, Context context) {
        network.getHvdcConverterStationStream()
                .filter(converter -> converter.getHvdcLine() == null)
                .peek(converter -> context.ignored("HVDC Converter Station " + converter.getId(), "No correct linked HVDC line found."))
                .collect(Collectors.toList())
                .forEach(Connectable::remove);
    }

    private void debugTopology(Context context) {
        context.network().getVoltageLevels().forEach(vl -> {
            String name = vl.getSubstation().getNameOrId() + "-" + vl.getNameOrId();
            name = name.replace('/', '-');
            Path file = Paths.get(System.getProperty("java.io.tmpdir"), "temp-cgmes-" + name + ".dot");
            try {
                vl.exportTopology(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static class Config {

        public enum StateProfile {
            SSH,
            SV
        }

        public List<String> substationIdsExcludedFromMapping() {
            return Collections.emptyList();
        }

        public boolean debugTopology() {
            return false;
        }

        public boolean allowUnsupportedTapChangers() {
            return allowUnsupportedTapChangers;
        }

        public Config setAllowUnsupportedTapChangers(boolean allowUnsupportedTapChangers) {
            this.allowUnsupportedTapChangers = allowUnsupportedTapChangers;
            return this;
        }

        public boolean useNodeBreaker() {
            return true;
        }

        public double lowImpedanceLineR() {
            return lowImpedanceLineR;
        }

        public double lowImpedanceLineX() {
            return lowImpedanceLineX;
        }

        public boolean convertBoundary() {
            return convertBoundary;
        }

        public Config setConvertBoundary(boolean convertBoundary) {
            this.convertBoundary = convertBoundary;
            return this;
        }

        public boolean mergeLinesUsingQuadripole() {
            return true;
        }

        public boolean changeSignForShuntReactivePowerFlowInitialState() {
            return changeSignForShuntReactivePowerFlowInitialState;
        }

        public Config setChangeSignForShuntReactivePowerFlowInitialState(boolean b) {
            changeSignForShuntReactivePowerFlowInitialState = b;
            return this;
        }

        public boolean computeFlowsAtBoundaryDanglingLines() {
            return true;
        }

        public boolean createBusbarSectionForEveryConnectivityNode() {
            return createBusbarSectionForEveryConnectivityNode;
        }

        public Config setCreateBusbarSectionForEveryConnectivityNode(boolean b) {
            createBusbarSectionForEveryConnectivityNode = b;
            return this;
        }

        public boolean convertSvInjections() {
            return convertSvInjections;
        }

        public Config setConvertSvInjections(boolean convertSvInjections) {
            this.convertSvInjections = convertSvInjections;
            return this;
        }

        public StateProfile getProfileUsedForInitialStateValues() {
            return profileUsedForInitialStateValues;
        }

        public Config setProfileUsedForInitialStateValues(String profileUsedForInitialFlowsValues) {
            switch (Objects.requireNonNull(profileUsedForInitialFlowsValues)) {
                case "SSH":
                case "SV":
                    this.profileUsedForInitialStateValues = StateProfile.valueOf(profileUsedForInitialFlowsValues);
                    break;
                default:
                    throw new CgmesModelException("Unexpected profile used for state hypothesis: " + profileUsedForInitialFlowsValues);
            }
            return this;
        }

        public boolean storeCgmesModelAsNetworkExtension() {
            return storeCgmesModelAsNetworkExtension;
        }

        public Config setStoreCgmesModelAsNetworkExtension(boolean storeCgmesModelAsNetworkExtension) {
            this.storeCgmesModelAsNetworkExtension = storeCgmesModelAsNetworkExtension;
            return this;
        }

        public boolean storeCgmesConversionContextAsNetworkExtension() {
            return storeCgmesConversionContextAsNetworkExtension;
        }

        public Config setStoreCgmesConversionContextAsNetworkExtension(boolean storeCgmesTerminalMappingAsNetworkExtension) {
            this.storeCgmesConversionContextAsNetworkExtension = storeCgmesTerminalMappingAsNetworkExtension;
            return this;
        }

        public Xfmr2RatioPhaseInterpretationAlternative getXfmr2RatioPhase() {
            return xfmr2RatioPhase;
        }

        public void setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative alternative) {
            xfmr2RatioPhase = alternative;
        }

        public Xfmr2ShuntInterpretationAlternative getXfmr2Shunt() {
            return xfmr2Shunt;
        }

        public void setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative alternative) {
            xfmr2Shunt = alternative;
        }

        public Xfmr2StructuralRatioInterpretationAlternative getXfmr2StructuralRatio() {
            return xfmr2StructuralRatio;
        }

        public void setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative alternative) {
            xfmr2StructuralRatio = alternative;
        }

        public Xfmr3RatioPhaseInterpretationAlternative getXfmr3RatioPhase() {
            return xfmr3RatioPhase;
        }

        public void setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative alternative) {
            this.xfmr3RatioPhase = alternative;
        }

        public Xfmr3ShuntInterpretationAlternative getXfmr3Shunt() {
            return xfmr3Shunt;
        }

        public void setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative alternative) {
            xfmr3Shunt = alternative;
        }

        public Xfmr3StructuralRatioInterpretationAlternative getXfmr3StructuralRatio() {
            return xfmr3StructuralRatio;
        }

        public void setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative alternative) {
            xfmr3StructuralRatio = alternative;
        }

        private boolean allowUnsupportedTapChangers = true;
        private boolean convertBoundary = false;
        private boolean changeSignForShuntReactivePowerFlowInitialState = false;
        private double lowImpedanceLineR = 7.0E-5;
        private double lowImpedanceLineX = 7.0E-5;

        private boolean createBusbarSectionForEveryConnectivityNode = false;
        private boolean convertSvInjections = true;
        private StateProfile profileUsedForInitialStateValues = SSH;
        private boolean storeCgmesModelAsNetworkExtension = true;
        private boolean storeCgmesConversionContextAsNetworkExtension = false;

        // Default interpretation.
        private Xfmr2RatioPhaseInterpretationAlternative xfmr2RatioPhase = Xfmr2RatioPhaseInterpretationAlternative.END1_END2;
        private Xfmr2ShuntInterpretationAlternative xfmr2Shunt = Xfmr2ShuntInterpretationAlternative.END1_END2;
        private Xfmr2StructuralRatioInterpretationAlternative xfmr2StructuralRatio = Xfmr2StructuralRatioInterpretationAlternative.X;

        private Xfmr3RatioPhaseInterpretationAlternative xfmr3RatioPhase = Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE;
        private Xfmr3ShuntInterpretationAlternative xfmr3Shunt = Xfmr3ShuntInterpretationAlternative.NETWORK_SIDE;
        private Xfmr3StructuralRatioInterpretationAlternative xfmr3StructuralRatio = Xfmr3StructuralRatioInterpretationAlternative.STAR_BUS_SIDE;

    }

    private final CgmesModel cgmes;
    private final Config config;
    private final List<CgmesImportPostProcessor> postProcessors;
    private final NetworkFactory networkFactory;

    private static final Logger LOG = LoggerFactory.getLogger(Conversion.class);

    public static final String NETWORK_PS_CGMES_MODEL_DETAIL = "CGMESModelDetail";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH = "bus-branch";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER = "node-breaker";
}
