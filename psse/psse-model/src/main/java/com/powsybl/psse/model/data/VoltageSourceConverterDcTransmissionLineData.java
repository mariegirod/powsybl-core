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
import com.powsybl.psse.model.PsseVoltageSourceConverterDcTransmissionLine;
import com.powsybl.psse.model.PsseVoltageSourceConverterDcTransmissionLine35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageSourceConverterDcTransmissionLineData extends BlockData {

    VoltageSourceConverterDcTransmissionLineData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    VoltageSourceConverterDcTransmissionLineData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseVoltageSourceConverterDcTransmissionLine> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_33);

        List<PsseVoltageSourceConverterDcTransmissionLine> voltageSourceConverterDcTransmissionLines = new ArrayList<>();

        List<String> records = readRecordBlock(reader);
        int i = 0;
        while (i < records.size()) {
            String record1 = records.get(i++);
            String record2 = records.get(i++);
            String record3 = records.get(i++);

            String record = String.join(context.getDelimiter(), record1, record2, record3);
            String[] headers = voltageSourceConverterDcTransmissionLineDataHeaders(record1.split(context.getDelimiter()).length,
                record2.split(context.getDelimiter()).length, record3.split(context.getDelimiter()).length, this.getPsseVersion());

            context.setVoltageSourceConverterDcTransmissionLineDataReadFields(readFields(record, headers, context.getDelimiter()));

            if (this.getPsseVersion() == PsseVersion.VERSION_35) {
                voltageSourceConverterDcTransmissionLines.add(parseRecordHeader(record, PsseVoltageSourceConverterDcTransmissionLine35.class, headers));
            } else { // version_33
                voltageSourceConverterDcTransmissionLines.add(parseRecordHeader(record, PsseVoltageSourceConverterDcTransmissionLine.class, headers));
            }
        }
        return voltageSourceConverterDcTransmissionLines;
    }

    List<PsseVoltageSourceConverterDcTransmissionLine> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode voltageSourceConverterDcTransmissionLineNode = networkNode.get("vscdc");
        if (voltageSourceConverterDcTransmissionLineNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(voltageSourceConverterDcTransmissionLineNode);
        List<String> records = nodeRecords(voltageSourceConverterDcTransmissionLineNode);

        context.setVoltageSourceConverterDcTransmissionLineDataReadFields(headers);
        List<PsseVoltageSourceConverterDcTransmissionLine35> voltageSourceConverterDcTransmissionLine35List = parseRecordsHeader(records, PsseVoltageSourceConverterDcTransmissionLine35.class, headers);
        return new ArrayList<>(voltageSourceConverterDcTransmissionLine35List);
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getVoltageSourceConverterDcTransmissionLineDataReadFields();
        String[] quoteFields = voltageSourceConverterDcTransmissionLineDataQuoteFields();

        String[] headers1 = BlockData.insideHeaders(voltageSourceConverterDcTransmissionLineRecord1DataHeaders(), headers);
        String[] headers2 = BlockData.insideHeaders(voltageSourceConverterDcTransmissionLineRecord2DataHeaders(this.getPsseVersion()), headers);
        String[] headers3 = BlockData.insideHeaders(voltageSourceConverterDcTransmissionLineRecord3DataHeaders(this.getPsseVersion()), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseVoltageSourceConverterDcTransmissionLine35> voltageSourceConverterDcTransmissionLine35List = model.getVoltageSourceConverterDcTransmissionLines().stream()
                .map(m -> (PsseVoltageSourceConverterDcTransmissionLine35) m).collect(Collectors.toList());

            List<String> r1 = BlockData.<PsseVoltageSourceConverterDcTransmissionLine35>writeBlock(PsseVoltageSourceConverterDcTransmissionLine35.class, voltageSourceConverterDcTransmissionLine35List, headers1,
                BlockData.insideHeaders(quoteFields, headers1), context.getDelimiter().charAt(0));
            List<String> r2 = BlockData.<PsseVoltageSourceConverterDcTransmissionLine35>writeBlock(PsseVoltageSourceConverterDcTransmissionLine35.class, voltageSourceConverterDcTransmissionLine35List, headers2,
                BlockData.insideHeaders(quoteFields, headers2), context.getDelimiter().charAt(0));
            List<String> r3 = BlockData.<PsseVoltageSourceConverterDcTransmissionLine35>writeBlock(PsseVoltageSourceConverterDcTransmissionLine35.class, voltageSourceConverterDcTransmissionLine35List, headers3,
                BlockData.insideHeaders(quoteFields, headers3), context.getDelimiter().charAt(0));
            writeRecords(r1, r2, r3, outputStream);

        } else {
            List<String> r1 = BlockData.<PsseVoltageSourceConverterDcTransmissionLine>writeBlock(PsseVoltageSourceConverterDcTransmissionLine.class, model.getVoltageSourceConverterDcTransmissionLines(), headers1,
                BlockData.insideHeaders(quoteFields, headers1), context.getDelimiter().charAt(0));
            List<String> r2 = BlockData.<PsseVoltageSourceConverterDcTransmissionLine>writeBlock(PsseVoltageSourceConverterDcTransmissionLine.class, model.getVoltageSourceConverterDcTransmissionLines(), headers2,
                BlockData.insideHeaders(quoteFields, headers2), context.getDelimiter().charAt(0));
            List<String> r3 = BlockData.<PsseVoltageSourceConverterDcTransmissionLine>writeBlock(PsseVoltageSourceConverterDcTransmissionLine.class, model.getVoltageSourceConverterDcTransmissionLines(), headers3,
                BlockData.insideHeaders(quoteFields, headers3), context.getDelimiter().charAt(0));
            writeRecords(r1, r2, r3, outputStream);
        }

        BlockData.writeEndOfBlockAndComment("END OF VOLTAGE SOURCE CONVERTER DATA, BEGIN IMPEDANCE CORRECTION DATA", outputStream);
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
            throw new PsseException("Psse: VoltageSourceConverterDcTransmissionLine. VoltageSourceConverterDcTransmissionLine records do not match " +
                String.format("%d %d %d", r1.size(), r2.size(), r3.size()));
        }
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.VOLTAGE_SOURCE_CONVERTER_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getVoltageSourceConverterDcTransmissionLineDataReadFields();
        List<PsseVoltageSourceConverterDcTransmissionLine35> voltageSourceConverterDcTransmissionLine35List = model.getVoltageSourceConverterDcTransmissionLines().stream()
            .map(m -> (PsseVoltageSourceConverterDcTransmissionLine35) m).collect(Collectors.toList());

        List<String> stringList = BlockData.<PsseVoltageSourceConverterDcTransmissionLine35>writexBlock(PsseVoltageSourceConverterDcTransmissionLine35.class, voltageSourceConverterDcTransmissionLine35List, headers,
            BlockData.insideHeaders(voltageSourceConverterDcTransmissionLineDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] voltageSourceConverterDcTransmissionLineDataHeaders(int record1Fields, int record2Fields, int record3Fields, PsseVersion version) {
        String[] headers = new String[] {};
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(voltageSourceConverterDcTransmissionLineRecord1DataHeaders(), 0, record1Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(voltageSourceConverterDcTransmissionLineRecord2DataHeaders(version), 0, record2Fields));
        headers = ArrayUtils.addAll(headers, ArrayUtils.subarray(voltageSourceConverterDcTransmissionLineRecord3DataHeaders(version), 0, record3Fields));
        return headers;
    }

    private static String[] voltageSourceConverterDcTransmissionLineRecord1DataHeaders() {
        return new String[] {"name", "mdc", "rdc", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
    }

    private static String[] voltageSourceConverterDcTransmissionLineRecord2DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus1", "type1", "mode1", "dcset1", "acset1", "aloss1", "bloss1", "minloss1", "smax1", "imax1", "pwf1", "maxq1", "minq1", "vsreg1", "nreg1", "rmpct1"};

        } else { // Version 33
            return new String[] {"ibus1", "type1", "mode1", "dcset1", "acset1", "aloss1", "bloss1", "minloss1", "smax1", "imax1", "pwf1", "maxq1", "minq1", "remot1", "rmpct1"};
        }
    }

    private static String[] voltageSourceConverterDcTransmissionLineRecord3DataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus2", "type2", "mode2", "dcset2", "acset2", "aloss2", "bloss2", "minloss2", "smax2", "imax2", "pwf2", "maxq2", "minq2", "vsreg2", "nreg2", "rmpct2"};

        } else { // Version 33
            return new String[] {"ibus2", "type2", "mode2", "dcset2", "acset2", "aloss2", "bloss2", "minloss2", "smax2", "imax2", "pwf2", "maxq2", "minq2", "remot2", "rmpct2"};
        }
    }

    private static String[] voltageSourceConverterDcTransmissionLineDataQuoteFields() {
        return new String[] {"name"};
    }
}
