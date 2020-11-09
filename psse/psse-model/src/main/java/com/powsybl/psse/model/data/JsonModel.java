/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class JsonModel {
    private final JsonNetwork network;

    JsonModel(JsonNetwork network) {
        this.network = network;
    }

    public JsonNetwork getNetwork() {
        return this.network;
    }

    public static class JsonNetwork {
        private ArrayData caseid;
        private TableData bus;
        private TableData load;
        private TableData fixshunt;
        private TableData generator;
        private TableData acline;
        private TableData transformer;
        private TableData area;
        private TableData twotermdc;
        private TableData impcor;
        private TableData msline;
        private TableData zone;
        private TableData iatransfer;
        private TableData owner;
        private TableData facts;
        private TableData swshunt;
        private TableData gne;

        void setCaseid(ArrayData caseid) {
            this.caseid = caseid;
        }

        public ArrayData getCaseid() {
            return this.caseid;
        }

        void setBus(TableData bus) {
            this.bus = bus;
        }

        public TableData getBus() {
            return this.bus;
        }

        void setLoad(TableData load) {
            this.load = load;
        }

        public TableData getLoad() {
            return this.load;
        }

        void setFixshunt(TableData fixshunt) {
            this.fixshunt = fixshunt;
        }

        public TableData getFixshunt() {
            return this.fixshunt;
        }

        void setGenerator(TableData generator) {
            this.generator = generator;
        }

        public TableData getGenerator() {
            return this.generator;
        }

        void setAcline(TableData acline) {
            this.acline = acline;
        }

        public TableData getAcline() {
            return this.acline;
        }

        void setTransformer(TableData transformer) {
            this.transformer = transformer;
        }

        public TableData getTransformer() {
            return this.transformer;
        }

        void setArea(TableData area) {
            this.area = area;
        }

        public TableData getArea() {
            return this.area;
        }

        void setTwotermdc(TableData twotermdc) {
            this.twotermdc = twotermdc;
        }

        public TableData getTwotermdc() {
            return this.twotermdc;
        }

        void setImpcor(TableData impcor) {
            this.impcor = impcor;
        }

        public TableData getImpcor() {
            return this.impcor;
        }

        void setMsline(TableData msline) {
            this.msline = msline;
        }

        public TableData getMsline() {
            return this.msline;
        }

        void setZone(TableData zone) {
            this.zone = zone;
        }

        public TableData getZone() {
            return this.zone;
        }

        void setIatransfer(TableData iatransfer) {
            this.iatransfer = iatransfer;
        }

        public TableData getIatransfer() {
            return this.iatransfer;
        }

        void setOwner(TableData owner) {
            this.owner = owner;
        }

        public TableData getOwner() {
            return this.owner;
        }

        void setFacts(TableData facts) {
            this.facts = facts;
        }

        public TableData getFacts() {
            return this.facts;
        }

        void setSwshunt(TableData swshunt) {
            this.swshunt = swshunt;
        }

        public TableData getSwshunt() {
            return this.swshunt;
        }

        void setGne(TableData gne) {
            this.gne = gne;
        }

        public TableData getGne() {
            return this.gne;
        }
    }

    @JsonPropertyOrder({"fields", "data"})
    public static class TableData {
        @JsonRawValue
        @JsonProperty("fields")
        private final List<String> quotedFields;
        private final List<String> data;

        TableData(String[] fields, List<String> data) {
            this.quotedFields = addQuote(fields);
            this.data = addSquareBrackets(data);
        }

        public List<String> getQuotedFields() {
            return this.quotedFields;
        }

        public List<String> getData() {
            return this.data;
        }

        private static List<String> addSquareBrackets(List<String> stringList) {
            List<String> bracketList = new ArrayList<>();
            stringList.forEach(s -> bracketList.add("[" + s + "]"));
            return bracketList;
        }
    }

    @JsonPropertyOrder({"fields", "data"})
    public static class ArrayData {
        @JsonRawValue
        @JsonProperty("fields")
        private final List<String> quotedFields;
        @JsonRawValue
        private final List<String> data;

        ArrayData(String[] fields, List<String> data) {
            this.quotedFields = addQuote(fields);
            this.data = data;
        }

        public List<String> getQuotedFields() {
            return this.quotedFields;
        }

        public List<String> getData() {
            return this.data;
        }
    }

    // null if this block have not read
    private static List<String> addQuote(String[] fields) {
        List<String> list = new ArrayList<>();
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                list.add("\"" + fields[i] + "\"");
            }
        }
        return list;
    }
}
