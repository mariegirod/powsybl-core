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
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.TableData;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseOwner;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class OwnerData extends BlockData {

    OwnerData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    OwnerData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseOwner> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.OWNER_DATA, PsseVersion.VERSION_33);

        String[] headers = ownerDataHeaders(this.getPsseVersion());
        List<String> records = readRecordBlock(reader);

        context.setOwnerDataReadFields(readFields(records, headers, context.getDelimiter()));
        return parseRecordsHeader(records, PsseOwner.class, headers);
    }

    List<PsseOwner> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.OWNER_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode ownerNode = networkNode.get("owner");
        if (ownerNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(ownerNode);
        List<String> records = nodeRecords(ownerNode);

        context.setOwnerDataReadFields(headers);
        return parseRecordsHeader(records, PsseOwner.class, headers);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.OWNER_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getOwnerDataReadFields();
        BlockData.<PsseOwner>writeBlock(PsseOwner.class, model.getOwners(), headers,
            BlockData.quoteFieldsInsideHeaders(ownerDataQuoteFields(), headers), context.getDelimiter().charAt(0),
            outputStream);
        BlockData.writeEndOfBlockAndComment("END OF OWNER DATA, BEGIN FACTS CONTROL DEVICE DATA", outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.OWNER_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getOwnerDataReadFields();
        List<String> stringList = BlockData.<PsseOwner>writexBlock(PsseOwner.class, model.getOwners(), headers,
            BlockData.quoteFieldsInsideHeaders(ownerDataQuoteFields(), headers), context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] ownerDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"iowner", "owname"};
        } else {
            return new String[] {"i", "owname"};
        }
    }

    private static String[] ownerDataQuoteFields() {
        return new String[] {"owname"};
    }
}
