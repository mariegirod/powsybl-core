/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DanglingLineXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/danglingLine.xml"));

        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/danglingLine.xml");

    }
}