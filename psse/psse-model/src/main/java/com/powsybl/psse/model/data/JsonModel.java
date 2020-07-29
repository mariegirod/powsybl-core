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
import com.powsybl.psse.model.data.JsonModel.RawString;

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

    @JsonPropertyOrder({"caseid", "bus", "load", "generator", "acline"})
    public static class JsonNetwork {
        private NetworkBlockData caseid;
        private NetworkBlockData bus;
        private NetworkBlockData load;
        private NetworkBlockData generator;
        private NetworkBlockData acline;

        void setCaseid(NetworkBlockData caseid) {
            this.caseid = caseid;
        }

        public NetworkBlockData getCaseid() {
            return this.caseid;
        }

        void setBus(NetworkBlockData bus) {
            this.bus = bus;
        }

        public NetworkBlockData getBus() {
            return this.bus;
        }

        void setLoad(NetworkBlockData load) {
            this.load = load;
        }

        public NetworkBlockData getLoad() {
            return this.load;
        }

        void setGenerator(NetworkBlockData generator) {
            this.generator = generator;
        }

        public NetworkBlockData getGenerator() {
            return this.generator;
        }

        void setAcline(NetworkBlockData acline) {
            this.acline = acline;
        }

        public NetworkBlockData getAcline() {
            return this.acline;
        }
    }

    @JsonPropertyOrder({"fields", "data"})
    public static class NetworkBlockData {
        @JsonRawValue
        @JsonProperty("fields")
        private final List<String> quotedFields;
        private final List<RawString> data;

        NetworkBlockData(String[] fields, List<String> data) {
            this.quotedFields = quotexFields(fields);
            this.data = createRawStringList(data);
        }

        public List<String> getQuotedFields() {
            return this.quotedFields;
        }

        public List<RawString> getData() {
            return this.data;
        }
        
        private static List<RawString> createRawStringList(List<String> stringList) {
            List<RawString> rawList = new ArrayList<>();
            stringList.forEach(s -> rawList.add(new RawString("[" + s + "]")));
            return rawList;
        }

        private static List<String> quotexFields(String[] fields) {
            List<String> list = new ArrayList<>();

            for (int i = 0; i < fields.length; i++) {
                list.add("\"" + fields[i] + "\"");
            }
            return list;
        }
    }

    public static class RawString {
        @JsonRawValue
        private final String data;

        RawString(String data) {
            this.data = data;
        }

        public String getData() {
            return this.data;
        }
    }
  }
