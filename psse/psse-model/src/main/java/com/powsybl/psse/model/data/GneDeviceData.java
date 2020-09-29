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

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.TableData;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseGneDevice;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class GneDeviceData extends BlockData {

    GneDeviceData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    GneDeviceData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseGneDevice> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.GNE_DEVICE_DATA, PsseVersion.VERSION_33);

        List<PsseGneDevice> gneDevice = new ArrayList<>();
        String[] headers = new String[] {};

        List<String> records = readRecordBlock(reader);
        int i = 0;
        while (i < records.size()) {
            String record = records.get(i++);
            headers = ArrayUtils.addAll(headers, gneDeviceDataHeadersFirstRecord(this.getPsseVersion()));
            int nreal = getNreal(record, context.getDelimiter());
            int nintg = getNintg(record, context.getDelimiter());
            int nchar = getNchar(record, context.getDelimiter());
            if (nreal > 0) {
                record = String.join(context.getDelimiter(), record, records.get(i++));
                headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(gneDeviceDataHeadersRealRecord(), 0, nreal));
            }
            if (nintg > 0) {
                record = String.join(context.getDelimiter(), record, records.get(i++));
                headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(gneDeviceDataHeadersIntgRecord(), 0, nintg));
            }
            if (nchar > 0) {
                record = String.join(context.getDelimiter(), record, records.get(i++));
                headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(gneDeviceDataHeadersCharRecord(), 0, nchar));
            }

            gneDevice.add(parseRecordHeader(record, PsseGneDevice.class, headers));
        }
        return gneDevice;
    }

    private static int getNreal(String record, String delimiter) {
        return getN(record, delimiter, 6);
    }

    private static int getNintg(String record, String delimiter) {
        return getN(record, delimiter, 7);
    }

    private static int getNchar(String record, String delimiter) {
        return getN(record, delimiter, 8);
    }

    private static int getN(String record, String delimiter, int length) {
        String[] tokens = record.split(delimiter);
        if (tokens.length < length) {
            return 0;
        }
        return Integer.parseInt(tokens[length - 1].trim());
    }

    List<PsseGneDevice> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.GNE_DEVICE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode gneNode = networkNode.get("gne");
        if (gneNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(gneNode);
        List<String> records = nodeRecords(gneNode);

        context.setGneDeviceDataReadFields(headers);
        return parseRecordsHeader(records, PsseGneDevice.class, headers);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.GNE_DEVICE_DATA, PsseVersion.VERSION_33);

        List<String> recordList = new ArrayList<>();
        model.getGneDevice().forEach(record -> {
            String[] headers = gneDeviceDataHeadersFirstRecord(this.getPsseVersion());
            recordList.add(BlockData.<PsseGneDevice>writeBlock(PsseGneDevice.class, record, headers,
                BlockData.insideHeaders(gneDeviceDataQuoteFields(), headers), context.getDelimiter().charAt(0)));
            if (record.getNreal() > 0) {
                headers = ArrayUtils.subarray(gneDeviceDataHeadersRealRecord(), 0, record.getNreal());
                recordList.add(BlockData.<PsseGneDevice>writeBlock(PsseGneDevice.class, record, headers,
                    BlockData.insideHeaders(gneDeviceDataQuoteFields(), headers), context.getDelimiter().charAt(0)));
            }
            if (record.getNintg() > 0) {
                headers = ArrayUtils.subarray(gneDeviceDataHeadersIntgRecord(), 0, record.getNintg());
                recordList.add(BlockData.<PsseGneDevice>writeBlock(PsseGneDevice.class, record, headers,
                    BlockData.insideHeaders(gneDeviceDataQuoteFields(), headers), context.getDelimiter().charAt(0)));
            }
            if (record.getNchar() > 0) {
                headers = ArrayUtils.subarray(gneDeviceDataHeadersCharRecord(), 0, record.getNchar());
                recordList.add(BlockData.<PsseGneDevice>writeBlock(PsseGneDevice.class, record, headers,
                    BlockData.insideHeaders(gneDeviceDataQuoteFields(), headers), context.getDelimiter().charAt(0)));
            }
        });

        BlockData.writeListString(recordList, outputStream);
        BlockData.writeEndOfBlockAndComment("END OF GNE DEVICE DATA, BEGIN INDUCTION MACHINE DATA", outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.GNE_DEVICE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getGneDeviceDataReadFields();
        List<String> stringList = BlockData.<PsseGneDevice>writexBlock(PsseGneDevice.class, model.getGneDevice(), headers,
            BlockData.insideHeaders(gneDeviceDataQuoteFields(), headers), context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] gneDeviceDataHeadersFirstRecord(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"name", "model", "nterm", "bus1", "bus2", "nreal", "nintg", "nchar", "stat", "owner", "nmet"};
        } else {
            return new String[] {"name", "model", "nterm", "bus1", "bus2", "nreal", "nintg", "nchar", "status", "owner", "nmet"};
        }
    }

    private static String[] gneDeviceDataHeadersRealRecord() {
        return new String[] {"real1", "real2", "real3", "real4", "real5", "real6", "real7", "real8", "real9", "real10"};
    }

    private static String[] gneDeviceDataHeadersIntgRecord() {
        return new String[] {"intg1", "intg2", "intg3", "intg4", "intg5", "intg6", "intg7", "intg8", "intg9", "intg10"};
    }

    private static String[] gneDeviceDataHeadersCharRecord() {
        return new String[] {"char1", "char2", "char3", "char4", "char5", "char6", "char7", "char8", "char9", "char10"};
    }

    private static String[] gneDeviceDataQuoteFields() {
        return new String[] {"name", "model", "char1", "char2", "char3", "char4", "char5", "char6", "char7", "char8", "char9", "char10"};
    }
}
