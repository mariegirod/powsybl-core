/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class IidmXmlUtil {

    public static void assertMinimumVersion(String rootElementName, String elementName, IidmXmlVersion minVersion, NetworkXmlReaderContext context) {
        if (context.getVersion().compareTo(minVersion) < 0) {
            throw new PowsyblException(rootElementName + "." + elementName + " is not supported for IIDM-XML version " + context.getVersion().toString(".") + ". " +
                    "IIDM-XML version should be >= " + minVersion.toString("."));
        }
    }

    private IidmXmlUtil() {
    }
}