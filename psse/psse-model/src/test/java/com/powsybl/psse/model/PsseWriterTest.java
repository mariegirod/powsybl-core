/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseWriterTest extends AbstractConverterTest {

    @Test
    public void ieee14BusWriteTest() throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus.raw")))) {
            PsseContext context = new PsseContext();
            PsseRawModel rawData = new PsseRawReader().read(reader, context);
            assertNotNull(rawData);

            new PsseWriter().write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_exported"));

            try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_exported.raw"))) {
                compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_exported.raw"), is);
            }
        }
    }

    @Test
    public void ieee14BusRev35WriteTest() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus_rev35.raw")))) {
            PsseContext context = new PsseContext();
            PsseRawModel rawData = new PsseRawReader().read(reader, context);
            assertNotNull(rawData);

            new PsseWriter().write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_rev35_exported"));
            try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_rev35_exported.raw"))) {
                compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_rev35_exported.raw"), is);
            }
        }
    }

    @Test
    public void minimalExampleRawxWriteTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/MinimalExample.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseContext context = new PsseContext();
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile, context);
        assertNotNull(rawData);

        new PsseWriter().writex(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "MinimalExample_exported"));

        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "MinimalExample_exported.rawx"))) {
            compareTxt(getClass().getResourceAsStream("/" + "MinimalExample_exported.rawx"), is);
        }
    }

    @Test
    public void ieee14BusRev35RawxWriteTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseContext context = new PsseContext();
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile, context);
        assertNotNull(rawData);

        new PsseWriter().writex(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_rev35_exported"));

        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_rev35_exported.rawx"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_rev35_exported.rawx"), is);
        }
    }
}
