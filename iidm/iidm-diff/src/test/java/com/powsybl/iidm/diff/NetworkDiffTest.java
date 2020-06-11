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

        network1 = NetworkDiffTestUtils.createNetwork1();
        network2 = NetworkDiffTestUtils.createNetwork2();

        network3 = NetworkDiffTestUtils.createNetwork3();
        network4 = NetworkDiffTestUtils.createNetwork4();
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
    public void testDifferencesToString() {
        NetworkDiff ndiff = new NetworkDiff(config);
        NetworkDiffResults ndifr = ndiff.diff(network1, network2);
        String ndiffJson = NetworkDiff.writeJson(ndifr);
        assertNotNull(ndiffJson);
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
