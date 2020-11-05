/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import org.apache.commons.math3.complex.Complex;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.update.CgmesExportUtil.complexVoltage;
import static com.powsybl.cgmes.model.CgmesNamespace.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class StateVariablesExport {

    private static final String SV_VOLTAGE_ANGLE = "SvVoltage.angle";
    private static final String SV_VOLTAGE_V = "SvVoltage.v";
    private static final String SV_VOLTAGE_TOPOLOGICAL_NODE = "SvVoltage.TopologicalNode";

    private static final Logger LOG = LoggerFactory.getLogger(StateVariablesExport.class);

    public static void write(Network network, XMLStreamWriter writer) {
        write(network, writer, new CgmesExportContext(network));
    }

    public static void write(Network network, XMLStreamWriter writer, CgmesExportContext context) {
        try {
            CgmesExportUtil.writeRdfRoot(context.getCimVersion(), writer);
            String cimNamespace = context.getCimNamespace();

            if (context.getCimVersion() == 16) {
                writeSvModelDescription(writer, context);
                writeTopologicalIslands(network, cimNamespace, writer, context);
                // Note: unmapped topological nodes (node breaker) & boundary topological nodes are not written in topological islands
            }

            writeVoltagesForTopologicalNodes(network, cimNamespace, writer, context);
            writeVoltagesForBoundaryNodes(network, cimNamespace, writer, context);
            for (String tn : context.getUnmappedTopologicalNodes()) {
                writeVoltage(tn, 0.0, 0.0, cimNamespace, writer);
            }
            writePowerFlows(network, cimNamespace, writer, context);
            writeShuntCompensatorSections(network, cimNamespace, writer);
            writeTapSteps(network, cimNamespace, writer);
            writeStatus(network, cimNamespace, writer);

            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSvModelDescription(XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writer.writeStartElement(MD_NAMESPACE, "FullModel");
        writer.writeAttribute(RDF_NAMESPACE, "about", "urn:uuid:" + CgmesExportUtil.getUniqueId());
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.SCENARIO_TIME);
        writer.writeCharacters(context.getScenarioTime().toString("yyyy-MM-dd'T'HH:mm:ss"));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.CREATED);
        writer.writeCharacters(DateTime.now().toString());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.DESCRIPTION);
        writer.writeCharacters(context.getSvDescription());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.VERSION);
        writer.writeCharacters(CgmesExportUtil.format(context.getSvVersion()));
        writer.writeEndElement();
        for (String dependency : context.getDependencies()) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.DEPENDENT_ON);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, dependency);
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters(SV_PROFILE);
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.MODELING_AUTHORITY_SET);
        writer.writeCharacters(context.getModelingAuthoritySet());
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeTopologicalIslands(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        Map<String, String> angleRefs = buildAngleRefs(network, context);
        Map<String, List<String>> islands = buildIslands(network, context);
        for (Map.Entry<String, List<String>> island : islands.entrySet()) {
            if (!angleRefs.containsKey(island.getKey())) {
                Supplier<String> log = () -> String.format("Synchronous component  %s does not have a defined slack bus: it is ignored", island.getKey());
                LOG.info(log.get());
                continue;
            }
            String islandId = CgmesExportUtil.getUniqueId();
            writer.writeStartElement(cimNamespace, CgmesNames.TOPOLOGICAL_ISLAND);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, islandId);
            writer.writeStartElement(cimNamespace, CgmesNames.NAME);
            writer.writeCharacters(islandId); // Use id as name
            writer.writeEndElement();
            writer.writeEmptyElement(cimNamespace, "TopologicalIsland.AngleRefTopologicalNode");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + angleRefs.get(island.getKey()));
            for (String tn : island.getValue()) {
                writer.writeEmptyElement(cimNamespace, "TopologicalIsland.TopologicalNodes");
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + tn);
            }
            writer.writeEndElement();
        }
    }

    private static Map<String, String> buildAngleRefs(Network network, CgmesExportContext context) {
        Map<String, String> angleRefs = new HashMap<>();
        for (VoltageLevel vl : network.getVoltageLevels()) {
            SlackTerminal slackTerminal = vl.getExtension(SlackTerminal.class);
            buildAngleRefs(slackTerminal, angleRefs, context);
        }
        return angleRefs;
    }

    private static void buildAngleRefs(SlackTerminal slackTerminal, Map<String, String> angleRefs, CgmesExportContext context) {
        if (slackTerminal != null && slackTerminal.getTerminal() != null) {
            Bus bus = slackTerminal.getTerminal().getBusBreakerView().getBus();
            if (bus != null && bus.getSynchronousComponent() != null) {
                buildAngleRefs(bus.getSynchronousComponent().getNum(), bus.getId(), angleRefs, context);
            } else if (bus != null) {
                buildAngleRefs(bus.getId(), angleRefs, context);
            } else {
                Supplier<String> message = () -> String.format("Slack terminal at equipment %s is not connected and is not exported as slack terminal", slackTerminal.getTerminal().getConnectable().getId());
                LOG.info(message.get());
            }
        }
    }

    private static void buildAngleRefs(int synchronousComponentNum, String busId, Map<String, String> angleRefs, CgmesExportContext context) {
        String componentNum = String.valueOf(synchronousComponentNum);
        if (angleRefs.containsKey(componentNum)) {
            Supplier<String> log = () -> String.format("Several slack buses are defined for synchronous component %s: only first slack bus (%s) is taken into account",
                    componentNum, angleRefs.get(componentNum));
            LOG.info(log.get());
            return;
        }
        Set<String> topologicalNodes = context.getTopologicalNodesByBusBreakerBus(busId);
        if (topologicalNodes == null) {
            return;
        }
        String topologicalNode = topologicalNodes.iterator().next();
        angleRefs.put(componentNum, topologicalNode);
    }

    private static void buildAngleRefs(String busId, Map<String, String> angleRefs, CgmesExportContext context) {
        Set<String> topologicalNodes = context.getTopologicalNodesByBusBreakerBus(busId);
        if (topologicalNodes == null) {
            return;
        }
        String topologicalNode = topologicalNodes.iterator().next();
        angleRefs.put(topologicalNode,
                topologicalNode);
    }

    private static Map<String, List<String>> buildIslands(Network network, CgmesExportContext context) {
        Map<String, List<String>> islands = new HashMap<>();
        for (Bus b : network.getBusBreakerView().getBuses()) {
            if (b.getSynchronousComponent() != null) {
                int num = b.getSynchronousComponent().getNum();
                Set<String> topologicalNodes = context.getTopologicalNodesByBusBreakerBus(b.getId());
                if (topologicalNodes == null) {
                    continue;
                }
                islands.computeIfAbsent(String.valueOf(num), i -> new ArrayList<>());
                islands.get(String.valueOf(num)).addAll(topologicalNodes);
            } else {
                islands.put(b.getId(), Collections.singletonList(b.getId()));
            }
        }
        return islands;
    }

    private static void writeVoltagesForTopologicalNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            Set<String> topologicalNodes = context.getTopologicalNodesByBusBreakerBus(b.getId());
            if (topologicalNodes == null) {
                continue;
            }
            for (String topologicalNode : topologicalNodes) {
                writeVoltage(topologicalNode, b.getV(), b.getAngle(), cimNamespace, writer);
            }
        }
    }

    private static void writeVoltagesForBoundaryNodes(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Bus b = dl.getTerminal().getBusBreakerView().getBus();
            Optional<String> topologicalNode = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.TOPOLOGICAL_NODE);
            if (topologicalNode.isPresent()) {
                context.isMapped(topologicalNode.get());
                if (dl.hasProperty("v") && dl.hasProperty("angle")) {
                    writeVoltage(topologicalNode.get(), Double.valueOf(dl.getProperty("v", "NaN")), Double.valueOf(dl.getProperty("angle", "NaN")), cimNamespace, writer);
                } else if (b != null) {
                    // calculate complex voltage value: abs for VOLTAGE, degrees for ANGLE
                    Complex v2 = complexVoltage(dl.getR(), dl.getX(), dl.getG(), dl.getB(), b.getV(), b.getAngle(), dl.getP0(), dl.getQ0());
                    writeVoltage(topologicalNode.get(), v2.abs(), Math.toDegrees(v2.getArgument()), cimNamespace, writer);
                } else {
                    writeVoltage(topologicalNode.get(), 0.0, 0.0, cimNamespace, writer);
                }
            }
        }
        // Voltages at inner nodes of Tie Lines
        // (boundary nodes that have been left inside CGM)
        for (Line l : network.getLines()) {
            if (!l.isTieLine()) {
                continue;
            }
            TieLine tieLine = (TieLine) l;
            // FIXME(Luma) Obtain voltage at inner node
            LOG.error("Must export topologicalNode voltage for boundary Tie Line {}", tieLine);
        }
    }

    private static void writeVoltage(String topologicalNode, double v, double angle, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SvVoltage");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_ANGLE);
        writer.writeCharacters(CgmesExportUtil.format(angle));
        writer.writeEndElement();
        writer.writeStartElement(cimNamespace, SV_VOLTAGE_V);
        writer.writeCharacters(CgmesExportUtil.format(v));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, SV_VOLTAGE_TOPOLOGICAL_NODE);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + topologicalNode);
        writer.writeEndElement();
    }

    private static void writePowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) {
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getLoadStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getGeneratorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getShuntCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getStaticVarCompensatorStream);
        writeInjectionsPowerFlows(network, cimNamespace, writer, Network::getBatteryStream);

        network.getDanglingLineStream().forEach(dl -> {
            // FIXME: the values (p0/q0) are wrong: these values are target and never updated, not calculated flows
            // DanglingLine's attributes will be created to store calculated flows on the boundary side
            if (context.exportBoundaryPowerFlows()) {
                dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + "Terminal_Boundary")
                        .ifPresent(terminal -> writePowerFlow(terminal, -dl.getP0(), -dl.getQ0(), cimNamespace, writer));
            }
            dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + "EquivalentInjectionTerminal")
                    .ifPresent(eit -> writePowerFlow(eit, dl.getP0(), dl.getQ0(), cimNamespace, writer));
        });

        // TODO: what about branches' power flows?
    }

    private static <I extends Injection<I>> void writeInjectionsPowerFlows(Network network, String cimNamespace, XMLStreamWriter writer, Function<Network, Stream<I>> getInjectionStream) {
        getInjectionStream.apply(network).forEach(i -> writePowerFlow(i.getTerminal(), cimNamespace, writer));
    }

    private static void writePowerFlow(Terminal terminal, String cimNamespace, XMLStreamWriter writer) {
        String cgmesTerminal = ((Connectable<?>) terminal.getConnectable()).getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.TERMINAL1).orElse(null);
        if (cgmesTerminal != null) {
            writePowerFlow(cgmesTerminal, terminal.getP(), terminal.getQ(), cimNamespace, writer);
        } else if (terminal.getConnectable() instanceof Load && terminal.getConnectable().isFictitious()) {
            Load svInjection = (Load) terminal.getConnectable();
            writeSvInjection(svInjection, cimNamespace, writer);
        } else {
            LOG.error("No defined CGMES terminal for {}", terminal.getConnectable().getId());
        }
    }

    private static void writePowerFlow(String terminal, double p, double q, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, "SvPowerFlow");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
            writer.writeStartElement(cimNamespace, "SvPowerFlow.p");
            writer.writeCharacters(CgmesExportUtil.format(p));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvPowerFlow.q");
            writer.writeCharacters(CgmesExportUtil.format(q));
            writer.writeEndElement();
            writer.writeEmptyElement(cimNamespace, "SvPowerFlow.Terminal");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + terminal);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeSvInjection(Load svInjection, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, "SvInjection");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, svInjection.getId());
            writer.writeStartElement(cimNamespace, "SvInjection.pInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getP0()));
            writer.writeEndElement();
            writer.writeStartElement(cimNamespace, "SvInjection.qInjection");
            writer.writeCharacters(CgmesExportUtil.format(svInjection.getQ0()));
            writer.writeEndElement();
            writer.writeEmptyElement(cimNamespace, "SvInjection.TopologicalNode");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + svInjection.getTerminal().getBusBreakerView().getBus().getId());
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private static void writeShuntCompensatorSections(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (ShuntCompensator s : network.getShuntCompensators()) {
            writer.writeStartElement(cimNamespace, "SvShuntCompensatorSections");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
            writer.writeEmptyElement(cimNamespace, "SvShuntCompensatorSections.ShuntCompensator");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + s.getId());
            writer.writeStartElement(cimNamespace, "SvShuntCompensatorSections.sections");
            writer.writeCharacters(CgmesExportUtil.format(s.getSectionCount()));
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void writeTapSteps(Network network, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.hasPhaseTapChanger()) {
                String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(ptcId, twt.getPhaseTapChanger().getTapPosition(), cimNamespace, writer);
            } else if (twt.hasRatioTapChanger()) {
                String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + 1).orElseGet(() -> twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow(PowsyblException::new));
                writeSvTapStep(rtcId, twt.getRatioTapChanger().getTapPosition(), cimNamespace, writer);
            }
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int i = 1;
            for (ThreeWindingsTransformer.Leg leg : Arrays.asList(twt.getLeg1(), twt.getLeg2(), twt.getLeg3())) {
                if (leg.hasPhaseTapChanger()) {
                    String ptcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.PHASE_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(ptcId, leg.getPhaseTapChanger().getTapPosition(), cimNamespace, writer);
                } else if (leg.hasRatioTapChanger()) {
                    String rtcId = twt.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.RATIO_TAP_CHANGER + i).orElseThrow(PowsyblException::new);
                    writeSvTapStep(rtcId, leg.getRatioTapChanger().getTapPosition(), cimNamespace, writer);
                }
                i++;
            }
        }
    }

    private static void writeSvTapStep(String tapChangerId, int tapPosition, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, "SvTapStep");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
        writer.writeStartElement(cimNamespace, "SvTapStep.position");
        writer.writeCharacters(CgmesExportUtil.format(tapPosition));
        writer.writeEndElement();
        writer.writeEmptyElement(cimNamespace, "SvTapStep.TapChanger");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + tapChangerId);
        writer.writeEndElement();
    }

    private static void writeStatus(Network network, String cimNamespace, XMLStreamWriter writer) {
        // create SvStatus, iterate on Connectables, check Terminal status, add
        // to SvStatus
        network.getConnectableStream().forEach(c -> writeConnectableStatus((Connectable<?>) c, cimNamespace, writer));

        // RK: For dangling lines (boundaries), the AC Line Segment is considered in service if and only if it is connected on the network side.
        // If it is disconnected on the boundary side, it might not appear on the SV file.
    }

    private static void writeConnectableStatus(Connectable<?> connectable, String cimNamespace, XMLStreamWriter writer) {
        writeStatus(Boolean.toString(connectable.getTerminals().stream().anyMatch(Terminal::isConnected)), connectable.getId(), cimNamespace, writer);
    }

    private static void writeStatus(String inService, String conductingEquipmentId, String cimNamespace, XMLStreamWriter writer) {
        try {
            writer.writeStartElement(cimNamespace, "SvStatus");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, CgmesExportUtil.getUniqueId());
            writer.writeStartElement(cimNamespace, "SvStatus.inService");
            writer.writeCharacters(inService);
            writer.writeEndElement();
            writer.writeEmptyElement(cimNamespace, "SvStatus.ConductingEquipment");
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + conductingEquipmentId);
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private StateVariablesExport() {
    }
}
