/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseLoad;
import com.powsybl.psse.model.PsseLoad35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class LoadData extends AbstractDataBlock<PsseLoad> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt", "dgenp", "dgenq", "dgenm", "loadtype"};
    private static final String[] FIELD_NAMES_33 = {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt"};

    LoadData() {
        super(PsseDataBlock.LOAD_DATA);
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
    public Class<? extends PsseLoad> psseTypeClass(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return PsseLoad35.class;
        } else {
            return PsseLoad.class;
        }
    }
}
