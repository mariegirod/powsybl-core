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

        public double getT1() {
            return t1;
        }

        public double getT2() {
            return t2;
        }

        public double getT3() {
            return t3;
        }

        public double getT4() {
            return t4;
        }

        public double getT5() {
            return t5;
        }

        public double getT6() {
            return t6;
        }

        public double getT7() {
            return t7;
        }

        public double getT8() {
            return t8;
        }

        public double getT9() {
            return t9;
        }

        public double getT10() {
            return t10;
        }

        public double getT11() {
            return t11;
        }
    }
}
