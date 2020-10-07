/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractBatteryTest;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class BatteryTest extends AbstractBatteryTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("merged", "merged");
        network.merge(BatteryNetworkFactory.create());
        return network;
    }

    @Test
    public void testRemove() {
        try {
            super.testRemove();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
