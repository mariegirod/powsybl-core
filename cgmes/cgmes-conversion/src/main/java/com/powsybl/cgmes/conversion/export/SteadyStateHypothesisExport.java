/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class SteadyStateHypothesisExport {

    private static final Logger LOG = LoggerFactory.getLogger(SteadyStateHypothesisExport.class);

    private SteadyStateHypothesisExport() {
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        final Map<String, List<RegulatingControlView>> regulatingControlViews = new HashMap<>();
        String cimNamespace = context.getCimNamespace();

        try {
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);

            if (context.getCimVersion() == 16) {
                CgmesExportUtil.writeModelDescription(writer, context.getSshModelDescription(), context);
            }

            writeEnergyConsumers(network, cimNamespace, writer);
            writeEquivalentInjections(network, cimNamespace, writer);
            writeTapChangers(network, cimNamespace, regulatingControlViews, writer);
            writeShuntCompensators(network, cimNamespace, regulatingControlViews, writer);
            writeSynchronousMachines(network, cimNamespace, regulatingControlViews, writer);
            writeStaticVarCompensators(network, cimNamespace, regulatingControlViews, writer);
            writeRegulatingControls(regulatingControlViews, cimNamespace, writer);
            writeGeneratingUnitsParticitationFactors(network, cimNamespace, writer);
            // TODO writeControlAreas
            writeTerminals(network, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeTerminals(Network network, String cimNamespace, XMLStreamWriter writer) {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                writeTerminal(t, c, cimNamespace, writer);
            }
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            // Terminal for equivalent injection at boundary is always connected
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + "EquivalentInjectionTerminal")
                    .ifPresent(tid -> writeTerminal(tid, true, cimNamespace, writer));
            // Terminal for boundary side of original line/switch is always connected
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + "Terminal_Boundary")
                    .ifPresent(tid -> writeTerminal(tid, true, cimNamespace, writer));
        }
    }

    private static void writeEquivalentInjections(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // One equivalent injection for every dangling line
        for (DanglingLine dl : network.getDanglingLines()) {
            writeEquivalentInjection(dl, cimNamespace, writer);
        }
    }

    private static void writeTapChangers(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + 1)
                        .orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger(twt, ptcId, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER, regulatingControlViews, cimNamespace, writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + 1)
                        .orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeTapChanger(twt, rtcId, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, regulatingControlViews, cimNamespace, writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger(twt, ptcId, leg.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER, regulatingControlViews, cimNamespace, writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeTapChanger(twt, rtcId, leg.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, regulatingControlViews, cimNamespace, writer);
                }
                i++;
            }
        }
    }

    private static void writeTapChanger(Identifiable<?> eq, String tcId, TapChanger<?, ?> tc, String defaultType, Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        String type = eq.getProperty(cgmesTapChangerReferenceKey(tcId, "type"), defaultType);
        writeTapChanger(type, tcId, tc, cimNamespace, writer);
        addTapChangerControl(eq, tcId, tc, regulatingControlViews);
        writeHiddenTapChanger(eq, tcId, defaultType, cimNamespace, writer);
    }

    private static void writeShuntCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            String linearNonlinear;
            switch (s.getModelType()) {
                case LINEAR:
                    linearNonlinear = "Linear";
                    break;
                case NON_LINEAR:
                    linearNonlinear = "Nonlinear";
                    break;
                default:
                    linearNonlinear = "";
                    break;
            }
            boolean controlEnabled = s.isVoltageRegulatorOn();
            writer.writeStartElement(cimNamespace, linearNonlinear + "ShuntCompensator");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + s.getId());
            writer.writeStartElement(cimNamespace, "ShuntCompensator.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeEndElement();

            if (s.hasProperty("RegulatingControl")) {
                // PowSyBl has considered the control as discrete, with a certain targetDeadband
                // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
                String rcid = s.getProperty("RegulatingControl");
                RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, true,
                        s.isVoltageRegulatorOn(), shuntCompensatorTargetDeadBand(s), shuntCompensatorTargetV(s), "k");
                regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static double shuntCompensatorTargetV(ShuntCompensator s) {
        if (s.hasProperty("targetValue")) {
            return Double.parseDouble(s.getProperty("targetValue"));
        }
        return s.getTargetV();
    }

    private static double shuntCompensatorTargetDeadBand(ShuntCompensator s) {
        if (s.hasProperty("targetDeadBand")) {
            return Double.parseDouble(s.getProperty("targetDeadBand"));
        }
        return s.getTargetDeadband();
    }

    private static void writeSynchronousMachines(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (Generator g : network.getGenerators()) {
            boolean controlEnabled = g.isVoltageRegulatorOn();
            writer.writeStartElement(cimNamespace, "SynchronousMachine");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + g.getId());
            writer.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "RotatingMachine.p");
            writer.writeCharacters(CgmesExportUtil.format(g.getTerminal().getP()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "RotatingMachine.q");
            writer.writeCharacters(CgmesExportUtil.format(g.getTerminal().getQ()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SynchronousMachine.referencePriority");
            // reference priority is used for angle reference selection (slack)
            writer.writeCharacters(isInSlackBus(g) ? "1" : "0");
            writer.writeEndElement();
            writer.writeEmptyElement(cimNamespace, "SynchronousMachine.operatingMode");
            // All generators in PowSyBl are considered as generator, not motor
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + "SynchronousMachineOperatingMode.generator");
            writer.writeEndElement();

            if (g.hasProperty("RegulatingControl")) {
                // PowSyBl has considered the control as continuous and with targetDeadband of size 0
                // The target value is stored in kV by PowSyBl, so unit multiplier is "k"
                String rcid = g.getProperty("RegulatingControl");
                double targetDeadBand = Double.parseDouble(g.getProperty("targetDeadBand"));
                RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, false,
                        g.isVoltageRegulatorOn(), targetDeadBand, g.getTargetV(), "k");
                regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static void writeStaticVarCompensators(Network network, String cimNamespace, Map<String, List<RegulatingControlView>> regulatingControlViews, XMLStreamWriter writer) throws XMLStreamException {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            StaticVarCompensator.RegulationMode regulationMode = svc.getRegulationMode();
            boolean controlEnabled = regulationMode != StaticVarCompensator.RegulationMode.OFF;
            writer.writeStartElement(cimNamespace, "StaticVarCompensator");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + svc.getId());
            writer.writeStartElement(cimNamespace, "RegulatingCondEq.controlEnabled");
            writer.writeCharacters(Boolean.toString(controlEnabled));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "StaticVarCompensator.q");
            writer.writeCharacters(CgmesExportUtil.format(svc.getTerminal().getQ()));
            writer.writeEndElement();
            writer.writeEndElement();

            if (svc.hasProperty("RegulatingControl")) {
                String rcid = svc.getProperty("RegulatingControl");
                double targetDeadBand = Double.parseDouble(svc.getProperty("targetDeadBand"));
                // Regulating control could be reactive power or voltage
                double targetValue;
                String multiplier;
                if (regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE) {
                    targetValue = svc.getVoltageSetpoint();
                    multiplier = "k";
                } else if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER) {
                    targetValue = svc.getReactivePowerSetpoint();
                    multiplier = "M";
                } else {
                    // TODO Consider storing targetValue and targetValueUnitMultiplier as properties
                    // during conversion of StaticVarCompensator,
                    // even if the regulating control is not active.
                    // Then obtain here the original values from properties
                    targetValue = 0;
                    multiplier = "k";
                }
                RegulatingControlView rcv = new RegulatingControlView(rcid, RegulatingControlType.REGULATING_CONTROL, false,
                        controlEnabled, targetDeadBand, targetValue, multiplier);
                regulatingControlViews.computeIfAbsent(rcid, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static boolean isInSlackBus(Generator g) {
        VoltageLevel vl = g.getTerminal().getVoltageLevel();
        SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
        if (slackTerminal != null) {
            Bus slackBus = slackTerminal.getTerminal().getBusBreakerView().getBus();
            if (slackBus == g.getTerminal().getBusBreakerView().getBus()) {
                return true;
            }
        }
        return false;
    }

    private static void writeTapChanger(String type, String id, TapChanger<?, ?> tc, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writeTapChanger(type, id, tc.isRegulating(), tc.getTapPosition(), cimNamespace, writer);
    }

    private static void writeTapChanger(String type, String id, boolean controlEnabled, int step, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, type);
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(cimNamespace, "TapChanger.controlEnabled");
        writer.writeCharacters(Boolean.toString(controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "TapChanger.step");
        writer.writeCharacters(CgmesExportUtil.format(step));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void addTapChangerControl(Identifiable<?> eq, String tcId, TapChanger tc, Map<String, List<RegulatingControlView>> regulatingControlViews) {
        // Multiple tap changers can be stored at the same equipment
        // We use the tap changer id as part of the key for storing the tap changer control id
        String key = String.format("%s%s.TapChangerControl", Conversion.CGMES_PREFIX_ALIAS, tcId);
        if (eq.hasProperty(key)) {
            String controlId = eq.getProperty(key);
            RegulatingControlView rcv = null;
            if (tc instanceof RatioTapChanger) {
                rcv = new RegulatingControlView(controlId,
                        RegulatingControlType.TAP_CHANGER_CONTROL,
                        true,
                        tc.isRegulating(),
                        tc.getTargetDeadband(),
                        ((RatioTapChanger) tc).getTargetV(),
                        // Unit multiplier is k for ratio tap changers (regulation value is a voltage in kV)
                        "k");
            } else if (tc instanceof PhaseTapChanger) {
                rcv = new RegulatingControlView(controlId,
                        RegulatingControlType.TAP_CHANGER_CONTROL,
                        true,
                        tc.isRegulating(),
                        tc.getTargetDeadband(),
                        ((PhaseTapChanger) tc).getRegulationValue(),
                        // Unit multiplier is M for phase tap changers (regulation value is an active power flow in MW)
                        "M");
            }
            if (rcv != null) {
                regulatingControlViews.computeIfAbsent(controlId, k -> new ArrayList<>()).add(rcv);
            }
        }
    }

    private static String cgmesTapChangerReferenceKey(String tcId, String property) {
        return String.format("%s%s.%s", Conversion.CGMES_PREFIX_ALIAS, tcId, property);
    }

    private static void writeHiddenTapChanger(Identifiable<?> eq, String tcId, String defaultType, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        String key = cgmesTapChangerReferenceKey(tcId, "hiddenTapChangerId");
        if (!eq.hasProperty(key)) {
            return;
        }
        String hiddenTcId = eq.getProperty(key);
        int step = Integer.parseInt(eq.getProperty(cgmesTapChangerReferenceKey(hiddenTcId, "step")));
        String type = eq.getProperty(cgmesTapChangerReferenceKey(hiddenTcId, "type"), defaultType);
        writeTapChanger(type, hiddenTcId, false, step, cimNamespace, writer);
    }

    private static void writeRegulatingControls(Map<String, List<RegulatingControlView>> regulatingControlViews, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (List<RegulatingControlView> views : regulatingControlViews.values()) {
            writeRegulatingControl(combineRegulatingControlViews(views), cimNamespace, writer);
        }
    }

    private static RegulatingControlView combineRegulatingControlViews(List<RegulatingControlView> rcs) {
        return rcs.get(0); // TODO
    }

    private static void writeRegulatingControl(RegulatingControlView rc, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, regulatingControlClassname(rc.type));
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + rc.id);
        writer.writeStartElement(cimNamespace, "RegulatingControl.discrete");
        writer.writeCharacters(Boolean.toString(rc.discrete));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RegulatingControl.enabled");
        writer.writeCharacters(Boolean.toString(rc.controlEnabled));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RegulatingControl.targetDeadband");
        writer.writeCharacters(CgmesExportUtil.format(rc.targetDeadband));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "RegulatingControl.targetValue");
        writer.writeCharacters(CgmesExportUtil.format(rc.targetValue));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "RegulatingControl.targetValueUnitMultiplier");
        writer.writeAttribute(RDF_NAMESPACE, "resource", cimNamespace + "UnitMultiplier." + rc.targetValueUnitMultiplier);
        writer.writeEndElement();
    }

    private static String regulatingControlClassname(RegulatingControlType type) {
        if (type == RegulatingControlType.TAP_CHANGER_CONTROL) {
            return "TapChangerControl";
        } else {
            return "RegulatingControl";
        }
    }

    private static void writeTerminal(Terminal t, Connectable<?> c, String cimNamespace, XMLStreamWriter writer) {
        Optional<String> tid;
        if (c instanceof DanglingLine) {
            tid = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + "Terminal_Network");
        } else {
            int numt = 0;
            if (c.getTerminals().size() == 1) {
                numt = 1;
            } else {
                if (c instanceof Injection) {
                    // An injection should have only one terminal
                } else if (c instanceof Branch) {
                    switch (((Branch<?>) c).getSide(t)) {
                        case ONE:
                            numt = 1;
                            break;
                        case TWO:
                            numt = 2;
                            break;
                    }
                } else if (c instanceof ThreeWindingsTransformer) {
                    switch (((ThreeWindingsTransformer) c).getSide(t)) {
                        case ONE:
                            numt = 1;
                            break;
                        case TWO:
                            numt = 2;
                            break;
                        case THREE:
                            numt = 3;
                            break;
                    }
                } else {
                    throw new PowsyblException("Unexpected Connectable instance: " + c.getClass());
                }
            }
            tid = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.TERMINAL + numt);
        }
        if (tid.isPresent()) {
            writeTerminal(tid.get(), t.isConnected(), cimNamespace, writer);
        } else {
            LOG.error("Alias not found for terminal {} in connectable {}", t, c.getId());
        }
    }

    private static void writeTerminal(String terminalId, boolean connected, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, CgmesNames.TERMINAL);
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + terminalId);
            writer.writeStartElement(cimNamespace, "ACDCTerminal.connected");
            writer.writeCharacters(Boolean.toString(connected));
            writer.writeEndElement();
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeEquivalentInjection(DanglingLine dl, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        Optional<String> ei = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + "EquivalentInjection");
        if (ei.isPresent()) {
            writer.writeStartElement(cimNamespace, "EquivalentInjection");
            writer.writeAttribute(RDF_NAMESPACE, "about", "#" + ei.get());
            writer.writeStartElement(cimNamespace, "EquivalentInjection.p");
            writer.writeCharacters(CgmesExportUtil.format(dl.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "EquivalentInjection.q");
            writer.writeCharacters(CgmesExportUtil.format(dl.getQ0()));
            writer.writeEndElement();
            // regulationStatus and regulationTarget are optional,
            // but test cases contain the attributes with disabled and 0
            boolean regulationStatus = false;
            double regulationTarget = 0;
            if (dl.getGeneration() != null) {
                regulationStatus = dl.getGeneration().isVoltageRegulationOn();
                regulationTarget = dl.getGeneration().getTargetV();
            }
            writer.writeStartElement(cimNamespace, "EquivalentInjection.regulationStatus");
            writer.writeCharacters(Boolean.toString(regulationStatus));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "EquivalentInjection.regulationTarget");
            writer.writeCharacters(CgmesExportUtil.format(regulationTarget));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeEnergyConsumers(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (Load load : network.getLoads()) {
            writeSshEnergyConsumer(load.getId(), load.getP0(), load.getQ0(), load.getExtension(LoadDetail.class), cimNamespace, writer);
        }
    }

    private static void writeSshEnergyConsumer(String id, double p, double q, LoadDetail loadDetail, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, loadClassName(loadDetail));
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + id);
        writer.writeStartElement(cimNamespace, "EnergyConsumer.p");
        writer.writeCharacters(CgmesExportUtil.format(p));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, "EnergyConsumer.q");
        writer.writeCharacters(CgmesExportUtil.format(q));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String loadClassName(LoadDetail loadDetail) {
        if (loadDetail != null) {
            // Conform load if fixed part is zero and variable part is non-zero
            if (loadDetail.getFixedActivePower() == 0 && loadDetail.getFixedReactivePower() == 0
                    && (loadDetail.getVariableActivePower() != 0 || loadDetail.getVariableReactivePower() != 0)) {
                return "ConformLoad";
            }
            // NonConform load if fixed part is non-zero and variable part is all zero
            if (loadDetail.getVariableActivePower() == 0 && loadDetail.getVariableReactivePower() == 0
                    && (loadDetail.getFixedActivePower() != 0 || loadDetail.getFixedReactivePower() != 0)) {
                return "NonConformLoad";
            }
        }
        return "EnergyConsumer";
    }

    private static void writeGeneratingUnitsParticitationFactors(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        // Multiple generators may share the same generation unit,
        // we will choose the participation factor from the last generator that references the generating unit
        // We only consider generators that have participation factors
        Map<String, GeneratingUnit> generatingUnits = new HashMap<>();
        for (Generator g : network.getGenerators()) {
            GeneratingUnit gu = generatingUnitForGenerator(g);
            if (gu != null) {
                generatingUnits.put(gu.id, gu);
            }
        }
        for (GeneratingUnit gu : generatingUnits.values()) {
            writeGeneratingUnitParticipationFactor(gu, cimNamespace, writer);
        }
    }

    private static GeneratingUnit generatingUnitForGenerator(Generator g) {
        if (g.hasProperty("GeneratingUnit")) {
            ActivePowerControl apc = g.getExtension(ActivePowerControl.class);
            if (apc != null) {
                GeneratingUnit gu = new GeneratingUnit();
                gu.id = g.getProperty("GeneratingUnit");
                gu.participationFactor = apc.getDroop();
                gu.className = generatingUnitClassname(g);
                return gu;
            }
        }
        return null;
    }

    private static void writeGeneratingUnitParticipationFactor(GeneratingUnit gu, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, gu.className);
        writer.writeAttribute(RDF_NAMESPACE, "about", "#" + gu.id);
        writer.writeStartElement(cimNamespace, "GeneratingUnit.normalPF");
        writer.writeCharacters(CgmesExportUtil.format(gu.participationFactor));
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String generatingUnitClassname(Generator g) {
        EnergySource energySource = g.getEnergySource();
        if (energySource == EnergySource.HYDRO) {
            return "HydroGeneratingUnit";
        } else if (energySource == EnergySource.NUCLEAR) {
            return "NuclearGeneratingUnit";
        } else if (energySource == EnergySource.SOLAR) {
            return "SolarGeneratingUnit";
        } else if (energySource == EnergySource.THERMAL) {
            return "ThermalGeneratingUnit";
        } else if (energySource == EnergySource.WIND) {
            return "WindGeneratingUnit";
        } else {
            return "GeneratingUnit";
        }
    }

    private enum RegulatingControlType {
        REGULATING_CONTROL, TAP_CHANGER_CONTROL
    }

    private static class GeneratingUnit {
        String id;
        String className;
        double participationFactor;
    }

    static class RegulatingControlView {
        String id;
        RegulatingControlType type;
        boolean discrete;
        boolean controlEnabled;
        double targetDeadband;
        double targetValue;
        String targetValueUnitMultiplier;

        RegulatingControlView(String id, RegulatingControlType type, boolean discrete, boolean controlEnabled,
                              double targetDeadband, double targetValue, String targetValueUnitMultiplier) {
            this.id = id;
            this.type = type;
            this.discrete = discrete;
            this.controlEnabled = controlEnabled;
            this.targetDeadband = targetDeadband;
            this.targetValue = targetValue;
            this.targetValueUnitMultiplier = targetValueUnitMultiplier;
        }
    }
}