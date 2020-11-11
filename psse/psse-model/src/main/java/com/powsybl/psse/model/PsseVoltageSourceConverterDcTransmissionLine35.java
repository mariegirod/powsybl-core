/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

@JsonIgnoreProperties({"remot1", "remot2"})

public class PsseVoltageSourceConverterDcTransmissionLine35 extends PsseVoltageSourceConverterDcTransmissionLine {

    @Parsed
    private int vsreg1 = 0;

    @Parsed
    private int nreg1 = 0;

    @Parsed
    private int vsreg2 = 0;

    @Parsed
    private int nreg2 = 0;

    @Override
    public int getRemot1() {
        throw new PsseException("Remot1 not available in version 35");
    }

    @Override
    public int getRemot2() {
        throw new PsseException("Remot2 not available in version 35");
    }

    public int getVsreg1() {
        return vsreg1;
    }

    public void setVsreg1(int vsreg1) {
        this.vsreg1 = vsreg1;
    }

    public int getNreg1() {
        return nreg1;
    }

    public void setNreg1(int nreg1) {
        this.nreg1 = nreg1;
    }

    public int getVsreg2() {
        return vsreg2;
    }

    public void setVsreg2(int vsreg2) {
        this.vsreg2 = vsreg2;
    }

    public int getNreg2() {
        return nreg2;
    }

    public void setNreg2(int nreg2) {
        this.nreg2 = nreg2;
    }
}
