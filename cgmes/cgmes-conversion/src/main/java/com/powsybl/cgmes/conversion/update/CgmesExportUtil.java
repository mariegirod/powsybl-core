/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.model.CgmesNamespace.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class CgmesExportUtil {

    private static final String CONNECTIVITY_NODE_1 = Conversion.CGMES_PREFIX_ALIAS + CgmesNames.CONNECTIVITY_NODE + "1";
    private static final String CONNECTIVITY_NODE_2 = Conversion.CGMES_PREFIX_ALIAS + CgmesNames.CONNECTIVITY_NODE + "2";

    private CgmesExportUtil() {
    }

    // Avoid trailing zeros and format always using US locale

    private static final DecimalFormatSymbols DOUBLE_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############", DOUBLE_FORMAT_SYMBOLS);

    public static String format(double value) {
        return DOUBLE_FORMAT.format(Double.isNaN(value) ? 0.0 : value);
    }

    public static String format(int value) {
        return String.valueOf(value);
    }

    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static void writeRdfRoot(int cimVersion, XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix("entsoe", ENTSOE_NAMESPACE);
        writer.setPrefix("rdf", RDF_NAMESPACE);
        writer.setPrefix("cim", getCimNamespace(cimVersion));
        writer.setPrefix("md", MD_NAMESPACE);
        writer.writeStartElement(RDF_NAMESPACE, "RDF");
        writer.writeNamespace("entsoe", ENTSOE_NAMESPACE);
        writer.writeNamespace("rdf", RDF_NAMESPACE);
        writer.writeNamespace("cim", getCimNamespace(cimVersion));
        writer.writeNamespace("md", MD_NAMESPACE);
    }

    public static Complex complexVoltage(double r, double x, double g, double b,
                                         double v, double angle, double p, double q) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
                new Complex(g * 0.5, b * 0.5), new Complex(g * 0.5, b * 0.5));
        Complex v1 = ComplexUtils.polar2Complex(v, Math.toRadians(angle));
        Complex s1 = new Complex(p, q);
        return (s1.conjugate().divide(v1.conjugate()).subtract(adm.y11().multiply(v1))).divide(adm.y12());
    }

    public static Map<String, Set<String>> buildTopologicalNodesByBusBreakerBusMapping(Network network, Path path) {
        return buildTopologicalNodesByBusBreakerBusMapping(network, path, TripleStoreFactory.defaultImplementation());
    }

    public static Map<String, Set<String>> buildTopologicalNodesByBusBreakerBusMapping(Network network, Path path, String tripleStoreImpl) {
        DataSource dataSource = Importers.createDataSource(path);
        return buildTopologicalNodesByBusBreakerBusMapping(network, dataSource, tripleStoreImpl);
    }

    public static Map<String, Set<String>> buildTopologicalNodesByBusBreakerBusMapping(Network network, DataSource dataSource) {
        return buildTopologicalNodesByBusBreakerBusMapping(network, dataSource, TripleStoreFactory.defaultImplementation());
    }

    public static Map<String, Set<String>> buildTopologicalNodesByBusBreakerBusMapping(Network network, DataSource dataSource, String tripleStoreImpl) {
        CgmesModel model = CgmesModelFactory.create(dataSource, null, tripleStoreImpl);
        return buildTopologicalNodesByBusBreakerBusMapping(network, model);
    }

    public static Map<String, Set<String>> buildTopologicalNodesByBusBreakerBusMapping(Network network, CgmesModel model) {
        Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping = new HashMap<>();
        Map<String, PropertyBag> connectivityNodes = model.connectivityNodes().toMap(CgmesNames.CONNECTIVITY_NODE);
        List<String> set = new ArrayList<>();
        network.getConnectableStream(Injection.class)
                .forEach(i -> buildTopologicalNodesByBusBreakerBusMappingFromTerminal(i.getProperty(CONNECTIVITY_NODE_1), i.getTerminal(), topologicalNodeByBusBreakerBusMapping, set, connectivityNodes));
        network.getBranchStream()
                .forEach(b -> {
                    buildTopologicalNodesByBusBreakerBusMappingFromTerminal(b.getProperty(CONNECTIVITY_NODE_1), b.getTerminal1(), topologicalNodeByBusBreakerBusMapping, set, connectivityNodes);
                    buildTopologicalNodesByBusBreakerBusMappingFromTerminal(b.getProperty(CONNECTIVITY_NODE_2), b.getTerminal2(), topologicalNodeByBusBreakerBusMapping, set, connectivityNodes);
                });
        network.getThreeWindingsTransformerStream()
                .forEach(twt -> {
                    buildTopologicalNodesByBusBreakerBusMappingFromTerminal(twt.getProperty(CONNECTIVITY_NODE_1), twt.getTerminal(ThreeWindingsTransformer.Side.ONE), topologicalNodeByBusBreakerBusMapping, set, connectivityNodes);
                    buildTopologicalNodesByBusBreakerBusMappingFromTerminal(twt.getProperty(CONNECTIVITY_NODE_2), twt.getTerminal(ThreeWindingsTransformer.Side.TWO), topologicalNodeByBusBreakerBusMapping, set, connectivityNodes);
                    buildTopologicalNodesByBusBreakerBusMappingFromTerminal(twt.getProperty(Conversion.CGMES_PREFIX_ALIAS + CgmesNames.CONNECTIVITY_NODE + "3"), twt.getTerminal(ThreeWindingsTransformer.Side.THREE), topologicalNodeByBusBreakerBusMapping, set, connectivityNodes);
                });
        network.getSwitchStream()
                .forEach(ss -> {
                    VoltageLevel.BusBreakerView bbv = ss.getVoltageLevel().getBusBreakerView();
                    String connectivityNode1 = ss.getProperty(CONNECTIVITY_NODE_1);
                    if (connectivityNode1 != null && !set.contains(connectivityNode1)) {
                        set.add(connectivityNode1);
                        PropertyBag p = connectivityNodes.get(connectivityNode1);
                        if (p != null) {
                            topologicalNodeByBusBreakerBusMapping.computeIfAbsent(bbv.getBus1(ss.getId()).getId(), s ->  new HashSet<>()).add(p.getId(CgmesNames.TOPOLOGICAL_NODE));
                        }
                    }
                    String connectivityNode2 = ss.getProperty(CONNECTIVITY_NODE_2);
                    if (connectivityNode2 != null && !set.contains(connectivityNode2)) {
                        set.add(connectivityNode2);
                        PropertyBag p = connectivityNodes.get(connectivityNode2);
                        if (p != null) {
                            topologicalNodeByBusBreakerBusMapping.computeIfAbsent(bbv.getBus2(ss.getId()).getId(), s -> new HashSet<>()).add(p.getId(CgmesNames.TOPOLOGICAL_NODE));
                        }
                    }
                });
        return Collections.unmodifiableMap(topologicalNodeByBusBreakerBusMapping);
    }

    private static void buildTopologicalNodesByBusBreakerBusMappingFromTerminal(String cn, Terminal t, Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping, List<String> set, Map<String, PropertyBag> connectivityNodes) {
        if (cn == null || set.contains(cn)) {
            return;
        }
        Bus b = t.getBusBreakerView().getBus();
        if (b == null) {
            return;
        }
        set.add(cn);
        PropertyBag p = connectivityNodes.get(cn);
        if (p != null) {
            topologicalNodeByBusBreakerBusMapping.computeIfAbsent(b.getId(), s -> new HashSet<>()).add(p.getId(CgmesNames.TOPOLOGICAL_NODE));
        }
    }

    public static Set<String> getUnmappedTopologicalNodes(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping, Path path) {
        return getUnmappedTopologicalNodes(topologicalNodeByBusBreakerBusMapping, path, TripleStoreFactory.defaultImplementation());
    }

    public static Set<String> getUnmappedTopologicalNodes(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping, Path path, String tripleStoreImpl) {
        DataSource dataSource = Importers.createDataSource(path);
        return getUnmappedTopologicalNodes(topologicalNodeByBusBreakerBusMapping, dataSource, tripleStoreImpl);
    }

    public static Set<String> getUnmappedTopologicalNodes(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping, DataSource dataSource) {
        return getUnmappedTopologicalNodes(topologicalNodeByBusBreakerBusMapping, dataSource, TripleStoreFactory.defaultImplementation());
    }

    public static Set<String> getUnmappedTopologicalNodes(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping, DataSource dataSource, String tripleStoreImpl) {
        CgmesModel model = CgmesModelFactory.create(dataSource, null, tripleStoreImpl);
        return getUnmappedTopologicalNodes(topologicalNodeByBusBreakerBusMapping, model);
    }

    public static Set<String> getUnmappedTopologicalNodes(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping, CgmesModel model) {
        Set<String> unmappedTopologicalNodes = new HashSet<>();
        Set<String> mapped = topologicalNodeByBusBreakerBusMapping.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        model.topologicalNodes()
                .pluckLocals("TopologicalNode")
                .stream()
                .filter(tn -> !mapped.contains(tn))
                .forEach(unmappedTopologicalNodes::add);
        return unmappedTopologicalNodes;
    }
}
