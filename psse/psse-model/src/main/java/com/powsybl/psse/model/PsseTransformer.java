/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

// order using alphabetic order, better field order
//@JsonPropertyOrder(alphabetic = true)
//
//@validate annotation is incompatible with reading partially the fields for writing (export process).
// If validated fields are not read then the validation fails and no records are returned

public class PsseTransformer {

    @Parsed(field = {"i", "ibus"})
    //@Validate
    private int i;

    @Parsed(field = { "j", "jbus" })
    //@Validate
    private int j;

    @Parsed(field = {"k", "kbus"})
    private int k = 0;

    @Parsed(defaultNullRead = "1")
    private String ckt;

    @Parsed
    private int cw = 1;

    @Parsed
    private int cz = 1;

    @Parsed
    private int cm = 1;

    @Parsed
    private double mag1 = 0;

    @Parsed
    private double mag2 = 0;

    @Parsed(field = {"nmetr", "nmet"})
    private int nmetr = 2;

    @Parsed(defaultNullRead = "            ")
    private String name;

    @Parsed
    private int stat = 1;

    @Parsed
    private int o1 = -1;

    @Parsed
    private double f1 = 1;

    @Parsed
    private int o2 = 0;

    @Parsed
    private double f2 = 1;

    @Parsed
    private int o3 = 0;

    @Parsed
    private double f3 = 1;

    @Parsed
    private int o4 = 0;

    @Parsed
    private double f4 = 1;

    @Parsed(defaultNullRead = "            ")
    private String vecgrp;

    @Parsed(field = {"r12", "r1_2"})
    private double r12 = 0;

    @Parsed(field = {"x12", "x1_2"})
    private double x12;

    @Parsed(field = {"sbase12", "sbase1_2"})
    private double sbase12 = Double.NaN;

    @Parsed(field = {"r23", "r2_3"})
    private double r23 = 0;

    @Parsed(field = {"x23", "x2_3"})
    private double x23 = Double.NaN;

    @Parsed(field = {"sbase23", "sbase2_3"})
    private double sbase23 = Double.NaN;

    @Parsed(field = {"r31", "r3_1"})
    private double r31 = 0;

    @Parsed(field = {"x31", "x3_1"})
    private double x31 = Double.NaN;

    @Parsed(field = {"sbase31", "sbase3_1"})
    private double sbase31 = Double.NaN;

    @Parsed
    private double vmstar = 1;

    @Parsed
    private double anstar = 0;

    @Parsed
    protected double windv1 = Double.NaN;

    @Parsed
    protected double nomv1 = 0;

    @Parsed
    protected double ang1 = 0;

    @Parsed
    protected double rata1 = 0;

    @Parsed
    protected double ratb1 = 0;

    @Parsed
    protected double ratc1 = 0;

    @Parsed
    protected int cod1 = 0;

    @Parsed
    protected int cont1 = 0;

    @Parsed
    protected double rma1 = Double.NaN;

    @Parsed
    protected double rmi1 = Double.NaN;

    @Parsed
    protected double vma1 = Double.NaN;

    @Parsed
    protected double vmi1 = Double.NaN;

    @Parsed
    protected int ntp1 = 33;

    @Parsed
    protected int tab1 = 0;

    @Parsed
    protected double cr1 = 0;

    @Parsed
    protected double cx1 = 0;

    @Parsed
    protected double cnxa1 = 0;

    @Parsed
    protected double windv2 = Double.NaN;

    @Parsed
    protected double nomv2 = 0;

    @Parsed
    protected double ang2 = 0;

    @Parsed
    protected double rata2 = 0;

    @Parsed
    protected double ratb2 = 0;

    @Parsed
    protected double ratc2 = 0;

    @Parsed
    protected int cod2 = 0;

    @Parsed
    protected int cont2 = 0;

    @Parsed
    protected double rma2 = Double.NaN;

    @Parsed
    protected double rmi2 = Double.NaN;

    @Parsed
    protected double vma2 = Double.NaN;

    @Parsed
    protected double vmi2 = Double.NaN;

    @Parsed
    protected int ntp2 = 33;

    @Parsed
    protected int tab2 = 0;

    @Parsed
    protected double cr2 = 0;

    @Parsed
    protected double cx2 = 0;

    @Parsed
    protected double cnxa2 = 0;

    @Parsed
    protected double windv3 = Double.NaN;

    @Parsed
    protected double nomv3 = 0;

