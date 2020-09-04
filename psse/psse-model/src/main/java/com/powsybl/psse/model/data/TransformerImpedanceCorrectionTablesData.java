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
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TransformerImpedanceCorrectionTablesData extends BlockData {

    TransformerImpedanceCorrectionTablesData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    TransformerImpedanceCorrectionTablesData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseTransformerImpedanceCorrection> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = transformerImpedanceCorrectionTablesDataHeaders(this.getPsseVersion());
        //context.setLoadDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = parseRecordsHeader(records, PsseTransformerImpedanceCorrection35.class, headers);
            return new ArrayList<>(impedanceCorrection35List);
        } else { // version_33
            return parseRecordsHeader(records, PsseTransformerImpedanceCorrection.class, headers);
        }
    }

    List<PsseTransformerImpedanceCorrection> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode impedanceCorrectionNode = networkNode.get("impcor");
        if (impedanceCorrectionNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(impedanceCorrectionNode);
        List<String> records = nodeRecords(impedanceCorrectionNode);

        //context.setLoadDataReadFields(headers);
        List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = parseRecordsHeader(records, PsseTransformerImpedanceCorrection35.class, headers);
        return new ArrayList<>(impedanceCorrection35List);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_33);

        String[] headers = null;
        String[] quoteFields = BlockData.insideHeaders(transformerImpedanceCorrectionTablesDataQuoteFields(this.getPsseVersion()), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {

            List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = model.getTransformerImpedanceCorrections().stream()
                .map(m -> (PsseTransformerImpedanceCorrection35) m).collect(Collectors.toList());

            BlockData.<PsseTransformerImpedanceCorrection35>writeBlock(PsseTransformerImpedanceCorrection35.class, impedanceCorrection35List, headers, quoteFields,
                context.getDelimiter().charAt(0), outputStream);
        } else {
            BlockData.<PsseTransformerImpedanceCorrection>writeBlock(PsseTransformerImpedanceCorrection.class, model.getTransformerImpedanceCorrections(), headers, quoteFields,
                context.getDelimiter().charAt(0), outputStream);
        }

        BlockData.writeEndOfBlockAndComment("END OF IMPEDANCE CORRECTION DATA, BEGIN MULTI-TERMINAL DC DATA", outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getLoadDataReadFields();
        List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = model.getTransformerImpedanceCorrections().stream()
            .map(m -> (PsseTransformerImpedanceCorrection35) m).collect(Collectors.toList());

        List<String> stringList = BlockData.<PsseTransformerImpedanceCorrection35>writexBlock(PsseTransformerImpedanceCorrection35.class, impedanceCorrection35List, headers,
            BlockData.insideHeaders(transformerImpedanceCorrectionTablesDataQuoteFields(this.getPsseVersion()), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] transformerImpedanceCorrectionTablesDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt",
                "dgenp", "dgenq", "dgenm", "loadtype"};
        } else { // Version 33
            return new String[] {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt"};
        }
    }

    private static String[] transformerImpedanceCorrectionTablesDataQuoteFields(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"loadid", "loadtype"};
        } else { // Version 33
            return new String[] {"id"};
        }
    }
}
