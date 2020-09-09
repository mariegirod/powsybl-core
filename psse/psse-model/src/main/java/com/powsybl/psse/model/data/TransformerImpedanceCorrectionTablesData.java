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
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.psse.model.PsseConstants.PsseFileFormat;
import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.data.JsonModel.TableData;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection.PsseTransformerImpedanceCorrectionPoint;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection.PsseTransformerImpedanceCorrectionRecord;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection35;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection35.PsseTransformerImpedanceCorrection35Point;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection35.PsseTransformerImpedanceCorrection35Record1;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection35.PsseTransformerImpedanceCorrection35Record2;
import com.powsybl.psse.model.PsseTransformerImpedanceCorrection35.PsseTransformerImpedanceCorrection35Recordx;

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

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = read35(records);
            return new ArrayList<>(impedanceCorrection35List);
        } else { // version_33
            String[] headers = transformerImpedanceCorrectionTablesDataHeaders();
            List<PsseTransformerImpedanceCorrectionRecord> recordImpedanceCorrectionList = parseRecordsHeader(records, PsseTransformerImpedanceCorrectionRecord.class, headers);
            return convertToImpedanceCorrectionList(recordImpedanceCorrectionList);
        }
    }

    private static List<PsseTransformerImpedanceCorrection> convertToImpedanceCorrectionList(
        List<PsseTransformerImpedanceCorrectionRecord> recordImpedanceCorrectionList) {

        List<PsseTransformerImpedanceCorrection> impedanceCorrectionList = new ArrayList<>();
        recordImpedanceCorrectionList.forEach(record -> impedanceCorrectionList.add(convertToImpedanceCorrection(record)));

        return impedanceCorrectionList;
    }

    private static PsseTransformerImpedanceCorrection convertToImpedanceCorrection(
        PsseTransformerImpedanceCorrectionRecord record) {

        PsseTransformerImpedanceCorrection impedanceCorrection = new PsseTransformerImpedanceCorrection(record.getI());
        if (validPoint(record.getT1(), record.getF1())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT1(), record.getF1()));
        }
        if (validPoint(record.getT2(), record.getF2())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT2(), record.getF2()));
        }
        if (validPoint(record.getT3(), record.getF3())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT3(), record.getF3()));
        }
        if (validPoint(record.getT4(), record.getF4())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT4(), record.getF4()));
        }
        if (validPoint(record.getT5(), record.getF5())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT5(), record.getF5()));
        }
        if (validPoint(record.getT6(), record.getF6())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT6(), record.getF6()));
        }
        if (validPoint(record.getT7(), record.getF7())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT7(), record.getF7()));
        }
        if (validPoint(record.getT8(), record.getF8())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT8(), record.getF8()));
        }
        if (validPoint(record.getT9(), record.getF9())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT9(), record.getF9()));
        }
        if (validPoint(record.getT10(), record.getF10())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT10(), record.getF10()));
        }
        if (validPoint(record.getT11(), record.getF11())) {
            impedanceCorrection.getPoints().add(new PsseTransformerImpedanceCorrectionPoint(record.getT11(), record.getF11()));
        }

        return impedanceCorrection;
    }

    private static boolean validPoint(double t, double f) {
        return t != 0.0 && f != 0.0;
    }

    List<PsseTransformerImpedanceCorrection35> read35(List<String> records) {
        List<PsseTransformerImpedanceCorrection35> impedanceCorrectionList35 = new ArrayList<>();

        String[] headersRecord1 = transformerImpedanceCorrectionTablesDataHeaders35Record1();
        String[] headersRecord2 = transformerImpedanceCorrectionTablesDataHeaders35Record2();

        int i = 0;
        while (i < records.size()) {
            PsseTransformerImpedanceCorrection35Record1 r1 = parseRecordHeader(records.get(i++), PsseTransformerImpedanceCorrection35Record1.class, headersRecord1);
            PsseTransformerImpedanceCorrection35 impedanceCorrection35 = new PsseTransformerImpedanceCorrection35(r1.getI());

            boolean endPoints = addImpedanceCorrectionPoints35(impedanceCorrection35, r1.getRecord2());

            while (i < records.size() && !endPoints) {
                PsseTransformerImpedanceCorrection35Record2 r2 = parseRecordHeader(records.get(i++), PsseTransformerImpedanceCorrection35Record2.class, headersRecord2);
                endPoints = addImpedanceCorrectionPoints35(impedanceCorrection35, r2);
            }
            if (!impedanceCorrection35.getPoints35().isEmpty()) {
                impedanceCorrectionList35.add(impedanceCorrection35);
            }
        }

        return impedanceCorrectionList35;
    }

    private static boolean addImpedanceCorrectionPoints35(PsseTransformerImpedanceCorrection35 impedanceCorrection35,
        PsseTransformerImpedanceCorrection35Record2 record2) {
        Objects.requireNonNull(record2);

        int maxPoints = 6;
        if (validPoint(record2.getT1(), record2.getRef1(), record2.getImf1())) {
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(record2.getT1(), record2.getRef1(), record2.getImf1()));
            maxPoints--;
        }
        if (validPoint(record2.getT2(), record2.getRef2(), record2.getImf2())) {
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(record2.getT2(), record2.getRef2(), record2.getImf2()));
            maxPoints--;
        }
        if (validPoint(record2.getT3(), record2.getRef3(), record2.getImf3())) {
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(record2.getT3(), record2.getRef3(), record2.getImf3()));
            maxPoints--;
        }
        if (validPoint(record2.getT4(), record2.getRef4(), record2.getImf4())) {
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(record2.getT4(), record2.getRef4(), record2.getImf4()));
            maxPoints--;
        }
        if (validPoint(record2.getT5(), record2.getRef5(), record2.getImf5())) {
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(record2.getT5(), record2.getRef5(), record2.getImf5()));
            maxPoints--;
        }
        if (validPoint(record2.getT6(), record2.getRef6(), record2.getImf6())) {
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(record2.getT6(), record2.getRef6(), record2.getImf6()));
            maxPoints--;
        }

        return maxPoints > 0;
    }

    private static boolean validPoint(double t, double ref, double imf) {
        return t != 0.0 && ref != 0.0 && imf != 0.0;
    }

    List<PsseTransformerImpedanceCorrection> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode impedanceCorrectionNode = networkNode.get("impcor");
        if (impedanceCorrectionNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(impedanceCorrectionNode);
        List<String> records = nodeRecords(impedanceCorrectionNode);

        List<PsseTransformerImpedanceCorrection35Recordx> recordxImpedanceCorrection35List = parseRecordsHeader(records, PsseTransformerImpedanceCorrection35Recordx.class, headers);
        List<PsseTransformerImpedanceCorrection35> impedanceCorrectionList35 = convertToImpedanceCorrection35List(recordxImpedanceCorrection35List);
        return new ArrayList<>(impedanceCorrectionList35);
    }

    private static List<PsseTransformerImpedanceCorrection35> convertToImpedanceCorrection35List(
        List<PsseTransformerImpedanceCorrection35Recordx> recordxs) {
        List<PsseTransformerImpedanceCorrection35> impedanceCorrectionList35 = new ArrayList<>();
        recordxs.forEach(record -> addToImpedanceCorrection35(impedanceCorrectionList35, record));

        return impedanceCorrectionList35;
    }

    private static void addToImpedanceCorrection35(List<PsseTransformerImpedanceCorrection35> impedanceCorrectionList35,
        PsseTransformerImpedanceCorrection35Recordx recordx) {
        if (impedanceCorrectionList35.isEmpty()) {
            PsseTransformerImpedanceCorrection35 impedanceCorrection35 = new PsseTransformerImpedanceCorrection35(recordx.getItable());
            impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(recordx.getTap(), recordx.getRefact(), recordx.getImfact()));
            impedanceCorrectionList35.add(impedanceCorrection35);
        } else {
            PsseTransformerImpedanceCorrection35 lastImpedanceCorrection35 = impedanceCorrectionList35.get(impedanceCorrectionList35.size() - 1);
            if (lastImpedanceCorrection35.getI() == recordx.getItable()) {
                lastImpedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(recordx.getTap(), recordx.getRefact(), recordx.getImfact()));
            } else {
                PsseTransformerImpedanceCorrection35 impedanceCorrection35 = new PsseTransformerImpedanceCorrection35(recordx.getItable());
                impedanceCorrection35.getPoints35().add(new PsseTransformerImpedanceCorrection35Point(recordx.getTap(), recordx.getRefact(), recordx.getImfact()));
                impedanceCorrectionList35.add(impedanceCorrection35);
            }
        }
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_33);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {

            List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = model
                .getTransformerImpedanceCorrections().stream()
                .map(m -> (PsseTransformerImpedanceCorrection35) m).collect(Collectors.toList());

            writeBlocks35(impedanceCorrection35List, context, outputStream);

        } else {
            String[] headers = transformerImpedanceCorrectionTablesDataHeaders();

            model.getTransformerImpedanceCorrections().forEach(impedanceCorrectionTable -> {
                PsseTransformerImpedanceCorrectionRecord recordImpedanceCorrection = convertToRecordImpedanceCorrection(impedanceCorrectionTable);
                String[] writeHeaders = ArrayUtils.subarray(headers, 0, 1 + 2 * impedanceCorrectionTable.getPoints().size());
                String[] quoteFields = BlockData.insideHeaders(transformerImpedanceCorrectionTablesDataQuoteFields(), writeHeaders);

                BlockData.<PsseTransformerImpedanceCorrectionRecord>writeBlock(PsseTransformerImpedanceCorrectionRecord.class, recordImpedanceCorrection, writeHeaders,
                    quoteFields, context.getDelimiter().charAt(0), outputStream);
            });
        }

        BlockData.writeEndOfBlockAndComment("END OF IMPEDANCE CORRECTION DATA, BEGIN MULTI-TERMINAL DC DATA", outputStream);
    }

    private static PsseTransformerImpedanceCorrectionRecord convertToRecordImpedanceCorrection(
        PsseTransformerImpedanceCorrection impedanceCorrectionTable) {

        PsseTransformerImpedanceCorrectionRecord record = new PsseTransformerImpedanceCorrectionRecord();
        record.setI(impedanceCorrectionTable.getI());

        int numPoints = impedanceCorrectionTable.getPoints().size();
        if (numPoints >= 1) {
            record.setTF1(impedanceCorrectionTable.getPoints().get(0).getT(), impedanceCorrectionTable.getPoints().get(0).getF());
        }
        if (numPoints >= 2) {
            record.setTF2(impedanceCorrectionTable.getPoints().get(1).getT(), impedanceCorrectionTable.getPoints().get(1).getF());
        }
        if (numPoints >= 3) {
            record.setTF3(impedanceCorrectionTable.getPoints().get(2).getT(), impedanceCorrectionTable.getPoints().get(2).getF());
        }
        if (numPoints >= 4) {
            record.setTF4(impedanceCorrectionTable.getPoints().get(3).getT(), impedanceCorrectionTable.getPoints().get(3).getF());
        }
        if (numPoints >= 5) {
            record.setTF5(impedanceCorrectionTable.getPoints().get(4).getT(), impedanceCorrectionTable.getPoints().get(4).getF());
        }
        if (numPoints >= 6) {
            record.setTF6(impedanceCorrectionTable.getPoints().get(5).getT(), impedanceCorrectionTable.getPoints().get(5).getF());
        }
        if (numPoints >= 7) {
            record.setTF7(impedanceCorrectionTable.getPoints().get(6).getT(), impedanceCorrectionTable.getPoints().get(6).getF());
        }
        if (numPoints >= 8) {
            record.setTF8(impedanceCorrectionTable.getPoints().get(7).getT(), impedanceCorrectionTable.getPoints().get(7).getF());
        }
        if (numPoints >= 9) {
            record.setTF9(impedanceCorrectionTable.getPoints().get(8).getT(), impedanceCorrectionTable.getPoints().get(8).getF());
        }
        if (numPoints >= 10) {
            record.setTF10(impedanceCorrectionTable.getPoints().get(9).getT(), impedanceCorrectionTable.getPoints().get(9).getF());
        }
        if (numPoints >= 11) {
            record.setTF11(impedanceCorrectionTable.getPoints().get(10).getT(), impedanceCorrectionTable.getPoints().get(10).getF());
        }

        return record;
    }

    private static void writeBlocks35(List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List,
        PsseContext context, OutputStream outputStream) {

        String[] headersRecord1 = transformerImpedanceCorrectionTablesDataHeaders35Record1();
        String[] headersRecord2 = transformerImpedanceCorrectionTablesDataHeaders35Record2();

        impedanceCorrection35List.forEach(impedanceCorrectionTable35 -> {

            int numPoints = impedanceCorrectionTable35.getPoints35().size();
            int indexPoints = 0;
            PsseTransformerImpedanceCorrection35Record1 record1ImpedanceCorrection35 = convertToRecord1ImpedanceCorrection35(impedanceCorrectionTable35, indexPoints, numPoints);
            String[] writeHeaders = ArrayUtils.subarray(headersRecord1, 0, 1 + 3 * recordPoints35(numPoints));
            String[] quoteFields = BlockData.insideHeaders(transformerImpedanceCorrectionTablesDataQuoteFields(), writeHeaders);

            BlockData.<PsseTransformerImpedanceCorrection35Record1>writeBlock(PsseTransformerImpedanceCorrection35Record1.class, record1ImpedanceCorrection35,
                writeHeaders, quoteFields, context.getDelimiter().charAt(0), outputStream);

            numPoints = numPoints - 6;
            indexPoints = indexPoints + 6;

            while (numPoints >= 0) {
                PsseTransformerImpedanceCorrection35Record2 record2ImpedanceCorrection35 = convertToRecord2ImpedanceCorrection35(impedanceCorrectionTable35, indexPoints, numPoints);
                writeHeaders = ArrayUtils.subarray(headersRecord2, 0, 3 * recordPoints35(numPoints));
                quoteFields = BlockData.insideHeaders(transformerImpedanceCorrectionTablesDataQuoteFields(), writeHeaders);

                BlockData.<PsseTransformerImpedanceCorrection35Record2>writeBlock(PsseTransformerImpedanceCorrection35Record2.class, record2ImpedanceCorrection35,
                    writeHeaders, quoteFields, context.getDelimiter().charAt(0), outputStream);

                numPoints = numPoints - 6;
                indexPoints = indexPoints + 6;
            }
        });
    }

    private static int recordPoints35(int numPoints) {
        if (numPoints >= 6) {
            return 6;
        } else {
            return numPoints + 1;
        }
    }

    private static PsseTransformerImpedanceCorrection35Record1 convertToRecord1ImpedanceCorrection35(
        PsseTransformerImpedanceCorrection35 impedanceCorrectionTable35, int indexPoints, int numPoints) {

        PsseTransformerImpedanceCorrection35Record1 record1 = new PsseTransformerImpedanceCorrection35Record1();
        PsseTransformerImpedanceCorrection35Record2 record2 = new PsseTransformerImpedanceCorrection35Record2();
        record1.setI(impedanceCorrectionTable35.getI());
        record1.setRecord2(record2);
        fillPoints35(impedanceCorrectionTable35, indexPoints, numPoints, record1.getRecord2());
        return record1;
    }

    private static PsseTransformerImpedanceCorrection35Record2 convertToRecord2ImpedanceCorrection35(
        PsseTransformerImpedanceCorrection35 impedanceCorrectionTable35, int indexPoints, int numPoints) {

        PsseTransformerImpedanceCorrection35Record2 record2 = new PsseTransformerImpedanceCorrection35Record2();
        fillPoints35(impedanceCorrectionTable35, indexPoints, numPoints, record2);
        return record2;
    }

    private static void fillPoints35(PsseTransformerImpedanceCorrection35 impedanceCorrectionTable35, int indexPoints,
        int numPoints, PsseTransformerImpedanceCorrection35Record2 record2) {

        if (numPoints >= 1) {
            PsseTransformerImpedanceCorrection35Point point35 = impedanceCorrectionTable35.getPoints35().get(indexPoints);
            Objects.requireNonNull(point35);
            record2.setTF1(point35.getT(), point35.getRef(), point35.getImf());
        }
        if (numPoints >= 2) {
            PsseTransformerImpedanceCorrection35Point point35 = impedanceCorrectionTable35.getPoints35().get(indexPoints + 1);
            record2.setTF2(point35.getT(), point35.getRef(), point35.getImf());
        }
        if (numPoints >= 3) {
            PsseTransformerImpedanceCorrection35Point point35 = impedanceCorrectionTable35.getPoints35().get(indexPoints + 2);
            record2.setTF3(point35.getT(), point35.getRef(), point35.getImf());
        }
        if (numPoints >= 4) {
            PsseTransformerImpedanceCorrection35Point point35 = impedanceCorrectionTable35.getPoints35().get(indexPoints + 3);
            record2.setTF4(point35.getT(), point35.getRef(), point35.getImf());
        }
        if (numPoints >= 5) {
            PsseTransformerImpedanceCorrection35Point point35 = impedanceCorrectionTable35.getPoints35().get(indexPoints + 4);
            record2.setTF5(point35.getT(), point35.getRef(), point35.getImf());
        }
        if (numPoints >= 6) {
            PsseTransformerImpedanceCorrection35Point point35 = impedanceCorrectionTable35.getPoints35().get(indexPoints + 5);
            record2.setTF6(point35.getT(), point35.getRef(), point35.getImf());
        }

        // End point

        if (numPoints == 0) {
            record2.setTF1(0.0, 0.0, 0.0);
        }
        if (numPoints == 1) {
            record2.setTF2(0.0, 0.0, 0.0);
        }
        if (numPoints == 2) {
            record2.setTF3(0.0, 0.0, 0.0);
        }
        if (numPoints == 3) {
            record2.setTF4(0.0, 0.0, 0.0);
        }
        if (numPoints == 4) {
            record2.setTF5(0.0, 0.0, 0.0);
        }
        if (numPoints == 5) {
            record2.setTF6(0.0, 0.0, 0.0);
        }
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.TRANSFORMER_IMPEDANCE_CORRECTION_TABLES, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getLoadDataReadFields();
        List<PsseTransformerImpedanceCorrection35> impedanceCorrection35List = model.getTransformerImpedanceCorrections().stream()
            .map(m -> (PsseTransformerImpedanceCorrection35) m).collect(Collectors.toList());

        List<String> stringList = BlockData.<PsseTransformerImpedanceCorrection35>writexBlock(PsseTransformerImpedanceCorrection35.class, impedanceCorrection35List, headers,
            BlockData.insideHeaders(transformerImpedanceCorrectionTablesDataQuoteFields(), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] transformerImpedanceCorrectionTablesDataHeaders35Record1() {
        return new String[] {"i", "t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3",
            "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"};
    }

    private static String[] transformerImpedanceCorrectionTablesDataHeaders35Record2() {
        return new String[] {"t1", "ref1", "imf1", "t2", "ref2", "imf2", "t3", "ref3", "imf3",
            "t4", "ref4", "imf4", "t5", "ref5", "imf5", "t6", "ref6", "imf6"};
    }

    private static String[] transformerImpedanceCorrectionTablesDataHeaders() {
        return new String[] {"i", "t1", "f1", "t2", "f2", "t3", "f3", "t4", "f4", "t5", "f5", "t6", "f6", "t7",
            "f7", "t8", "f8", "t9", "f9", "t10", "f10", "t11", "f11"};
    }

    private static String[] transformerImpedanceCorrectionTablesDataQuoteFields() {
        return new String[] {};
    }
}