    @Parsed
    protected double ang3 = 0;

    @Parsed
    protected double rata3 = 0;

    @Parsed
    protected double ratb3 = 0;

    @Parsed
    protected double ratc3 = 0;

    @Parsed
    protected int cod3 = 0;

    @Parsed
    protected int cont3 = 0;

    @Parsed
    protected double rma3 = Double.NaN;

    @Parsed
    protected double rmi3 = Double.NaN;

    @Parsed
    protected double vma3 = Double.NaN;

    @Parsed
    protected double vmi3 = Double.NaN;

    @Parsed
    protected int ntp3 = 33;

    @Parsed
    protected int tab3 = 0;

    @Parsed
    protected double cr3 = 0;

    @Parsed
    protected double cx3 = 0;

    @Parsed
    protected double cnxa3 = 0;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public int getCw() {
        return cw;
    }

    public void setCw(int cw) {
        this.cw = cw;
    }

    public int getCz() {
        return cz;
    }

    public void setCz(int cz) {
        this.cz = cz;
    }

    public int getCm() {
        return cm;
    }

    public void setCm(int cm) {
        this.cm = cm;
    }

    public double getMag1() {
        return mag1;
    }

    public void setMag1(double mag1) {
        this.mag1 = mag1;
    }

    public double getMag2() {
        return mag2;
    }

    public void setMag2(double mag2) {
        this.mag2 = mag2;
    }

    public int getNmetr() {
        return nmetr;
    }

