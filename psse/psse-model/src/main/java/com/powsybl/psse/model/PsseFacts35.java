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

@JsonIgnoreProperties({"remot"})

public class PsseFacts35 extends PsseFacts {

    @Parsed
    private int fcreg = 0;

    @Parsed
    private int nreg = 0;

    @Override
    public int getRemot() {
        throw new PsseException("Remot not available in version 35");
    }

    @Override
    public void setRemot(int remot) {
        throw new PsseException("Remot not available in version 35");
    }

    public int getFcreg() {
        return fcreg;
    }

    public void setFcreg(int fcreg) {
        this.fcreg = fcreg;
    }

    public int getNreg() {
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }
}
