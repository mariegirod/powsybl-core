/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiff {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkDiff.class);

    private final DiffProc<VoltageLevel> voltagediff;

    public NetworkDiff(DiffConfig config) {
        this.voltagediff = new VoltageLevelDiffProc(config);
    }

    static void writeJson(JsonGenerator generator, List<? extends DiffResult> diffResults) {
        Objects.requireNonNull(diffResults);
        try {
            generator.writeStartArray();
            for (DiffResult diffResult : diffResults) {
                diffResult.writeJson(generator);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void writeJson(Writer writer, List<? extends DiffResult> diffResults) {
        JsonUtil.writeJson(writer, generator -> writeJson(generator, diffResults));
    }

    static void writeJson(Path file, List<? extends DiffResult> diffResults) {
        JsonUtil.writeJson(file, generator -> writeJson(generator, diffResults));
    }

    static String toJson(List<? extends DiffResult> diffResults) {
        return JsonUtil.toJson(generator -> writeJson(generator, diffResults));
    }

    public static String writeJson(NetworkDiffResults ndifr) {
        return JsonUtil.toJson(ndifr::writeJson);
    }

    public static void writeJson(Writer writer, NetworkDiffResults ndifr) {
        JsonUtil.writeJson(writer, ndifr::writeJson);
    }

    public static void writeJson(Path file, NetworkDiffResults ndifr) {
        JsonUtil.writeJson(file, ndifr::writeJson);
    }

    public NetworkDiffResults diff(Network network1, Network network2) {
        return diff(network1, network2, null);
    }

    public NetworkDiffResults diff(Network network1, Network network2, String... vls) {
        long start = System.currentTimeMillis();

        Set<String> vlIds;
        if (vls == null) {
            vlIds = network1
                    .getVoltageLevelStream()
                    .map(VoltageLevel::getId)
                    .filter(vlId -> network2.getVoltageLevel(vlId) != null)
                    .collect(Collectors.toSet());

        } else {
            vlIds = Arrays.asList(vls)
                    .stream()
                    .filter(vlid -> network1.getVoltageLevel(vlid) != null && network2.getVoltageLevel(vlid) != null)
                    .collect(Collectors.toSet());
        }
        List<DiffResult> vlDiffs = vlIds
                .stream()
                .sorted()
                .map(vlId -> voltagediff.diff(network1.getVoltageLevel(vlId), network2.getVoltageLevel(vlId)))
                .filter(DiffResult::isDifferent)
                .collect(Collectors.toList());
        NetworkDiffResults ndifr = new NetworkDiffResults(network1.getId(), network2.getId(), vlDiffs);

        LOGGER.debug("diff generated in {} ms", System.currentTimeMillis() - start);
        return ndifr;
    }
}
