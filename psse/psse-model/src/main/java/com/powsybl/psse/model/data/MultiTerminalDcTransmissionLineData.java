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
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcBus;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcBusx;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcConverter;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcConverterx;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcLink;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcLinkx;
import com.powsybl.psse.model.PsseMultiTerminalDcTransmissionLine.PsseMultiTerminalDcMain;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class MultiTerminalDcTransmissionLineData extends BlockData {

    MultiTerminalDcTransmissionLineData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    MultiTerminalDcTransmissionLineData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseMultiTerminalDcTransmissionLine> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_33);

        String[] mainHeaders = multiTerminalDcTransmissionLineMainRecordDataHeaders();
        String[] converterHeaders = multiTerminalDcTransmissionLineConverterRecordDataHeaders(this.getPsseVersion(), this.getFileFormat());
        String[] busHeaders = multiTerminalDcTransmissionLineBusRecordDataHeaders(this.getPsseVersion(), this.getFileFormat());
        String[] linkHeaders = multiTerminalDcTransmissionLineLinkRecordDataHeaders(this.getPsseVersion(), this.getFileFormat());

        List<PsseMultiTerminalDcTransmissionLine> multiTerminalDcTransmissionLines = new ArrayList<>();

        List<String> records = readRecordBlock(reader);
        int i = 0;
        while (i < records.size()) {
            String mainRecord = records.get(i++);
            PsseMultiTerminalDcMain main = parseRecordHeader(mainRecord, PsseMultiTerminalDcMain.class, mainHeaders);

            List<String> converterRecords = new ArrayList<>();
            int nConverter = 0;
            while (nConverter < main.getNconv()) {
                converterRecords.add(records.get(i++));
                nConverter++;
            }
            List<String> dcbusRecords = new ArrayList<>();
            int nDcbus = 0;
            while (nDcbus < main.getNdcbs()) {
                dcbusRecords.add(records.get(i++));
                nDcbus++;
            }
            List<String> dclinkRecords = new ArrayList<>();
            int nDclinks = 0;
            while (nDclinks < main.getNdcln()) {
                dclinkRecords.add(records.get(i++));
                nDclinks++;
            }

            PsseMultiTerminalDcTransmissionLine multiTerminalDcTransmissionLine = new PsseMultiTerminalDcTransmissionLine(main);
            multiTerminalDcTransmissionLine.getDcConverters().addAll(parseRecordsHeader(converterRecords, PsseMultiTerminalDcConverter.class, converterHeaders));
            multiTerminalDcTransmissionLine.getDcBuses().addAll(parseRecordsHeader(dcbusRecords, PsseMultiTerminalDcBus.class, busHeaders));
            multiTerminalDcTransmissionLine.getDcLinks().addAll(parseRecordsHeader(dclinkRecords, PsseMultiTerminalDcLink.class, linkHeaders));
            multiTerminalDcTransmissionLines.add(multiTerminalDcTransmissionLine);

            context.setMultiTerminalDcTransmissionLineDataReadFields(readFields(mainRecord, mainHeaders, context.getDelimiter()));
            context.setMultiTerminalDcTransmissionLineDataConverterReadFields(readFields(converterRecords, converterHeaders, context.getDelimiter()));
            context.setMultiTerminalDcTransmissionLineDataBusReadFields(readFields(dcbusRecords, busHeaders, context.getDelimiter()));
            context.setMultiTerminalDcTransmissionLineDataLinkReadFields(readFields(dclinkRecords, linkHeaders, context.getDelimiter()));
        }
        return multiTerminalDcTransmissionLines;
    }

    List<PsseMultiTerminalDcTransmissionLine> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode multiTerminalDcTransmissionLineNode = networkNode.get("ntermdc");
        if (multiTerminalDcTransmissionLineNode == null) {
            return new ArrayList<>();
        }
        String[] headers = nodeFields(multiTerminalDcTransmissionLineNode);
        List<String> records = nodeRecords(multiTerminalDcTransmissionLineNode);

        JsonNode multiTerminalDcTransmissionLineNodeConverter = networkNode.get("ntermdcconv");
        if (multiTerminalDcTransmissionLineNodeConverter == null) {
            throw new PsseException("MultiTerminalDcTransmissionLineData: Unexpected ntermdcconv.");
        }
        String[] converterHeaders = nodeFields(multiTerminalDcTransmissionLineNodeConverter);
        List<String> converterRecords = nodeRecords(multiTerminalDcTransmissionLineNodeConverter);

        JsonNode multiTerminalDcTransmissionLineNodeBus = networkNode.get("ntermdcbus");
        if (multiTerminalDcTransmissionLineNodeBus == null) {
            throw new PsseException("MultiTerminalDcTransmissionLineData: Unexpected ntermdcbus.");
        }
        String[] busHeaders = nodeFields(multiTerminalDcTransmissionLineNodeBus);
        List<String> busRecords = nodeRecords(multiTerminalDcTransmissionLineNodeBus);

        JsonNode multiTerminalDcTransmissionLineNodeLink = networkNode.get("ntermdclink");
        if (multiTerminalDcTransmissionLineNodeLink == null) {
            throw new PsseException("MultiTerminalDcTransmissionLineData: Unexpected ntermdclink.");
        }
        String[] linkHeaders = nodeFields(multiTerminalDcTransmissionLineNodeLink);
        List<String> linkRecords = nodeRecords(multiTerminalDcTransmissionLineNodeLink);

        return mixRecords(headers, records, converterHeaders, converterRecords, busHeaders, busRecords, linkHeaders, linkRecords, context);
    }

    private List<PsseMultiTerminalDcTransmissionLine> mixRecords(String[] headers, List<String> records, String[] converterHeaders, List<String> converterRecords,
        String[] busHeaders, List<String> busRecords, String[] linkHeaders, List<String> linkRecords, PsseContext context) {

        context.setMultiTerminalDcTransmissionLineDataReadFields(headers);
        List<PsseMultiTerminalDcMain> mainList = parseRecordsHeader(records, PsseMultiTerminalDcMain.class, headers);

        context.setMultiTerminalDcTransmissionLineDataConverterReadFields(converterHeaders);
        List<PsseMultiTerminalDcConverterx> converterxList = parseRecordsHeader(converterRecords, PsseMultiTerminalDcConverterx.class, converterHeaders);

        context.setMultiTerminalDcTransmissionLineDataBusReadFields(busHeaders);
        List<PsseMultiTerminalDcBusx> busxList = parseRecordsHeader(busRecords, PsseMultiTerminalDcBusx.class, busHeaders);

        context.setMultiTerminalDcTransmissionLineDataLinkReadFields(linkHeaders);
        List<PsseMultiTerminalDcLinkx> linkxList = parseRecordsHeader(linkRecords, PsseMultiTerminalDcLinkx.class, linkHeaders);

        List<PsseMultiTerminalDcTransmissionLine> multiTerminalDcTransmissionLineList = new ArrayList<>();

        for (PsseMultiTerminalDcMain main : mainList) {
            List<PsseMultiTerminalDcConverter> converterList = converterxList.stream().filter(c -> c.getName().equals(main.getName())).map(PsseMultiTerminalDcConverterx::getConverter).collect(Collectors.toList());
            List<PsseMultiTerminalDcBus> busList = busxList.stream().filter(c -> c.getName().equals(main.getName())).map(PsseMultiTerminalDcBusx::getBus).collect(Collectors.toList());
            List<PsseMultiTerminalDcLink> linkList = linkxList.stream().filter(c -> c.getName().equals(main.getName())).map(PsseMultiTerminalDcLinkx::getLink).collect(Collectors.toList());

            multiTerminalDcTransmissionLineList.add(new PsseMultiTerminalDcTransmissionLine(main, converterList, busList, linkList));
        }

        return multiTerminalDcTransmissionLineList;
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_33);

        String[] mainHeaders = context.getMultiTerminalDcTransmissionLineDataReadFields();
        String[] converterHeaders = context.getMultiTerminalDcTransmissionLineDataConverterReadFields();
        String[] busHeaders = context.getMultiTerminalDcTransmissionLineDataBusReadFields();
        String[] linkHeaders = context.getMultiTerminalDcTransmissionLineDataLinkReadFields();
        String[] quoteFields = multiTerminalDcTransmissionLineDataQuoteFields();

        model.getMultiTerminalDcTransmissionLines().forEach(multiTerminalDcTransmissionLine -> {
            String main = BlockData.<PsseMultiTerminalDcMain>writeBlock(PsseMultiTerminalDcMain.class, multiTerminalDcTransmissionLine.getMain(), mainHeaders,
                BlockData.insideHeaders(quoteFields, mainHeaders), context.getDelimiter().charAt(0));
            List<String> converters = BlockData.<PsseMultiTerminalDcConverter>writeBlock(PsseMultiTerminalDcConverter.class, multiTerminalDcTransmissionLine.getDcConverters(), converterHeaders,
                BlockData.insideHeaders(quoteFields, converterHeaders), context.getDelimiter().charAt(0));
            List<String> buses = BlockData.<PsseMultiTerminalDcBus>writeBlock(PsseMultiTerminalDcBus.class, multiTerminalDcTransmissionLine.getDcBuses(), busHeaders,
                BlockData.insideHeaders(quoteFields, busHeaders), context.getDelimiter().charAt(0));
            List<String> links = BlockData.<PsseMultiTerminalDcLink>writeBlock(PsseMultiTerminalDcLink.class, multiTerminalDcTransmissionLine.getDcLinks(), linkHeaders,
                BlockData.insideHeaders(quoteFields, linkHeaders), context.getDelimiter().charAt(0));

            writeRecords(main, converters, buses, links, outputStream);
        });

        BlockData.writeEndOfBlockAndComment("END OF MULTI-TERMINAL DC DATA, BEGIN MULTI-SECTION LINE DATA", outputStream);
    }

    private static void writeRecords(String main, List<String> converters, List<String> buses, List<String> links, OutputStream outputStream) {
        BlockData.writeString(main, outputStream);
        BlockData.writeListString(converters, outputStream);
        BlockData.writeListString(buses, outputStream);
        BlockData.writeListString(links, outputStream);
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getMultiTerminalDcTransmissionLineDataReadFields();
        List<PsseMultiTerminalDcMain> mainList = model.getMultiTerminalDcTransmissionLines().stream().map(PsseMultiTerminalDcTransmissionLine::getMain).collect(Collectors.toList());

        List<String> stringList = BlockData.<PsseMultiTerminalDcMain>writexBlock(PsseMultiTerminalDcMain.class, mainList, headers,
            BlockData.insideHeaders(multiTerminalDcTransmissionLineDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    TableData writexConverters(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getMultiTerminalDcTransmissionLineDataConverterReadFields();
        List<PsseMultiTerminalDcConverterx> converterList = new ArrayList<>();

        model.getMultiTerminalDcTransmissionLines().forEach(multiTerminalDcTransmissionLine ->
            multiTerminalDcTransmissionLine.getDcConverters().forEach(converter -> converterList.add(
                new PsseMultiTerminalDcConverterx(multiTerminalDcTransmissionLine.getMain().getName(), converter))));

        List<String> stringList = BlockData.<PsseMultiTerminalDcConverterx>writexBlock(PsseMultiTerminalDcConverterx.class, converterList, headers,
            BlockData.insideHeaders(multiTerminalDcTransmissionLineDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    TableData writexBuses(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getMultiTerminalDcTransmissionLineDataBusReadFields();
        List<PsseMultiTerminalDcBusx> busList = new ArrayList<>();

        model.getMultiTerminalDcTransmissionLines().forEach(multiTerminalDcTransmissionLine ->
            multiTerminalDcTransmissionLine.getDcBuses().forEach(bus -> busList.add(
                new PsseMultiTerminalDcBusx(multiTerminalDcTransmissionLine.getMain().getName(), bus))));

        List<String> stringList = BlockData.<PsseMultiTerminalDcBusx>writexBlock(PsseMultiTerminalDcBusx.class, busList, headers,
            BlockData.insideHeaders(multiTerminalDcTransmissionLineDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    TableData writexLinks(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.MULTI_TERMINAL_DC_TRANSMISSION_LINE_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getMultiTerminalDcTransmissionLineDataLinkReadFields();
        List<PsseMultiTerminalDcLinkx> linkList = new ArrayList<>();

        model.getMultiTerminalDcTransmissionLines().forEach(multiTerminalDcTransmissionLine ->
            multiTerminalDcTransmissionLine.getDcLinks().forEach(link -> linkList.add(
                new PsseMultiTerminalDcLinkx(multiTerminalDcTransmissionLine.getMain().getName(), link))));

        List<String> stringList = BlockData.<PsseMultiTerminalDcLinkx>writexBlock(PsseMultiTerminalDcLinkx.class, linkList, headers,
            BlockData.insideHeaders(multiTerminalDcTransmissionLineDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] multiTerminalDcTransmissionLineMainRecordDataHeaders() {
        return new String[] {"name", "nconv", "ndcbs", "ndcln", "mdc", "vconv", "vcmod", "vconvn"};
    }

    private static String[] multiTerminalDcTransmissionLineConverterRecordDataHeaders(PsseVersion version, PsseFileFormat format) {
        if (version == PsseVersion.VERSION_35 && format == PsseFileFormat.FORMAT_RAWX) {
            return new String[] {"name", "ib", "n", "angmx", "angmn", "rc", "xc", "ebas", "tr", "tap", "tpmx", "tpmn", "tstp", "setvl", "dcpf", "marg", "cnvcod"};
        } else {
            return new String[] {"ib", "n", "angmx", "angmn", "rc", "xc", "ebas", "tr", "tap", "tpmx", "tpmn", "tstp", "setvl", "dcpf", "marg", "cnvcod"};
        }
    }

    private static String[] multiTerminalDcTransmissionLineBusRecordDataHeaders(PsseVersion version, PsseFileFormat format) {
        if (version == PsseVersion.VERSION_35 && format == PsseFileFormat.FORMAT_RAWX) {
            return new String[] {"name", "idc", "ib", "area", "zone", "dcname", "idc2", "rgrnd", "owner"};
        } else {
            return new String[] {"idc", "ib", "area", "zone", "dcname", "idc2", "rgrnd", "owner"};
        }
    }

    private static String[] multiTerminalDcTransmissionLineLinkRecordDataHeaders(PsseVersion version, PsseFileFormat format) {
        if (version == PsseVersion.VERSION_35 && format == PsseFileFormat.FORMAT_RAWX) {
            return new String[] {"name", "idc", "jdc", "dcckt", "met", "rdc", "ldc"};
        } else {
            return new String[] {"idc", "jdc", "dcckt", "met", "rdc", "ldc"};
        }
    }

    private static String[] multiTerminalDcTransmissionLineDataQuoteFields() {
        return new String[] {"name", "dcname", "dcckt"};
    }
}