    public void setNmetr(int nmetr) {
        this.nmetr = nmetr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
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

    public String getVecgrp() {
        return vecgrp;
    }

    public void setVecgrp(String vecgrp) {
        this.vecgrp = vecgrp;
    }

    public double getR12() {
        return r12;
    }

    public void setR12(double r12) {
        this.r12 = r12;
    }

    public double getX12() {
        return x12;
    }

    public void setX12(double x12) {
        this.x12 = x12;
    }

    public double getSbase12() {
        return sbase12;
    }

    public void setSbase12(double sbase12) {
        this.sbase12 = sbase12;
    }

    public double getR23() {
        return r23;
    }

    public void setR23(double r23) {
        this.r23 = r23;
    }

    public double getX23() {
        return x23;
    }

    public void setX23(double x23) {
        this.x23 = x23;
    }

    public double getSbase23() {
        return sbase23;
    }

    public void setSbase23(double sbase23) {
        this.sbase23 = sbase23;
    }

    public double getR31() {
        return r31;
    }

    public void setR31(double r31) {
        this.r31 = r31;
    }

    public double getX31() {
        return x31;
    }

    public void setX31(double x31) {
        this.x31 = x31;
    }

    public double getSbase31() {
        return sbase31;
    }

    public void setSbase31(double sbase31) {
        this.sbase31 = sbase31;
    }

    public double getVmstar() {
        return vmstar;
    }

    public void setVmstar(double vmstar) {
        this.vmstar = vmstar;
    }

    public double getAnstar() {
        return anstar;
    }

    public void setAnstar(double anstar) {
        this.anstar = anstar;
    }

    // winding1

    public double getWindv1() {
        return this.windv1;
    }

    public void setWindv1(double windv1) {
        this.windv1 = windv1;
    }

    public double getNomv1() {
        return this.nomv1;
    }

    public void setNomv1(double nomv1) {
        this.nomv1 = nomv1;
    }

    public double getAng1() {
        return this.ang1;
    }

    public void setAng1(double ang1) {
        this.ang1 = ang1;
    }

    public double getRata1() {
        return this.rata1;
    }

    public void setRata1(double rata1) {
        this.rata1 = rata1;
    }

    public double getRatb1() {
        return this.ratb1;
    }

    public void setRatb1(double ratb1) {
        this.ratb1 = ratb1;
    }

    public double getRatc1() {
        return this.ratc1;
    }

    public void setRatc1(double ratc1) {
        this.ratc1 = ratc1;
    }

    public int getCod1() {
        return this.cod1;
    }

    public void setCod1(int cod1) {
        this.cod1 = cod1;
    }

    public int getCont1() {
        return this.cont1;
    }

    public void setCont1(int cont1) {
        this.cont1 = cont1;
    }

    public double getRma1() {
        return this.rma1;
    }

    public void setRma1(double rma1) {
        this.rma1 = rma1;
    }

    public double getRmi1() {
        return this.rmi1;
    }

    public void setRmi1(double rmi1) {
        this.rmi1 = rmi1;
    }

    public double getVma1() {
        return this.vma1;
    }

    public void setVma1(double vma1) {
        this.vma1 = vma1;
    }

    public double getVmi1() {
        return this.vmi1;
    }

    public void setVmi1(double vmi1) {
        this.vmi1 = vmi1;
    }

    public int getNtp1() {
        return this.ntp1;
    }

    public void setNtp1(int ntp1) {
        this.ntp1 = ntp1;
    }

    public int getTab1() {
        return this.tab1;
    }

    public void setTab1(int tab1) {
        this.tab1 = tab1;
    }

    public double getCr1() {
        return this.cr1;
    }

    public void setCr1(double cr1) {
        this.cr1 = cr1;
    }

    public double getCx1() {
        return this.cx1;
    }

    public void setCx1(double cx1) {
        this.cx1 = cx1;
    }

    public double getCnxa1() {
        return this.cnxa1;
    }

    public void setCnxa1(double cnxa1) {
        this.cnxa1 = cnxa1;
    }

    // winding2

    public double getWindv2() {
        return this.windv2;
    }

    public void setWindv2(double windv2) {
        this.windv2 = windv2;
    }

    public double getNomv2() {
        return this.nomv2;
    }

    public void setNomv2(double nomv2) {
        this.nomv2 = nomv2;
    }

    public double getAng2() {
        return this.ang2;
    }

    public void setAng2(double ang2) {
        this.ang2 = ang2;
    }

    public double getRata2() {
        return this.rata2;
    }

    public void setRata2(double rata2) {
        this.rata2 = rata2;
    }

    public double getRatb2() {
        return this.ratb2;
    }

    public void setRatb2(double ratb2) {
        this.ratb2 = ratb2;
    }

    public double getRatc2() {
        return this.ratc2;
    }

    public void setRatc2(double ratc2) {
        this.ratc2 = ratc2;
    }

    public int getCod2() {
        return this.cod2;
    }

    public void setCod2(int cod2) {
        this.cod2 = cod2;
    }

    public int getCont2() {
        return this.cont2;
    }

    public void setCont2(int cont2) {
        this.cont2 = cont2;
    }

    public double getRma2() {
        return this.rma2;
    }

    public void setRma2(double rma2) {
        this.rma2 = rma2;
    }

    public double getRmi2() {
        return this.rmi2;
    }

    public void setRmi2(double rmi2) {
        this.rmi2 = rmi2;
    }

    public double getVma2() {
        return this.vma2;
    }

    public void setVma2(double vma2) {
        this.vma2 = vma2;
    }

    public double getVmi2() {
        return this.vmi2;
    }

    public void setVmi2(double vmi2) {
        this.vmi2 = vmi2;
    }

    public int getNtp2() {
        return this.ntp2;
    }

    public void setNtp2(int ntp2) {
        this.ntp2 = ntp2;
    }

    public int getTab2() {
        return this.tab2;
    }

    public void setTab2(int tab2) {
        this.tab2 = tab2;
    }

    public double getCr2() {
        return this.cr2;
    }

    public void setCr2(double cr2) {
        this.cr2 = cr2;
    }

    public double getCx2() {
        return this.cx2;
    }

    public void setCx2(double cx2) {
        this.cx2 = cx2;
    }

    public double getCnxa2() {
        return this.cnxa2;
    }

    public void setCnxa2(double cnxa2) {
        this.cnxa2 = cnxa2;
    }

    // winding3

    public double getWindv3() {
        return this.windv3;
    }

    public void setWindv3(double windv3) {
        this.windv3 = windv3;
    }

    public double getNomv3() {
        return this.nomv3;
    }

    public void setNomv3(double nomv3) {
        this.nomv3 = nomv3;
    }

    public double getAng3() {
        return this.ang3;
    }

    public void setAng3(double ang3) {
        this.ang3 = ang3;
    }

    public double getRata3() {
        return this.rata3;
    }

    public void setRata3(double rata3) {
        this.rata3 = rata3;
    }

    public double getRatb3() {
        return this.ratb3;
    }

    public void setRatb3(double ratb3) {
        this.ratb3 = ratb3;
    }

    public double getRatc3() {
        return this.ratc3;
    }

    public void setRatc3(double ratc3) {
        this.ratc3 = ratc3;
    }

    public int getCod3() {
        return this.cod3;
    }

    public void setCod3(int cod3) {
        this.cod3 = cod3;
    }

    public int getCont3() {
        return this.cont3;
    }

    public void setCont3(int cont3) {
        this.cont3 = cont3;
    }

    public double getRma3() {
        return this.rma3;
    }

    public void setRma3(double rma3) {
        this.rma3 = rma3;
    }

    public double getRmi3() {
        return this.rmi3;
    }

    public void setRmi3(double rmi3) {
        this.rmi3 = rmi3;
    }

    public double getVma3() {
        return this.vma3;
    }

    public void setVma3(double vma3) {
        this.vma3 = vma3;
    }

    public double getVmi3() {
        return this.vmi3;
    }

    public void setVmi3(double vmi3) {
        this.vmi3 = vmi3;
    }

    public int getNtp3() {
        return this.ntp3;
    }

    public void setNtp3(int ntp3) {
        this.ntp3 = ntp3;
    }

    public int getTab3() {
        return this.tab3;
    }

    public void setTab3(int tab3) {
        this.tab3 = tab3;
    }

    public double getCr3() {
        return this.cr3;
    }

    public void setCr3(double cr3) {
        this.cr3 = cr3;
    }

    public double getCx3() {
        return this.cx3;
    }

    public void setCx3(double cx3) {
        this.cx3 = cx3;
    }

    public double getCnxa3() {
        return this.cnxa3;
    }

    public void setCnxa3(double cnxa3) {
        this.cnxa3 = cnxa3;
    }

    // windings

    @JsonIgnore
    public WindingRecord getWindingRecord1() {
        return new WindingRecord(windv1, nomv1, ang1, rata1, ratb1, ratc1, cod1, cont1, rma1, rmi1, vma1, vmi1, ntp1,
            tab1, cr1, cx1, cnxa1);
    }

    @JsonIgnore
    public WindingRecord getWindingRecord2() {
        return new WindingRecord(windv2, nomv2, ang2, rata2, ratb2, ratc2, cod2, cont2, rma2, rmi2, vma2, vmi2, ntp2,
            tab2, cr2, cx2, cnxa2);
    }

    @JsonIgnore
    public WindingRecord getWindingRecord3() {
        return new WindingRecord(windv3, nomv3, ang3, rata3, ratb3, ratc3, cod3, cont3, rma3, rmi3, vma3, vmi3, ntp3,
            tab3, cr3, cx3, cnxa3);
    }

    public static class WindingRecord {
        private final double windv;
        private final double nomv;
        private final double ang;
        private final double rata;
        private final double ratb;
        private final double ratc;
        private final int cod;
        private final int cont;
        private final double rma;
        private final double rmi;
        private final double vma;
        private final double vmi;
        private final int ntp;
        private final int tab;
        private final double cr;
        private final double cx;
        private final double cnxa;

        WindingRecord(double windv, double nomv, double ang, double rata, double ratb, double ratc, int cod, int cont,
            double rma, double rmi, double vma, double vmi, int ntp, int tab, double cr, double cx, double cnxa) {
            this.windv = windv;
            this.nomv = nomv;
            this.ang = ang;
            this.rata = rata;
            this.ratb = ratb;
            this.ratc = ratc;
            this.cod = cod;
            this.cont = cont;
            this.rma = rma;
            this.rmi = rmi;
            this.vma = vma;
            this.vmi = vmi;
            this.ntp = ntp;
            this.tab = tab;
            this.cr = cr;
            this.cx = cx;
            this.cnxa = cnxa;
        }

        public double getWindv() {
            return windv;
        }

        public double getNomv() {
            return nomv;
        }

        public double getAng() {
            return ang;
        }

        public double getRata() {
            return rata;
        }

        public double getRatb() {
            return ratb;
        }

        public double getRatc() {
            return ratc;
        }

        public int getCod() {
            return cod;
        }

        public int getCont() {
            return cont;
        }

        public double getRma() {
            return rma;
        }

        public double getRmi() {
            return rmi;
        }

        public double getVma() {
            return vma;
        }

        public double getVmi() {
            return vmi;
        }

        public int getNtp() {
            return ntp;
        }

        public int getTab() {
            return tab;
        }

        public double getCr() {
            return cr;
        }

        public double getCx() {
            return cx;
        }

        public double getCnxa() {
            return cnxa;
        }
    }
}
