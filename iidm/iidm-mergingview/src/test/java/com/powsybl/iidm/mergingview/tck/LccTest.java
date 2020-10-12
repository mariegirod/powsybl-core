/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.mergingview.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractLccTest;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LccTest extends AbstractLccTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("test", "test");
        network.merge(HvdcTestNetwork.createLcc());
        return network;
    }

    @Test
    public void testHvdcLineRemove() {
        TestUtil.notImplemented(super::testHvdcLineRemove);
    }
}
