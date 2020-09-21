/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractCgmesModel implements CgmesModel {

    public AbstractCgmesModel() {
        this.properties = new Properties();
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public Map<String, PropertyBags> groupedTransformerEnds() {
        if (cachedGroupedTransformerEnds == null) {
            cachedGroupedTransformerEnds = computeGroupedTransformerEnds();
        }
        return cachedGroupedTransformerEnds;
    }

    @Override
    public CgmesTerminal terminal(String terminalId) {
        if (cachedTerminals == null) {
            cachedTerminals = computeTerminals();
        }
        return cachedTerminals.get(terminalId);
    }

    @Override
    public CgmesDcTerminal dcTerminal(String dcTerminalId) {
        if (cachedDcTerminals == null) {
            cachedDcTerminals = computeDcTerminals();
        }
        return cachedDcTerminals.get(dcTerminalId);
    }

    @Override
    public List<String> ratioTapChangerListForPowerTransformer(String powerTransformerId) {
        return powerTransformerRatioTapChanger.get(powerTransformerId) == null ? null : Arrays.asList(powerTransformerRatioTapChanger.get(powerTransformerId));
    }

    @Override
    public List<String> phaseTapChangerListForPowerTransformer(String powerTransformerId) {
        return powerTransformerPhaseTapChanger.get(powerTransformerId) == null ? null : Arrays.asList(powerTransformerPhaseTapChanger.get(powerTransformerId));
    }

    @Override
    public String substation(CgmesTerminal t, boolean nodeBreaker) {
        CgmesContainer c = container(t, nodeBreaker);
        if (c == null) {
            return null;
        }
        return c.substation();
    }

    @Override
    public String voltageLevel(CgmesTerminal t, boolean nodeBreaker) {
        CgmesContainer c = container(t, nodeBreaker);
        if (c == null) {
            return null;
        }
        return c.voltageLevel();
    }

    @Override
    public CgmesContainer container(String containerId) {
        if (cachedContainers == null) {
            cachedContainers = computeContainers();
        }
        return cachedContainers.get(containerId);
    }

    @Override
    public double nominalVoltage(String baseVoltageId) {
        if (cachedBaseVoltages == null) {
            cachedBaseVoltages = new HashMap<>();
            baseVoltages()
                .forEach(bv -> cachedBaseVoltages.put(bv.getId("BaseVoltage"), bv.asDouble("nominalVoltage")));
        }
        if (cachedBaseVoltages.containsKey(baseVoltageId)) {
            return cachedBaseVoltages.get(baseVoltageId);
        } else {
            return Double.NaN;
        }
    }

    private CgmesContainer container(CgmesTerminal t, boolean nodeBreaker) {
        if (cachedNodes == null) {
            cachedNodes = computeNodes();
        }
        String containerId = null;
        String nodeId = nodeBreaker && t.connectivityNode() != null ? t.connectivityNode() : t.topologicalNode();
        if (nodeId != null) {
            PropertyBag node = cachedNodes.get(nodeId);
            if (node != null) {
                containerId = node.getId("ConnectivityNodeContainer");
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Missing node {} from terminal {}", nodeId, t.id());
                }
            }
        }

        return (containerId == null) ? null : container(containerId);
    }

    private Map<String, PropertyBags> computeGroupedTransformerEnds() {
        // Alternative implementation:
        // instead of sorting after building each list,
        // use a sorted collection when inserting
        String endNumber = "endNumber";
        Map<String, PropertyBags> gends = new HashMap<>();
        powerTransformerRatioTapChanger = new HashMap<>();
        powerTransformerPhaseTapChanger = new HashMap<>();
        transformerEnds()
            .forEach(end -> {
                String id = end.getId("PowerTransformer");
                PropertyBags ends = gends.computeIfAbsent(id, x -> new PropertyBags());
                ends.add(end);
                if (end.getId("PhaseTapChanger") != null) {
                    powerTransformerPhaseTapChanger.computeIfAbsent(id, s -> new String[3]);
                    powerTransformerPhaseTapChanger.get(id)[end.asInt(endNumber, 1) - 1] = end.getId("PhaseTapChanger");
                } else if (end.getId("RatioTapChanger") != null) {
                    powerTransformerRatioTapChanger.computeIfAbsent(id, s -> new String[3]);
                    powerTransformerRatioTapChanger.get(id)[end.asInt(endNumber, 1) - 1] = end.getId("RatioTapChanger");
                }
            });
        gends.entrySet()
            .forEach(tends -> {
                PropertyBags tends1 = new PropertyBags(
                    tends.getValue().stream()
                        .sorted(Comparator
                            .comparing(WindingType::fromTransformerEnd)
                            .thenComparing(end -> end.asInt(endNumber, -1)))
                        .collect(Collectors.toList()));
                tends.setValue(tends1);
            });
        return gends;
    }

    private Map<String, PropertyBag> computeNodes() {
        Map<String, PropertyBag> nodes = new HashMap<>();
        connectivityNodes().forEach(cn -> nodes.put(cn.getId("ConnectivityNode"), cn));
        topologicalNodes().forEach(tn -> nodes.put(tn.getId("TopologicalNode"), tn));
        return nodes;
    }

    private Map<String, CgmesTerminal> computeTerminals() {
        Map<String, CgmesTerminal> ts = new HashMap<>();
        terminals().forEach(t -> {
            CgmesTerminal td = new CgmesTerminal(t);
            if (ts.containsKey(td.id())) {
                return;
            }
            ts.put(td.id(), td);
        });
        return ts;
    }

    private Map<String, CgmesDcTerminal> computeDcTerminals() {
        Map<String, CgmesDcTerminal> ts = new HashMap<>();
        dcTerminals().forEach(t -> {
            CgmesDcTerminal td = new CgmesDcTerminal(t);
            if (ts.containsKey(td.id())) {
                return;
            }
            ts.put(td.id(), td);
        });
        return ts;
    }

    // TODO(Luma): better caches create an object "Cache" that is final ...
    // (avoid filling all places with if cached == null...)
    private Map<String, CgmesContainer> computeContainers() {
        Map<String, CgmesContainer> cs = new HashMap<>();
        connectivityNodeContainers().forEach(c -> {
            String id = c.getId("ConnectivityNodeContainer");
            String voltageLevel = c.getId("VoltageLevel");
            String substation = c.getId("Substation");
            cs.put(id, new CgmesContainer(voltageLevel, substation));
        });
        return cs;
    }

    // read/write

    @Override
    public void setBasename(String baseName) {
        this.baseName = Objects.requireNonNull(baseName);
    }

    @Override
    public String getBasename() {
        return baseName;
    }

    @Override
    public void read(ReadOnlyDataSource mainDataSource, ReadOnlyDataSource alternativeDataSourceForBoundary) {
        setBasename(CgmesModel.baseName(mainDataSource));
        read(mainDataSource);
        if (!hasBoundary() && alternativeDataSourceForBoundary != null) {
            read(alternativeDataSourceForBoundary);
        }
    }

    @Override
    public void read(ReadOnlyDataSource ds) {
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        for (String name : cds.names()) {
            LOG.info("Reading [{}]", name);
            try (InputStream is = cds.dataSource().newInputStream(name)) {
                read(is, baseName, name);
            } catch (IOException e) {
                String msg = String.format("Reading [%s]", name);
                LOG.warn(msg);
                throw new CgmesModelException(msg, e);
            }
        }
    }

    @Override
    public void copyCachedIidmNodesForTopologicalNodes(Map<Pair<String, Integer>, String> iidmNodesTPNodes) {
        this.cachedIiidmNodesForTopologicalNodes.clear();
        this.cachedIiidmNodesForTopologicalNodes.putAll(Objects.requireNonNull(iidmNodesTPNodes));
    }

    @Override
    public Map<Pair<String, Integer>, String> iidmNodesForTopologicalNodes() {
        return cachedIiidmNodesForTopologicalNodes;
    }

    @Override
    public void cacheIidmNode(String voltageLevelId, int iidmNode, String connectivityNode) {
        // For now we assume the topology is not changed. The topology might change, if
        // switches are opened in IIDM. Then we'd need to create new TNs.
        String topologicalNode = cachedNodes.get(connectivityNode).getId(CgmesNames.TOPOLOGICAL_NODE);
        // We're storing all possible IIDM node IDs for given TN.
        cachedIiidmNodesForTopologicalNodes.computeIfAbsent(Pair.of(voltageLevelId, iidmNode), n -> topologicalNode);

    }

    private final Properties properties;
    private String baseName;
    // Caches
    private Map<String, PropertyBags> cachedGroupedTransformerEnds;
    private Map<String, CgmesTerminal> cachedTerminals;
    private Map<String, CgmesContainer> cachedContainers;
    private Map<String, Double> cachedBaseVoltages;
    private Map<String, PropertyBag> cachedNodes;
    private final Map<Pair<String, Integer>, String> cachedIiidmNodesForTopologicalNodes = new HashMap<>();
    // equipmentId, sequenceNumber, terminalId
    private Map<String, String[]> powerTransformerRatioTapChanger;
    private Map<String, String[]> powerTransformerPhaseTapChanger;
    private Map<String, CgmesDcTerminal> cachedDcTerminals;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCgmesModel.class);
}
