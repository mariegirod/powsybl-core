/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface VoltageLimits extends OperationalLimits {

    @Override
    default LimitType getLimitType() {
        return LimitType.VOLTAGE;
    }

    double getHighVoltage();

    VoltageLimits setHighVoltage(double highVoltage);

    double getLowVoltage();

    VoltageLimits setLowVoltage(double lowVoltage);
}