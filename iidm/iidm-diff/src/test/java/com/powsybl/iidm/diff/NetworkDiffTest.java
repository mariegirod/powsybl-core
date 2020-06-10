/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiffTest {
    private FileSystem fileSystem;
    private Path tmpDir;

    private InMemoryPlatformConfig platformConfig;

    private Network network1;
    private Network network2;

    private Network network3;
    private Network network4;

    private DiffConfig config;

    private Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getBusView().getBus("VLGEN_0").setV(24.5).setAngle(2.33);
        network.getBusView().getBus("VLHV1_0").setV(402.14).setAngle(0);
        network.getBusView().getBus("VLHV2_0").setV(389.95).setAngle(-3.5);
        network.getBusView().getBus("VLLOAD_0").setV(147.58).setAngle(9.61);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(302.4).setQ(98.7);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(-300.4).setQ(-137.1);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(302.4).setQ(98.7);
        network.getLine("NHV1_NHV2_2").getTerminal2().setP(-300.4).setQ(-137.1);
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().setP(607).setQ(225.4);
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().setP(-606.3).setQ(-197.4);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal1().setP(600).setQ(274.3);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal2().setP(-600).setQ(-200);
        network.getGenerator("GEN").getTerminal().setP(607).setQ(225.4);
        network.getLoad("LOAD").getTerminal().setP(600).setQ(200);
        return network;
    }

    private Network createNetwork2() {
        Network network = NetworkTest1Factory.create();
        return network;
    }

    private void checkValues(DiffConfig config, double genericThreshold, boolean filterDifferent) {
        assertEquals(config.getGenericTreshold(), genericThreshold, 0.0);
        assertEquals(config.isFilterDifferent(), filterDifferent);
    }

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
        platformConfig = new InMemoryPlatformConfig(fileSystem);

        config = DiffConfig.load(platformConfig);

        network1 = createNetwork();
        network2 = createNetwork();
        network2.getBranches().iterator().next().remove();
        network2.getBusView().getBus("VLHV2_0").setV(350);

        network3 = createNetwork2();
        network4 = createNetwork2();
        network4.getSwitch("voltageLevel1Breaker1").setOpen(true);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testNoDifferences() {
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults ndifr = ndiff.diff(network1, network1);
        assertFalse(ndifr.isDifferent());
    }

    @Test
    public void testDifferences() {
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults ndifr = ndiff.diff(network1, network2);
        assertTrue(ndifr.isDifferent());
        assertNotNull(NetworkDiff.writeJson(ndifr));
    }

    @Test
    public void testDifferencesSpecificVoltageLevel() {
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults noDiffVl = ndiff.diff(network1, network2, "VLGEN");
        assertFalse(noDiffVl.isDifferent());

        NetworkDiffResults diffVl = ndiff.diff(network1, network2, "VLHV2");
        assertTrue(diffVl.isDifferent());
        assertNotNull(NetworkDiff.writeJson(diffVl));
    }

    @Test
    public void testDifferencesChangeThreshold() {
        config.setGenericTreshold(100.0);
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults ndifr = ndiff.diff(network1, network2);
        assertFalse(ndifr.isDifferent());
        assertNotNull(NetworkDiff.writeJson(ndifr));
    }

    @Test
    public void testDifferences2() {
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults ndifr = ndiff.diff(network3, network4);
        assertTrue(ndifr.isDifferent());
        assertNotNull(NetworkDiff.writeJson(ndifr));
    }

    @Test
    public void testDifferencesToFile() {
        Path outFile = tmpDir.resolve("diff-networks.json");
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults ndifr = ndiff.diff(network1, network2);
        NetworkDiff.writeJson(outFile, ndifr);
        assertTrue(Files.exists(outFile));
    }

    @Test
    public void checkCompleteConfig() {
        double genericThreshold = 0.5;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("networks-diff");
        moduleConfig.setStringProperty("generic-threshold", Double.toString(genericThreshold));
        moduleConfig.setStringProperty("filter-diff", Boolean.toString(true));
        DiffConfig config = DiffConfig.load(platformConfig);
        checkValues(config, genericThreshold, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErrorsInConfig() {
        double genericThreshold = -1;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("networks-diff");
        moduleConfig.setStringProperty("generic-threshold", Double.toString(genericThreshold));
        moduleConfig.setStringProperty("filter-diff", Boolean.toString(false));
        DiffConfig config = DiffConfig.load(platformConfig);
        checkValues(config, genericThreshold, true);
    }
}
