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
import com.powsybl.psse.model.PsseLineGrouping;
import com.powsybl.psse.model.PsseLineGrouping.PsseLineGroupingx;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class MultiSectionLineGroupingData extends BlockData {

    private static final String EMPTY_DUMX = "null";

    MultiSectionLineGroupingData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    MultiSectionLineGroupingData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseLineGrouping> read(BufferedReader reader) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_SECTION_LINE_GROUPING_DATA, PsseVersion.VERSION_33);

        String[] headers = multiSectionLineGroupingDataHeaders();
        List<String> records = readRecordBlock(reader);

        return parseRecordsHeader(records, PsseLineGrouping.class, headers);
    }

    List<PsseLineGrouping> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_SECTION_LINE_GROUPING_DATA, PsseVersion.VERSION_35,
            PsseFileFormat.FORMAT_RAWX);

        JsonNode lineGroupingNode = networkNode.get("msline");
        if (lineGroupingNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(lineGroupingNode);
        List<String> records = nodeRecords(lineGroupingNode);

        context.setMultiSectionLineGroupingDataReadFields(headers);
        List<PsseLineGroupingx> recordListx = parseRecordsHeader(records, PsseLineGroupingx.class, headers);
        return convertToPsseLineGrouping(recordListx);
    }

    private static List<PsseLineGrouping> convertToPsseLineGrouping(List<PsseLineGroupingx> recordListx) {
        List<PsseLineGrouping> recordList = new ArrayList<>();

        recordListx.forEach(recordx -> {
            PsseLineGrouping record = new PsseLineGrouping();
            record.setI(recordx.getIbus());
            record.setJ(recordx.getJbus());
            record.setId(recordx.getMslid());
            record.setMet(recordx.getMet());
            if (!recordx.getDum1().contains(EMPTY_DUMX)) {
                record.setDum1(Integer.parseInt(recordx.getDum1()));
            }
            if (!recordx.getDum2().contains(EMPTY_DUMX)) {
                record.setDum2(Integer.parseInt(recordx.getDum2()));
            }
            if (!recordx.getDum3().contains(EMPTY_DUMX)) {
                record.setDum3(Integer.parseInt(recordx.getDum3()));
            }
            if (!recordx.getDum4().contains(EMPTY_DUMX)) {
                record.setDum4(Integer.parseInt(recordx.getDum4()));
            }
            if (!recordx.getDum5().contains(EMPTY_DUMX)) {
                record.setDum5(Integer.parseInt(recordx.getDum5()));
            }
            if (!recordx.getDum6().contains(EMPTY_DUMX)) {
                record.setDum6(Integer.parseInt(recordx.getDum6()));
            }
            if (!recordx.getDum7().contains(EMPTY_DUMX)) {
                record.setDum7(Integer.parseInt(recordx.getDum7()));
            }
            if (!recordx.getDum8().contains(EMPTY_DUMX)) {
                record.setDum8(Integer.parseInt(recordx.getDum8()));
            }
            if (!recordx.getDum9().contains(EMPTY_DUMX)) {
                record.setDum9(Integer.parseInt(recordx.getDum9()));
            }
            recordList.add(record);
        });

        return recordList;
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_SECTION_LINE_GROUPING_DATA, PsseVersion.VERSION_33);

        String[] headers = multiSectionLineGroupingDataHeaders();

        model.getLineGrouping().forEach(record -> {
            String[] validHeaders = ArrayUtils.subarray(headers, 0, validRecordFields(record));
            BlockData.<PsseLineGrouping>writeBlock(PsseLineGrouping.class, record, validHeaders,
                BlockData.insideHeaders(multiSectionLineGroupingDataQuoteFields(), validHeaders),
                context.getDelimiter().charAt(0),
                outputStream);
        });
        BlockData.writeEndOfBlockAndComment("END OF MULTI-SECTION LINE DATA, BEGIN ZONE DATA", outputStream);
    }

    private static int validRecordFields(PsseLineGrouping record) {
        // I, J, Id, Met always valid
        int valid = 4;
        if (record.getDum1() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum2() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum3() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum4() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum5() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum6() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum7() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum8() == null) {
            return valid;
        }
        valid = valid + 1;
        if (record.getDum9() == null) {
            return valid;
        }
        return valid + 1;
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_SECTION_LINE_GROUPING_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getMultiSectionLineGroupingDataReadFields();
        List<PsseLineGroupingx> recordListx = convertToPsseLineGroupingx(model.getLineGrouping());
        List<String> stringList = BlockData.<PsseLineGroupingx>writexBlock(PsseLineGroupingx.class, recordListx, headers,
            BlockData.insideHeaders(multiSectionLineGroupingDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static List<PsseLineGroupingx> convertToPsseLineGroupingx(List<PsseLineGrouping> recordList) {
        List<PsseLineGroupingx> recordListx = new ArrayList<>();

        recordList.forEach(record -> {
            PsseLineGroupingx recordx = new PsseLineGroupingx();
            recordx.setIbus(record.getI());
            recordx.setJbus(record.getJ());
            recordx.setMslid(record.getId());
            recordx.setMet(record.getMet());
            recordx.setDum1(getDum(record.getDum1()));
            recordx.setDum2(getDum(record.getDum2()));
            recordx.setDum3(getDum(record.getDum3()));
            recordx.setDum4(getDum(record.getDum4()));
            recordx.setDum5(getDum(record.getDum5()));
            recordx.setDum6(getDum(record.getDum6()));
            recordx.setDum7(getDum(record.getDum7()));
            recordx.setDum8(getDum(record.getDum8()));
            recordx.setDum9(getDum(record.getDum9()));
            recordListx.add(recordx);
        });

        return recordListx;
    }

    private static String getDum(Integer dum) {
        if (dum == null) {
            return EMPTY_DUMX;
        } else {
            return String.valueOf(dum);
        }
    }

    private static String[] multiSectionLineGroupingDataHeaders() {
        return new String[] {"i", "j", "id", "met", "dum1", "dum2", "dum3", "dum4", "dum5", "dum6", "dum7", "dum8", "dum9"};
    }

    private static String[] multiSectionLineGroupingDataQuoteFields() {
        return new String[] {"id"};
    }
}
