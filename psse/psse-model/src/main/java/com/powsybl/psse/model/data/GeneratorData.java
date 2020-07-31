/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.TableData;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseGenerator;
import com.powsybl.psse.model.PsseGenerator35;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class GeneratorData extends BlockData {

    GeneratorData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    GeneratorData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseGenerator> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.GENERATOR_DATA, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = generatorDataHeaders(this.getPsseVersion());
        context.setGeneratorDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseGenerator35> generator35List = parseRecordsHeader(records, PsseGenerator35.class, headers);
            return new ArrayList<>(generator35List); // TODO improve
        } else { // version_33
            return parseRecordsHeader(records, PsseGenerator.class, headers);
        }
    }

    List<PsseGenerator> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.GENERATOR_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode generatorNode = networkNode.get("generator");
        if (generatorNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(generatorNode);
        List<String> records = nodeRecords(generatorNode);

        context.setGeneratorDataReadFields(headers);
        List<PsseGenerator35> generator35List = parseRecordsHeader(records, PsseGenerator35.class, headers);
        return new ArrayList<>(generator35List); // TODO improve
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.GENERATOR_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getGeneratorDataReadFields();
        String[] quoteFields = BlockData.insideHeaders(generatorDataQuoteFields(this.getPsseVersion()), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {

            List<PsseGenerator35> generator35List = model.getGenerators().stream()
                .map(m -> (PsseGenerator35) m).collect(Collectors.toList()); // TODO improve

            BlockData.<PsseGenerator35>writeBlock(PsseGenerator35.class, generator35List, headers,
                quoteFields, context.getDelimiter().charAt(0), outputStream);

        } else {
            BlockData.<PsseGenerator>writeBlock(PsseGenerator.class, model.getGenerators(), headers,
                quoteFields, context.getDelimiter().charAt(0), outputStream);
        }

        BlockData.writeEndOfBlockAndComment("END OF GENERATOR DATA, BEGIN BRANCH DATA", outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.GENERATOR_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getGeneratorDataReadFields();
        List<PsseGenerator35> generator35List = model.getGenerators().stream()
            .map(m -> (PsseGenerator35) m).collect(Collectors.toList()); // TODO improve

        List<String> stringList = BlockData.<PsseGenerator35>writexBlock(PsseGenerator35.class, generator35List, headers,
            BlockData.insideHeaders(generatorDataQuoteFields(this.getPsseVersion()), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] generatorDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg", "mbase", "zr", "zx", "rt",
                "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};

        } else { // Version 33
            return new String[] {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt",
                "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
        }
    }

    private static String[] generatorDataQuoteFields(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"machid"};

        } else { // Version 33
            return new String[] {"id"};
        }
    }
}
