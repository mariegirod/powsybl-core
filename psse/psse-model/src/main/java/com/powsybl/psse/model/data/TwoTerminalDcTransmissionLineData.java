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

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.TableData;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseTwoTerminalDcTransmissionLine;
import com.powsybl.psse.model.PsseTwoTerminalDcTransmissionLine35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TwoTerminalDcTransmissionLineData extends BlockData {

    TwoTerminalDcTransmissionLineData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    TwoTerminalDcTransmissionLineData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseTwoTerminalDcTransmissionLine> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_33);

        List<PsseTwoTerminalDcTransmissionLine> twoTerminalDcTransmissionLines = new ArrayList<>();

        List<String> records = readRecordBlock(reader);
        int i = 0;
        while (i < records.size()) {
            String record1 = records.get(i++);
            String record2 = records.get(i++);
            String record3 = records.get(i++);

            String record = String.join(context.getDelimiter(), record1, record2, record3);
            String[] headers = twoTerminalDcTransmissionLineDataHeaders(record1.split(context.getDelimiter()).length,
                record2.split(context.getDelimiter()).length, record3.split(context.getDelimiter()).length, this.getPsseVersion());

            context.setTwoTerminalDcTransmissionLineDataReadFields(readFields(record, headers, context.getDelimiter()));

            if (this.getPsseVersion() == PsseVersion.VERSION_35) {
                twoTerminalDcTransmissionLines.add(parseRecordHeader(record, PsseTwoTerminalDcTransmissionLine35.class, headers));
            } else { // version_33
                twoTerminalDcTransmissionLines.add(parseRecordHeader(record, PsseTwoTerminalDcTransmissionLine.class, headers));
            }
        }
        return twoTerminalDcTransmissionLines;
    }

    List<PsseTwoTerminalDcTransmissionLine> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode twoTerminalDcTransmissionLineNode = networkNode.get("twotermdc");
        if (twoTerminalDcTransmissionLineNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(twoTerminalDcTransmissionLineNode);
        List<String> records = nodeRecords(twoTerminalDcTransmissionLineNode);

        context.setTwoTerminalDcTransmissionLineDataReadFields(headers);
        List<PsseTwoTerminalDcTransmissionLine35> twoTerminalDcTransmissionLine35List = parseRecordsHeader(records, PsseTwoTerminalDcTransmissionLine35.class, headers);
        return new ArrayList<>(twoTerminalDcTransmissionLine35List);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getTwoTerminalDcTransmissionLineDataReadFields();
        String[] quoteFields = twoTerminalDcTransmissionLineDataQuoteFields(this.getPsseVersion());

        String[] headers1 = BlockData.insideHeaders(twoTerminalDcTransmissionLineRecord1DataHeaders(this.getPsseVersion()), headers);
        String[] headers2 = BlockData.insideHeaders(twoTerminalDcTransmissionLineRecord2DataHeaders(this.getPsseVersion()), headers);
        String[] headers3 = BlockData.insideHeaders(twoTerminalDcTransmissionLineRecord3DataHeaders(this.getPsseVersion()), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseTwoTerminalDcTransmissionLine35> twoTerminalDcTransmissionLine35List = model.getTwoTerminalDcTransmissionLines().stream()
                .map(m -> (PsseTwoTerminalDcTransmissionLine35) m).collect(Collectors.toList());

            List<String> r1 = BlockData.<PsseTwoTerminalDcTransmissionLine35>writeBlock(PsseTwoTerminalDcTransmissionLine35.class, twoTerminalDcTransmissionLine35List, headers1,
                BlockData.insideHeaders(quoteFields, headers1), context.getDelimiter().charAt(0));
            List<String> r2 = BlockData.<PsseTwoTerminalDcTransmissionLine35>writeBlock(PsseTwoTerminalDcTransmissionLine35.class, twoTerminalDcTransmissionLine35List, headers2,
                BlockData.insideHeaders(quoteFields, headers2), context.getDelimiter().charAt(0));
            List<String> r3 = BlockData.<PsseTwoTerminalDcTransmissionLine35>writeBlock(PsseTwoTerminalDcTransmissionLine35.class, twoTerminalDcTransmissionLine35List, headers3,
                BlockData.insideHeaders(quoteFields, headers3), context.getDelimiter().charAt(0));
            writeRecords(r1, r2, r3, outputStream);

        } else {
            List<String> r1 = BlockData.<PsseTwoTerminalDcTransmissionLine>writeBlock(PsseTwoTerminalDcTransmissionLine.class, model.getTwoTerminalDcTransmissionLines(), headers1,
                BlockData.insideHeaders(quoteFields, headers1), context.getDelimiter().charAt(0));
            List<String> r2 = BlockData.<PsseTwoTerminalDcTransmissionLine>writeBlock(PsseTwoTerminalDcTransmissionLine.class, model.getTwoTerminalDcTransmissionLines(), headers2,
                BlockData.insideHeaders(quoteFields, headers2), context.getDelimiter().charAt(0));
            List<String> r3 = BlockData.<PsseTwoTerminalDcTransmissionLine>writeBlock(PsseTwoTerminalDcTransmissionLine.class, model.getTwoTerminalDcTransmissionLines(), headers3,
                BlockData.insideHeaders(quoteFields, headers3), context.getDelimiter().charAt(0));
            writeRecords(r1, r2, r3, outputStream);
        }

        BlockData.writeEndOfBlockAndComment("END OF TWO-TERMINAL DC DATA, BEGIN VOLTAGE SOURCE CONVERTER DATA", outputStream);
    }

    private static void writeRecords(List<String> r1, List<String> r2, List<String> r3, OutputStream outputStream) {
        if (r1.size() == r2.size() && r1.size() == r3.size()) {

            List<String> mixList = new ArrayList<>();
            for (int i = 0; i < r1.size(); i++) {
                mixList.add(r1.get(i));
                mixList.add(r2.get(i));
                mixList.add(r3.get(i));
            }
            BlockData.writeListString(mixList, outputStream);
        } else {
            throw new PsseException("Psse: TwoTerminalDcTransmissionLine. TwoTerminalDcTransmissionLine records do not match " +
                String.format("%d %d %d", r1.size(), r2.size(), r3.size()));
        }
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.TWO_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getTwoTerminalDcTransmissionLineDataReadFields();
        List<PsseTwoTerminalDcTransmissionLine35> twoTerminalDcTransmissionLine35List = model.getTwoTerminalDcTransmissionLines().stream()
            .map(m -> (PsseTwoTerminalDcTransmissionLine35) m).collect(Collectors.toList());

        List<String> stringList = BlockData.<PsseTwoTerminalDcTransmissionLine35>writexBlock(PsseTwoTerminalDcTransmissionLine35.class, twoTerminalDcTransmissionLine35List, headers,
            BlockData.insideHeaders(twoTerminalDcTransmissionLineDataQuoteFields(this.getPsseVersion()), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] twoTerminalDcTransmissionLineDataHeaders(int record1Fields, int record2Fields, int record3Fields, PsseVersion version) {
        String[] headers = new String[] {};
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(twoTerminalDcTransmissionLineRecord1DataHeaders(version), 0, record1Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(twoTerminalDcTransmissionLineRecord2DataHeaders(version), 0, record2Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(twoTerminalDcTransmissionLineRecord3DataHeaders(version), 0, record3Fields));
        return headers;
    }

    private static String[] twoTerminalDcTransmissionLineRecord1DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"name", "mdc", "rdc", "setvl", "vschd", "vcmod", "rcomp", "delti", "met", "dcvin", "cccitmx", "cccacc"};

        } else { // Version 33
            return new String[] {"name", "mdc", "rdc", "setvl", "vschd", "vcmod", "rcomp", "delti", "meter", "dcvin", "cccitmx", "cccacc"};
        }
    }

    private static String[] twoTerminalDcTransmissionLineRecord2DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ipr", "nbr", "anmxr", "anmnr", "rcr", "xcr", "ebasr", "trr", "tapr", "tmxr", "tmnr", "stpr", "icr", "ndr", "ifr", "itr", "idr", "xcapr"};

        } else { // Version 33
            return new String[] {"ipr", "nbr", "anmxr", "anmnr", "rcr", "xcr", "ebasr", "trr", "tapr", "tmxr", "tmnr", "stpr", "icr", "ifr", "itr", "idr", "xcapr"};
        }
    }

    private static String[] twoTerminalDcTransmissionLineRecord3DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ipi", "nbi", "anmxi", "anmni", "rci", "xci", "ebasi", "tri", "tapi", "tmxi", "tmni", "stpi", "ici", "ndi", "ifi", "iti", "idi", "xcapi"};

        } else { // Version 33
            return new String[] {"ipi", "nbi", "anmxi", "anmni", "rci", "xci", "ebasi", "tri", "tapi", "tmxi", "tmni", "stpi", "ici", "ifi", "iti", "idi", "xcapi"};
        }
    }

    private static String[] twoTerminalDcTransmissionLineDataQuoteFields(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"name", "met", "idr", "idi"};

        } else { // Version 33
            return new String[] {"name", "meter", "idr", "idi"};
        }
    }
}
