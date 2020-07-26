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
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseLoad;
import com.powsybl.psse.model.PsseLoad35;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class LoadData extends BlockData {

    LoadData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    LoadData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseLoad> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.LOAD_DATA, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = loadDataHeaders(this.getPsseVersion());
        context.setLoadDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseLoad35> load35List = parseRecordsHeader(records, PsseLoad35.class, headers);
            return new ArrayList<>(load35List); // TODO improve
        } else { // version_33
            return parseRecordsHeader(records, PsseLoad.class, headers);
        }
    }

    List<PsseLoad> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.LOAD_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode loadNode = networkNode.get("load");
        if (loadNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(loadNode);
        List<String> records = nodeRecords(loadNode);

        context.setLoadDataReadFields(headers);
        List<PsseLoad35> load35List = parseRecordsHeader(records, PsseLoad35.class, headers);
        return new ArrayList<>(load35List); // TODO improve
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.LOAD_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getLoadDataReadFields();
        String[] quoteFields = BlockData.quoteFieldsInsideHeaders(loadDataQuoteFields(this.getPsseVersion()), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {

            List<PsseLoad35> load35List = model.getLoads().stream()
                .map(m -> (PsseLoad35) m).collect(Collectors.toList()); // TODO improve

            BlockData.<PsseLoad35>writeBlock(PsseLoad35.class, load35List, headers, quoteFields,
                context.getDelimiter().charAt(0), outputStream);
        } else {
            BlockData.<PsseLoad>writeBlock(PsseLoad.class, model.getLoads(), headers, quoteFields,
                context.getDelimiter().charAt(0), outputStream);
        }

        BlockData.writeEndOfBlockAndComment("END OF LOAD DATA, BEGIN FIXED SHUNT DATA", outputStream);
    }

    private static String[] loadDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt",
                "dgenp", "dgenq", "dgenm", "loadtype"};
        } else { // Version 33
            return new String[] {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt"};
        }
    }

    private static String[] loadDataQuoteFields(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"loadid", "loadtype"};
        } else { // Version 33
            return new String[] {"id"};
        }
    }
}
