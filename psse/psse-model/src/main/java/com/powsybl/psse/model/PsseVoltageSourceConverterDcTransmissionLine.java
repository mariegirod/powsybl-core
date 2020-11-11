/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseVoltageSourceConverterDcTransmissionLine {

    @Parsed
    private String name;

    @Parsed
    private int mdc = 1;

    @Parsed
    private double rdc;

    @Parsed
    private int o1 = 1;

    @Parsed
    private double f1 = 1.0;

    @Parsed
    private int o2 = 0;

    @Parsed
    private double f2 = 1.0;

    @Parsed
    private int o3 = 0;

    @Parsed
    private double f3 = 1.0;

    @Parsed
    private int o4 = 0;

    @Parsed
    private double f4 = 1.0;

    @Parsed
    private int ibus1;

    @Parsed
    private int type1;

    @Parsed
    private int mode1 = 1;

    @Parsed
    private double dcset1;

    @Parsed
    private double acset1 = 1.0;

    @Parsed
    private double aloss1 = 0.0;

    @Parsed
    private double bloss1 = 0.0;

    @Parsed
    private double minloss1 = 0.0;

    @Parsed
    private double smax1 = 0.0;

    @Parsed
    private double imax1 = 0.0;

    @Parsed
    private double pwf1 = 1.0;

    @Parsed
    private double maxq1 = 9999.0;

    @Parsed
    private double minq1 = -9999.0;

    @Parsed
    private int remot1 = 0;

    @Parsed
    private double rmpct1 = 100.0;

    @Parsed
    private int ibus2;

    @Parsed
    private int type2;

    @Parsed
    private int mode2 = 1;

    @Parsed
    private double dcset2;

    @Parsed
    private double acset2 = 1.0;

    @Parsed
    private double aloss2 = 0.0;

    @Parsed
    private double bloss2 = 0.0;

    @Parsed
    private double minloss2 = 0.0;

    @Parsed
    private double smax2 = 0.0;

    @Parsed
    private double imax2 = 0.0;

    @Parsed
    private double pwf2 = 1.0;

    @Parsed
    private double maxq2 = 9999.0;

    @Parsed
    private double minq2 = -9999.0;

    @Parsed
    private int remot2 = 0;

    @Parsed
    private double rmpct2 = 100.0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMdc() {
        return mdc;
    }

    public void setMdc(int mdc) {
        this.mdc = mdc;
    }

    public double getRdc() {
        return rdc;
    }

    public void setRdc(double rdc) {
        this.rdc = rdc;
    }

    public int getO1() {
        return o1;
    }

    public void setO1(int o1) {
        this.o1 = o1;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public int getO2() {
        return o2;
    }

    public void setO2(int o2) {
        this.o2 = o2;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public int getO4() {
        return o4;
    }

    public void setO4(int o4) {
        this.o4 = o4;
    }

    public double getF4() {
        return f4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public int getIbus1() {
        return ibus1;
    }

    public void setIbus1(int ibus1) {
        this.ibus1 = ibus1;
    }

    public int getType1() {
        return type1;
    }

    public void setType1(int type1) {
        this.type1 = type1;
    }

    public int getMode1() {
        return mode1;
    }

    public void setMode1(int mode1) {
        this.mode1 = mode1;
    }

    public double getDcset1() {
        return dcset1;
    }

    public void setDcset1(double dcset1) {
        this.dcset1 = dcset1;
    }

    public double getAcset1() {
        return acset1;
    }

    public void setAcset1(double acset1) {
        this.acset1 = acset1;
    }

    public double getAloss1() {
        return aloss1;
    }

    public void setAloss1(double aloss1) {
        this.aloss1 = aloss1;
    }

    public double getBloss1() {
        return bloss1;
    }

    public void setBloss1(double bloss1) {
        this.bloss1 = bloss1;
    }

    public double getMinloss1() {
        return minloss1;
    }

    public void setMinloss1(double minloss1) {
        this.minloss1 = minloss1;
    }

    public double getSmax1() {
        return smax1;
    }

    public void setSmax1(double smax1) {
        this.smax1 = smax1;
    }

    public double getImax1() {
        return imax1;
    }

    public void setImax1(double imax1) {
        this.imax1 = imax1;
    }

    public double getPwf1() {
        return pwf1;
    }

    public void setPwf1(double pwf1) {
        this.pwf1 = pwf1;
    }

    public double getMaxq1() {
        return maxq1;
    }

    public void setMaxq1(double maxq1) {
        this.maxq1 = maxq1;
    }

    public double getMinq1() {
        return minq1;
    }

    public void setMinq1(double minq1) {
        this.minq1 = minq1;
    }

    public int getRemot1() {
        return remot1;
    }

    public void setRemot1(int remot1) {
        this.remot1 = remot1;
    }

    public double getRmpct1() {
        return rmpct1;
    }

    public void setRmpct1(double rmpct1) {
        this.rmpct1 = rmpct1;
    }

    public int getIbus2() {
        return ibus2;
    }

    public void setIbus2(int ibus2) {
        this.ibus2 = ibus2;
    }

    public int getType2() {
        return type2;
    }

    public void setType2(int type2) {
        this.type2 = type2;
    }

    public int getMode2() {
        return mode2;
    }

    public void setMode2(int mode2) {
        this.mode2 = mode2;
    }

    public double getDcset2() {
        return dcset2;
    }

    public void setDcset2(double dcset2) {
        this.dcset2 = dcset2;
    }

    public double getAcset2() {
        return acset2;
    }

    public void setAcset2(double acset2) {
        this.acset2 = acset2;
    }

    public double getAloss2() {
        return aloss2;
    }

    public void setAloss2(double aloss2) {
        this.aloss2 = aloss2;
    }

    public double getBloss2() {
        return bloss2;
    }

    public void setBloss2(double bloss2) {
        this.bloss2 = bloss2;
    }

    public double getMinloss2() {
        return minloss2;
    }

    public void setMinloss2(double minloss2) {
        this.minloss2 = minloss2;
    }

    public double getSmax2() {
        return smax2;
    }

    public void setSmax2(double smax2) {
        this.smax2 = smax2;
    }

    public double getImax2() {
        return imax2;
    }

    public void setImax2(double imax2) {
        this.imax2 = imax2;
    }

    public double getPwf2() {
        return pwf2;
    }

    public void setPwf2(double pwf2) {
        this.pwf2 = pwf2;
    }

    public double getMaxq2() {
        return maxq2;
    }

    public void setMaxq2(double maxq2) {
        this.maxq2 = maxq2;
    }

    public double getMinq2() {
        return minq2;
    }

    public void setMinq2(double minq2) {
        this.minq2 = minq2;
    }

    public int getRemot2() {
        return remot2;
    }

    public void setRemot2(int remote2) {
        this.remot2 = remote2;
    }

    public double getRmpct2() {
        return rmpct2;
    }

    public void setRmpct2(double rmpct2) {
        this.rmpct2 = rmpct2;
    }
}
