/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.ArrayList;
import java.util.List;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseTransformerImpedanceCorrection {

    private int i;
    private List<PsseTransformerImpedanceCorrectionPoint> points;

    public PsseTransformerImpedanceCorrection(int i) {
        this.i = i;
        this.points = new ArrayList<>();
    }

    public int getI() {
        return i;
    }

    public List<PsseTransformerImpedanceCorrectionPoint> getPoints() {
        return points;
    }

    public static class PsseTransformerImpedanceCorrectionPoint {
        private double t;
        private double f;

        public PsseTransformerImpedanceCorrectionPoint(double t, double f) {
            this.t = t;
            this.f = f;
        }

        public double getT() {
            return t;
        }

        public double getF() {
            return f;
        }

    }

    public static class PsseTransformerImpedanceCorrectionRecord {

        @Parsed
        private int i;

        @Parsed
        private double t1 = 0.0;

        @Parsed
        private double f1 = 0.0;

        @Parsed
        private double t2 = 0.0;

        @Parsed
        private double f2 = 0.0;

        @Parsed
        private double t3 = 0.0;

        @Parsed
        private double f3 = 0.0;

        @Parsed
        private double t4 = 0.0;

        @Parsed
        private double f4 = 0.0;

        @Parsed
        private double t5 = 0.0;

        @Parsed
        private double f5 = 0.0;

        @Parsed
        private double t6 = 0.0;

        @Parsed
        private double f6 = 0.0;

        @Parsed
        private double t7 = 0.0;

        @Parsed
        private double f7 = 0.0;

        @Parsed
        private double t8 = 0.0;

        @Parsed
        private double f8 = 0.0;

        @Parsed
        private double t9 = 0.0;

        @Parsed
        private double f9 = 0.0;

        @Parsed
        private double t10 = 0.0;

        @Parsed
        private double f10 = 0.0;

        @Parsed
        private double t11 = 0.0;

        @Parsed
        private double f11 = 0.0;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public double getT1() {
            return t1;
        }

        public double getF1() {
            return f1;
        }

        public void setTF1(double t1, double f1) {
            this.t1 = t1;
            this.f1 = f1;
        }

        public double getT2() {
            return t2;
        }

        public double getF2() {
            return f2;
        }

        public void setTF2(double t2, double f2) {
            this.t2 = t2;
            this.f2 = f2;
        }

        public double getT3() {
            return t3;
        }

        public double getF3() {
            return f3;
        }

        public void setTF3(double t3, double f3) {
            this.t3 = t3;
            this.f3 = f3;
        }

        public double getT4() {
            return t4;
        }

        public double getF4() {
            return f4;
        }

        public void setTF4(double t4, double f4) {
            this.t4 = t4;
            this.f4 = f4;
        }

        public double getT5() {
            return t5;
        }

        public double getF5() {
            return f5;
        }

        public void setTF5(double t5, double f5) {
            this.t5 = t5;
            this.f5 = f5;
        }

        public double getT6() {
            return t6;
        }

        public double getF6() {
            return f6;
        }

        public void setTF6(double t6, double f6) {
            this.t6 = t6;
            this.f6 = f6;
        }

        public double getT7() {
            return t7;
        }

        public double getF7() {
            return f7;
        }

        public void setTF7(double t7, double f7) {
            this.t7 = t7;
            this.f7 = f7;
        }

        public double getT8() {
            return t8;
        }

        public double getF8() {
            return f8;
        }

        public void setTF8(double t8, double f8) {
            this.t8 = t8;
            this.f8 = f8;
        }

        public double getT9() {
            return t9;
        }

        public double getF9() {
            return f9;
        }

        public void setTF9(double t9, double f9) {
            this.t9 = t9;
            this.f9 = f9;
        }

        public double getT10() {
            return t10;
        }

        public double getF10() {
            return f10;
        }

        public void setTF10(double t10, double f10) {
            this.t10 = t10;
            this.f10 = f10;
        }

        public double getT11() {
            return t11;
        }

        public double getF11() {
            return f11;
        }

        public void setTF11(double t11, double f11) {
            this.t11 = t11;
            this.f11 = f11;
        }
    }
}
