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
import com.powsybl.psse.model.PsseNonTransformerBranch;
import com.powsybl.psse.model.PsseNonTransformerBranch35;
import com.powsybl.psse.model.PsseRawModel;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class NonTransformerBranchData extends BlockData {

    NonTransformerBranchData(PsseVersion psseVersion) {
        super(psseVersion);
    }

    NonTransformerBranchData(PsseVersion psseVersion, PsseFileFormat psseFileFormat) {
        super(psseVersion, psseFileFormat);
    }

    List<PsseNonTransformerBranch> read(BufferedReader reader, PsseContext context) throws IOException {
        assertMinimumExpectedVersion(PsseBlockData.NON_TRANSFORMER_BRANCH_DATA, PsseVersion.VERSION_33);

        List<String> records = readRecordBlock(reader);
        String[] headers = nonTransformerBranchDataHeaders(this.getPsseVersion());
        context.setNonTransformerBranchDataReadFields(readFields(records, headers, context.getDelimiter()));

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {
            List<PsseNonTransformerBranch35> nonTransformerBranch35List = parseRecordsHeader(records, PsseNonTransformerBranch35.class, headers);
            return new ArrayList<>(nonTransformerBranch35List); // TODO improve
        } else { // version_33
            return parseRecordsHeader(records, PsseNonTransformerBranch.class, headers);
        }
    }

    List<PsseNonTransformerBranch> readx(JsonNode networkNode, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.NON_TRANSFORMER_BRANCH_DATA, PsseVersion.VERSION_35, PsseFileFormat.FORMAT_RAWX);

        JsonNode nonTransformerBranchNode = networkNode.get("acline");
        if (nonTransformerBranchNode == null) {
            return new ArrayList<>();
        }

        String[] headers = nodeFields(nonTransformerBranchNode);
        List<String> records = nodeRecords(nonTransformerBranchNode);

        context.setNonTransformerBranchDataReadFields(headers);
        List<PsseNonTransformerBranch35> nonTransformerBranch35List = parseRecordsHeader(records, PsseNonTransformerBranch35.class, headers);
        return new ArrayList<>(nonTransformerBranch35List); // TODO improve
    }

    void write(PsseRawModel model, PsseContext context, OutputStream outputStream) {
        assertMinimumExpectedVersion(PsseBlockData.NON_TRANSFORMER_BRANCH_DATA, PsseVersion.VERSION_33);

        String[] headers = context.getNonTransformerBranchDataReadFields();
        String[] quoteFields = BlockData.insideHeaders(nonTransformerBranchDataQuoteFields(this.getPsseVersion()), headers);

        if (this.getPsseVersion() == PsseVersion.VERSION_35) {

            List<PsseNonTransformerBranch35> nonTransformerBranch35List = model.getNonTransformerBranches().stream()
                .map(m -> (PsseNonTransformerBranch35) m).collect(Collectors.toList()); // TODO improve

            BlockData.<PsseNonTransformerBranch35>writeBlock(PsseNonTransformerBranch35.class, nonTransformerBranch35List,
                headers, quoteFields, context.getDelimiter().charAt(0), outputStream);

            BlockData.writeEndOfBlockAndComment("END OF BRANCH DATA, BEGIN SYSTEM SWITCHING DEVICE DATA", outputStream);
        } else {
            BlockData.<PsseNonTransformerBranch>writeBlock(PsseNonTransformerBranch.class, model.getNonTransformerBranches(),
                headers, quoteFields, context.getDelimiter().charAt(0), outputStream);

            BlockData.writeEndOfBlockAndComment("END OF BRANCH DATA, BEGIN TRANSFORMER DATA", outputStream);
        }
    }

    TableData writex(PsseRawModel model, PsseContext context) {
        assertMinimumExpectedVersion(PsseBlockData.NON_TRANSFORMER_BRANCH_DATA, PsseVersion.VERSION_35,
            PsseFileFormat.FORMAT_RAWX);

        String[] headers = context.getNonTransformerBranchDataReadFields();
        List<PsseNonTransformerBranch35> nonTransformerBranch35List = model.getNonTransformerBranches().stream()
            .map(m -> (PsseNonTransformerBranch35) m).collect(Collectors.toList()); // TODO improve

        List<String> stringList = BlockData.<PsseNonTransformerBranch35>writexBlock(PsseNonTransformerBranch35.class,
            nonTransformerBranch35List, headers,
            BlockData.insideHeaders(nonTransformerBranchDataQuoteFields(this.getPsseVersion()), headers),
            context.getDelimiter().charAt(0));

        return new TableData(headers, stringList);
    }

    private static String[] nonTransformerBranchDataHeaders(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ibus", "jbus", "ckt", "rpu", "xpu", "bpu", "name", "rate1", "rate2", "rate3", "rate4", "rate5",
                "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12", "gi", "bi", "gj", "bj",
                "stat", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        } else { // Version 33
            return new String[] {"i", "j", "ckt", "r", "x", "b", "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj",
                "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        }
    }

    private static String[] nonTransformerBranchDataQuoteFields(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return new String[] {"ckt", "name"};
        } else { // Version 33
            return new String[] {"ckt"};
        }
    }
}
