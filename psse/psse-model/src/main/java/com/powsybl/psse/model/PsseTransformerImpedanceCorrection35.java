/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

@JsonIgnoreProperties({"points"})

public class PsseTransformerImpedanceCorrection35 extends PsseTransformerImpedanceCorrection {

    private List<PsseTransformerImpedanceCorrection35Point> points35;

    public PsseTransformerImpedanceCorrection35(int i) {
        super(i);
        this.points35 = new ArrayList<>();
    }

    @Override
    public List<PsseTransformerImpedanceCorrectionPoint> getPoints() {
        throw new PsseException("Points not available in version 35");
    }

    public List<PsseTransformerImpedanceCorrection35Point> getPoints35() {
        return points35;
    }

    public static class PsseTransformerImpedanceCorrection35Point {
        private double t;
        private double ref;
        private double imf;

        public PsseTransformerImpedanceCorrection35Point(double t, double ref, double imf) {
            this.t = t;
            this.ref = ref;
            this.imf = imf;
        }

        public double getT() {
            return t;
        }

        public double getRef() {
            return ref;
        }

        public double getImf() {
            return imf;
        }
    }

    public static class PsseTransformerImpedanceCorrection35Record1 {

        @Parsed
        private int i;

        @Nested
        private PsseTransformerImpedanceCorrection35Record2 record2;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public PsseTransformerImpedanceCorrection35Record2 getRecord2() {
            return record2;
        }

        public void setRecord2(PsseTransformerImpedanceCorrection35Record2 record2) {
            this.record2 = record2;
        }
    }

    public static class PsseTransformerImpedanceCorrection35Record2 {

        @Parsed
        private double t1 = 0.0;

        @Parsed
        private double ref1 = 0.0;

        @Parsed
        private double imf1 = 0.0;

        @Parsed
        private double t2 = 0.0;

        @Parsed
        private double ref2 = 0.0;

        @Parsed
        private double imf2 = 0.0;

        @Parsed
        private double t3 = 0.0;

        @Parsed
        private double ref3 = 0.0;

        @Parsed
        private double imf3 = 0.0;

        @Parsed
        private double t4 = 0.0;

        @Parsed
        private double ref4 = 0.0;

        @Parsed
        private double imf4 = 0.0;

        @Parsed
        private double t5 = 0.0;

        @Parsed
        private double ref5 = 0.0;

        @Parsed
        private double imf5 = 0.0;

        @Parsed
        private double t6 = 0.0;

        @Parsed
        private double ref6 = 0.0;

        @Parsed
        private double imf6 = 0.0;

        public double getT1() {
            return t1;
        }

        public double getRef1() {
            return ref1;
        }

        public double getImf1() {
            return imf1;
        }

        public void setTF1(double t1, double ref1, double imf1) {
            this.t1 = t1;
            this.ref1 = ref1;
            this.imf1 = imf1;
        }

        public double getT2() {
            return t2;
        }

        public double getRef2() {
            return ref2;
        }

        public double getImf2() {
            return imf2;
        }

        public void setTF2(double t2, double ref2, double imf2) {
            this.t2 = t2;
            this.ref2 = ref2;
            this.imf2 = imf2;
        }

        public double getT3() {
            return t3;
        }

        public double getRef3() {
            return ref3;
        }

        public double getImf3() {
            return imf3;
        }

        public void setTF3(double t3, double ref3, double imf3) {
            this.t3 = t3;
            this.ref3 = ref3;
            this.imf3 = imf3;
        }

        public double getT4() {
            return t4;
        }

        public double getRef4() {
            return ref4;
        }

        public double getImf4() {
            return imf4;
        }

        public void setTF4(double t4, double ref4, double imf4) {
            this.t4 = t4;
            this.ref4 = ref4;
            this.imf4 = imf4;
        }

        public double getT5() {
            return t5;
        }

        public double getRef5() {
            return ref5;
        }

        public double getImf5() {
            return imf5;
        }

        public void setTF5(double t5, double ref5, double imf5) {
            this.t5 = t5;
            this.ref5 = ref5;
            this.imf5 = imf5;
        }

        public double getT6() {
            return t6;
        }

        public double getRef6() {
            return ref6;
        }

        public double getImf6() {
            return imf6;
        }

        public void setTF6(double t6, double ref6, double imf6) {
            this.t6 = t6;
            this.ref6 = ref6;
            this.imf6 = imf6;
        }
    }

    public static class PsseTransformerImpedanceCorrection35Recordx {

        @Parsed
        private int itable;

        @Parsed
        private double tap;

        @Parsed
        private double refact;

        @Parsed
        private double imfact;

        public int getItable() {
            return itable;
        }

        public double getTap() {
            return tap;
        }

        public double getRefact() {
            return refact;
        }

        public double getImfact() {
            return imfact;
        }

        public void set(int itable, double tap, double refact, double imfact) {
            this.itable = itable;
            this.tap = tap;
            this.refact = refact;
            this.imfact = imfact;
        }
    }
}
