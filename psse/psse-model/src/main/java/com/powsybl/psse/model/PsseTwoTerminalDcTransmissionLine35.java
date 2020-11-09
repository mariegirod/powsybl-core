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
public class PsseTwoTerminalDcTransmissionLine35 extends PsseTwoTerminalDcTransmissionLine {

    @Parsed
    private int ndr = 0;

    @Parsed
    private int ndi = 0;

    public int getNdr() {
        return ndr;
    }

    public void setNdr(int ndr) {
        this.ndr = ndr;
    }

    public int getNdi() {
        return ndi;
    }

    public void setNdi(int ndi) {
        this.ndi = ndi;
    }
}
