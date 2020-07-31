/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Objects;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.psse.model.data.PsseData;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseWriter {

    public void write(PsseRawModel model, PsseContext context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "raw", false));
        new PsseData().write(model, context, bufferedOutputStream);

        bufferedOutputStream.close();
    }

    public void writex(PsseRawModel model, PsseContext context, DataSource dataSource) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(context);
        Objects.requireNonNull(dataSource);

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dataSource.newOutputStream(null, "rawx", false));
        new PsseData().writex(model, context, bufferedOutputStream);

        bufferedOutputStream.close();
    }
}
