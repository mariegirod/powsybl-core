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
public class PsseLineGrouping {

    @Parsed
    private int i;

    @Parsed
    private int j;

    @Parsed(defaultNullRead = "&1")
    private String id;

    @Parsed
    private int met = 1;

    @Parsed
    private int dum1;

    @Parsed
    private int dum2;

    @Parsed
    private int dum3;

    @Parsed
    private int dum4;

    @Parsed
    private int dum5;

    @Parsed
    private int dum6;

    @Parsed
    private int dum7;

    @Parsed
    private int dum8;

    @Parsed
    private int dum9;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDum1() {
        return dum1;
    }

    public void setDum1(int dum1) {
        this.dum1 = dum1;
    }

    public int getDum2() {
        return dum2;
    }

    public void setDum2(int dum2) {
        this.dum2 = dum2;
    }

    public int getDum3() {
        return dum3;
    }

    public void setDum3(int dum3) {
        this.dum3 = dum3;
    }

    public int getDum4() {
        return dum4;
    }

    public void setDum4(int dum4) {
        this.dum4 = dum4;
    }

    public int getDum5() {
        return dum5;
    }

    public void setDum5(int dum5) {
        this.dum5 = dum5;
    }

    public int getDum6() {
        return dum6;
    }

    public void setDum6(int dum6) {
        this.dum6 = dum6;
    }

    public int getDum7() {
        return dum7;
    }

    public void setDum7(int dum7) {
        this.dum7 = dum7;
    }

    public int getDum8() {
        return dum8;
    }

    public void setDum8(int dum8) {
        this.dum8 = dum8;
    }

    public int getDum9() {
        return dum9;
    }

    public void setDum9(int dum9) {
        this.dum9 = dum9;
    }
}
