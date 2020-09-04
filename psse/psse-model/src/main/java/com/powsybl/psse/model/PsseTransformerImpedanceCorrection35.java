/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
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
}
