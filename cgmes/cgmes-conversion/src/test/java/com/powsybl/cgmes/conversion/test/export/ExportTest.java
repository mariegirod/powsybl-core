/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.update.NetworkChanges;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ExportTest {

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testExportAlternativesBusBranchSmall() throws IOException {
        DifferenceEvaluator knownDiffs =
                DifferenceEvaluators.chain(
                        ExportXmlCompare::ensuringIncreasedModelVersion,
                        ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        exportUsingCgmesModelUsingOnlyNetworkAndCompare(CgmesConformity1Catalog.smallBusBranch().dataSource(), knownDiffs);
    }

    @Test
    public void testExportAlternativesBusBranchMicro() throws IOException {
        DifferenceEvaluator knownDiffs =
                DifferenceEvaluators.chain(
                        ExportXmlCompare::ensuringIncreasedModelVersion,
                        ExportXmlCompare::ignoringMissingTopologicalIslandInControl,
                        ExportXmlCompare::ignoringSynchronousMachinesWithTargetDeadband,
                        ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        exportUsingCgmesModelUsingOnlyNetworkAndCompare(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), knownDiffs);
    }

    @Test
    public void testExportAlternativesBusBranchMicroT4() throws IOException {
        DifferenceEvaluator knownDiffs =
                DifferenceEvaluators.chain(
                        ExportXmlCompare::ensuringIncreasedModelVersion,
                        ExportXmlCompare::ignoringStaticVarCompensatorDiffq,
                        ExportXmlCompare::ignoringMissingTopologicalIslandInControl,
                        ExportXmlCompare::ignoringSynchronousMachinesWithTargetDeadband,
                        ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        exportUsingCgmesModelUsingOnlyNetworkAndCompare(CgmesConformity1Catalog.microGridType4BE().dataSource(), knownDiffs);
    }

    @Ignore("not yet implemented")
    @Test
    public void testExportAlternativesNodeBreakerSmall() throws IOException {
        DifferenceEvaluator knownDiffs = ExportXmlCompare::ignoringJunctionOrBusbarTerminals;
        exportUsingCgmesModelUsingOnlyNetworkAndCompare(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), knownDiffs);
    }

    private void exportUsingCgmesModelUsingOnlyNetworkAndCompare(ReadOnlyDataSource ds, DifferenceEvaluator knownDiffs) throws IOException {
        Properties ip = new Properties();
        ip.setProperty(CgmesImport.STORE_CGMES_MODEL_AS_NETWORK_EXTENSION, "true");
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), ip);
        NetworkChanges.modifyStateVariables(network0);
        network0.setProperty("baseName", ds.getBaseName());

        CgmesExport e = new CgmesExport();

        // Export modified network to new CGMES using two alternatives
        DataSource tmpUsingCgmes = tmpDataSource(fileSystem, "usingCgmes");
        DataSource tmpUsingOnlyNetwork = tmpDataSource(fileSystem, "usingOnlyNetwork");
        Properties ep = new Properties();
        e.export(network0, ep, tmpUsingCgmes);
        ep.setProperty("cgmes.export.usingOnlyNetwork", "true");
        network0.removeExtension(CgmesModelExtension.class);
        e.export(network0, ep, tmpUsingOnlyNetwork);

        // Check resulting SV and SSH of both variants
        ExportXmlCompare.compare(tmpUsingCgmes, tmpUsingOnlyNetwork, "SV", ExportXmlCompare::diffSV, knownDiffs, ds.getBaseName());
        ExportXmlCompare.compare(tmpUsingCgmes, tmpUsingOnlyNetwork, "SSH", ExportXmlCompare::diffSSH, knownDiffs, ds.getBaseName());
    }

    public static DataSource tmpDataSource(FileSystem fileSystem, String name) throws IOException {
        Path exportFolder = fileSystem.getPath(name);
        // XXX (local testing) Path exportFolder = Paths.get("/", "Users", "zamarrenolm", "work", "temp", name);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;
}
