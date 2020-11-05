/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.conversion.extensions.CimCharacteristics;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.iidm.network.Network;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContext {

    public enum TopologicalMappingUse {
        MAPPING_ONLY,
        PARTIAL_MAPPING,
        NO_MAPPING
    }

    private int cimVersion = 16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.BUS_BRANCH;

    private DateTime scenarioTime = DateTime.now();

    private String svDescription = "SV Model";
    private int svVersion = 1;
    private final List<String> dependencies = new ArrayList<>();
    private String modelingAuthoritySet = "powsybl.org";

    private boolean exportBoundaryPowerFlows = false;

    private final Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping = new HashMap<>();
    private TopologicalMappingUse topologicalMappingUse = TopologicalMappingUse.NO_MAPPING;

    public CgmesExportContext(Network network) {
        CimCharacteristics cimCharacteristics = network.getExtension(CimCharacteristics.class);
        if (cimCharacteristics != null) {
            cimVersion = cimCharacteristics.getCimVersion();
            topologyKind = cimCharacteristics.getTopologyKind();
        }
        scenarioTime = network.getCaseDate();
        CgmesSvMetadata svMetadata = network.getExtension(CgmesSvMetadata.class);
        if (svMetadata != null) {
            svDescription = svMetadata.getDescription();
            svVersion = svMetadata.getSvVersion() + 1;
            dependencies.addAll(svMetadata.getDependencies());
            modelingAuthoritySet = svMetadata.getModelingAuthoritySet();
        }
    }

    public CgmesExportContext() {
    }

    public int getCimVersion() {
        return cimVersion;
    }

    public CgmesExportContext setCimVersion(int cimVersion) {
        this.cimVersion = cimVersion;
        return this;
    }

    public CgmesTopologyKind getTopologyKind() {
        return topologyKind;
    }

    public CgmesExportContext setTopologyKind(CgmesTopologyKind topologyKind) {
        this.topologyKind = Objects.requireNonNull(topologyKind);
        return this;
    }

    public DateTime getScenarioTime() {
        return scenarioTime;
    }

    public CgmesExportContext setScenarioTime(DateTime scenarioTime) {
        this.scenarioTime = Objects.requireNonNull(scenarioTime);
        return this;
    }

    public String getSvDescription() {
        return svDescription;
    }

    public CgmesExportContext setSvDescription(String svDescription) {
        this.svDescription = Objects.requireNonNull(svDescription);
        return this;
    }

    public int getSvVersion() {
        return svVersion;
    }

    public CgmesExportContext setSvVersion(int svVersion) {
        this.svVersion = svVersion;
        return this;
    }

    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public CgmesExportContext addDependency(String dependency) {
        dependencies.add(Objects.requireNonNull(dependency));
        return this;
    }

    public CgmesExportContext addDependencies(List<String> dependencies) {
        this.dependencies.addAll(Objects.requireNonNull(dependencies));
        return this;
    }

    public CgmesExportContext clearDependencies() {
        this.dependencies.clear();
        return this;
    }

    public String getModelingAuthoritySet() {
        return modelingAuthoritySet;
    }

    public CgmesExportContext setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = Objects.requireNonNull(modelingAuthoritySet);
        return this;
    }

    public boolean exportBoundaryPowerFlows() {
        return exportBoundaryPowerFlows;
    }

    public CgmesExportContext setExportBoundaryPowerFlows(boolean exportBoundaryPowerFlows) {
        this.exportBoundaryPowerFlows = exportBoundaryPowerFlows;
        return this;
    }

    public String getCimNamespace() {
        return CgmesNamespace.getCimNamespace(cimVersion);
    }

    public CgmesExportContext setTopologicalMappingUse(TopologicalMappingUse topologicalMappingUse) {
        this.topologicalMappingUse = Objects.requireNonNull(topologicalMappingUse);
        return this;
    }

    public Set<String> getTopologicalNodesByBusBreakerBus(String busId) {
        if (topologicalMappingUse == TopologicalMappingUse.MAPPING_ONLY) {
            return topologicalNodeByBusBreakerBusMapping.get(busId);
        } else if (topologicalMappingUse == TopologicalMappingUse.NO_MAPPING) {
            return Collections.singleton(busId);
        } else if (topologicalMappingUse == TopologicalMappingUse.PARTIAL_MAPPING) {
            return Optional.ofNullable(topologicalNodeByBusBreakerBusMapping.get(busId)).orElseGet(() -> Collections.singleton(busId));
        }
        throw new AssertionError("Unexpected mapping use: " + topologicalMappingUse);
    }

    public CgmesExportContext setTopologicalNodeByBusBreakerBusMapping(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping) {
        this.topologicalNodeByBusBreakerBusMapping.clear();
        this.topologicalNodeByBusBreakerBusMapping.putAll(topologicalNodeByBusBreakerBusMapping);
        return this;
    }
}
