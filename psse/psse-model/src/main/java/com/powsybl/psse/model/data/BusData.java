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

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseBus;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class BusData extends BlockData {

    BusData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    BusData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseBus> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.BUS_DATA, PsseVersion.VERSION_33);

        String[] headers = busDataHeaders(this.getPsseVersion());
        List<String> records = readRecordBlock(reader);

        context.setBusDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseBus.class, headers);
    }

    List<PsseBus> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.BUS_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode busNode = networkNode.get("bus");
        if (busNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(busNode);
        List<String> records = nodeRecords(busNode);

        context.setBusDataReadFields(headers);
        return parseRecordsHeader(records, PsseBus.class, headers);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.BUS_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getBusDataReadFields();
        BlockData.<PsseBus>writeBlock(PsseBus.class, model.getBuses(), headers,
            BlockData.quoteFieldsInsideHeaders(busDataQuoteFields(), headers), context.getDelimiter().charAt(0),
            outputStream);
        BlockData.writeEndOfBlockAndComment("END OF BUS DATA, BEGIN LOAD DATA", outputStream);
    }

    private static String[] busDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo"};
        } else {
            return new String[] {"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo"};
        }
    }

    private static String[] busDataQuoteFields() {
        return new String[] {"name"};
    }
}
