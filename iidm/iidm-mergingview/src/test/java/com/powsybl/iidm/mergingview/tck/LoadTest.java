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
import com.powsybl.iidm.network.tck.AbstractLoadTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LoadTest extends AbstractLoadTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("test", "test");
        network.merge(FictitiousSwitchFactory.create());
        return network;
    }

    @Test
    public void testChangesNotification() {
        TestUtil.notImplemented(super::testChangesNotification);
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        TestUtil.notImplemented(super::testSetterGetterInMultiVariants);
    }

    @Test
    public void testRemove() {
        TestUtil.notImplemented(super::testRemove);
    }
}
