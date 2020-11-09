/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseContext {

    private String delimiter;

    private String[] caseIdentificationDataReadFields;
    private String[] busDataReadFields;
    private String[] loadDataReadFields;
    private String[] fixedBusShuntDataReadFields;
    private String[] generatorDataReadFields;
    private String[] nonTransformerBranchDataReadFields;
    private String[] t3wTransformerDataReadFields;
    private String[] t2wTransformerDataReadFields;
    private String[] areaInterchangeDataReadFields;
    private String[] twoTerminalDcTransmissionLineDataReadFields;
    private String[] transformerImpedanceCorrectionTablesDataReadFields;
    private String[] multiSectionLineGroupingDataReadFields;
    private String[] interareaTransferDataReadFields;
    private String[] zoneDataReadFields;
    private String[] ownerDataReadFields;
    private String[] factsDeviceDataReadFields;
    private String[] switchedShuntDataReadFields;
    private String[] gneDeviceDataReadFields;

    PsseContext() {
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setCaseIdentificationDataReadFields(String[] fields) {
        this.caseIdentificationDataReadFields = fields;
    }

    public String[] getCaseIdentificationDataReadFields() {
        return this.caseIdentificationDataReadFields;
    }

    public void setBusDataReadFields(String[] fields) {
        this.busDataReadFields = fields;
    }

    public String[] getBusDataReadFields() {
        return this.busDataReadFields;
    }

    public void setLoadDataReadFields(String[] fields) {
        this.loadDataReadFields = fields;
    }

    public String[] getLoadDataReadFields() {
        return this.loadDataReadFields;
    }

    public void setFixedBusShuntDataReadFields(String[] fields) {
        this.fixedBusShuntDataReadFields = fields;
    }

    public String[] getFixedBusShuntDataReadFields() {
        return this.fixedBusShuntDataReadFields;
    }

    public void setGeneratorDataReadFields(String[] fields) {
        this.generatorDataReadFields = fields;
    }

    public String[] getGeneratorDataReadFields() {
        return this.generatorDataReadFields;
    }

    public void setNonTransformerBranchDataReadFields(String[] fields) {
        this.nonTransformerBranchDataReadFields = fields;
    }

    public String[] getNonTransformerBranchDataReadFields() {
        return this.nonTransformerBranchDataReadFields;
    }

    public void set3wTransformerDataReadFields(String[] fields) {
        this.t3wTransformerDataReadFields = fields;
    }

    public boolean is3wTransformerDataReadFieldsEmpty() {
        return this.t3wTransformerDataReadFields == null;
    }

    public String[] get3wTransformerDataReadFields() {
        return this.t3wTransformerDataReadFields;
    }

    public void set2wTransformerDataReadFields(String[] fields) {
        this.t2wTransformerDataReadFields = fields;
    }

    public boolean is2wTransformerDataReadFieldsEmpty() {
        return this.t2wTransformerDataReadFields == null;
    }

    public String[] get2wTransformerDataReadFields() {
        return this.t2wTransformerDataReadFields;
    }

    public void setAreaInterchangeDataReadFields(String[] fields) {
        this.areaInterchangeDataReadFields = fields;
    }

    public String[] getAreaInterchangeDataReadFields() {
        return this.areaInterchangeDataReadFields;
    }

    public void setTwoTerminalDcTransmissionLineDataReadFields(String[] fields) {
        this.twoTerminalDcTransmissionLineDataReadFields = fields;
    }

    public String[] getTwoTerminalDcTransmissionLineDataReadFields() {
        return this.twoTerminalDcTransmissionLineDataReadFields;
    }

    public void setTransformerImpedanceCorrectionTablesDataReadFields(String[] fields) {
        this.transformerImpedanceCorrectionTablesDataReadFields = fields;
    }

    public String[] getTransformerImpedanceCorrectionTablesDataReadFields() {
        return this.transformerImpedanceCorrectionTablesDataReadFields;
    }

    public void setMultiSectionLineGroupingDataReadFields(String[] fields) {
        this.multiSectionLineGroupingDataReadFields = fields;
    }

    public String[] getMultiSectionLineGroupingDataReadFields() {
        return this.multiSectionLineGroupingDataReadFields;
    }

    public void setInterareaTransferDataReadFields(String[] fields) {
        this.interareaTransferDataReadFields = fields;
    }

    public String[] getInterareaTransferDataReadFields() {
        return this.interareaTransferDataReadFields;
    }

    public void setZoneDataReadFields(String[] fields) {
        this.zoneDataReadFields = fields;
    }

    public String[] getZoneDataReadFields() {
        return this.zoneDataReadFields;
    }

    public void setOwnerDataReadFields(String[] fields) {
        this.ownerDataReadFields = fields;
    }

    public String[] getOwnerDataReadFields() {
        return this.ownerDataReadFields;
    }

    public void setFactsDeviceDataReadFields(String[] fields) {
        this.factsDeviceDataReadFields = fields;
    }

    public String[] getFactsDeviceDataReadFields() {
        return this.factsDeviceDataReadFields;
    }

    public void setSwitchedShuntDataReadFields(String[] fields) {
        this.switchedShuntDataReadFields = fields;
    }

    public String[] getSwitchedShuntDataReadFields() {
        return this.switchedShuntDataReadFields;
    }

    public void setGneDeviceDataReadFields(String[] fields) {
        this.gneDeviceDataReadFields = fields;
    }

    public String[] getGneDeviceDataReadFields() {
        return this.gneDeviceDataReadFields;
    }
}
