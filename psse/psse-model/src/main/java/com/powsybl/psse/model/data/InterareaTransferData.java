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
import com.powsybl.psse.model.PsseInterareaTransfer;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class InterareaTransferData extends BlockData {

    InterareaTransferData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    InterareaTransferData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseInterareaTransfer> read(BufferedReader reader) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.INTERAREA_TRANSFER_DATA, PsseVersion.VERSION_33);

        String[] headers = interareaTransferDataHeaders();
        List<String> records = readRecordBlock(reader);

        return parseRecordsHeader(records, PsseInterareaTransfer.class, headers);
    }

    List<PsseInterareaTransfer> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.INTERAREA_TRANSFER_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode interareaTransferNode = networkNode.get("iatransfer");
        if (interareaTransferNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(interareaTransferNode);
        List<String> records = nodeRecords(interareaTransferNode);

        context.setInterareaTransferDataReadFields(headers);
        return parseRecordsHeader(records, PsseInterareaTransfer.class, headers);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.INTERAREA_TRANSFER_DATA, PsseVersion.VERSION_33);

        String[] headers = interareaTransferDataHeaders();
        BlockData.<PsseInterareaTransfer>writeBlock(PsseInterareaTransfer.class, model.getInterareaTransfer(), headers,
            BlockData.insideHeaders(interareaTransferDataQuoteFields(), headers), context.getDelimiter().charAt(0),
            outputStream);
        BlockData.writeEndOfBlockAndComment("END OF INTER-AREA TRANSFER DATA, BEGIN OWNER DATA", outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.INTERAREA_TRANSFER_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getInterareaTransferDataReadFields();
        List<String> stringList = BlockData.<PsseInterareaTransfer>writexBlock(PsseInterareaTransfer.class, model.getInterareaTransfer(), headers,
            BlockData.insideHeaders(interareaTransferDataQuoteFields(), headers), context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] interareaTransferDataHeaders() {
        return new String[] {"arfrom", "arto", "trid", "ptran"};
    }

    private static String[] interareaTransferDataQuoteFields() {
        return new String[] {"trid"};
    }
}
