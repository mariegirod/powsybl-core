/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseGenerator;
import com.powsybl.psse.model.PsseGenerator35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class GeneratorData extends AbstractDataBlock<PsseGenerator> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
    private static final String[] FIELD_NAMES_33 = {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};

    GeneratorData() {
        super(PsseDataBlock.GENERATOR_DATA);
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return FIELD_NAMES_35;
        } else {
            return FIELD_NAMES_33;
        }
    }

    @Override
    public Class<? extends PsseGenerator> psseTypeClass(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return PsseGenerator35.class;
        } else {
            return PsseGenerator.class;
        }
    }
}
