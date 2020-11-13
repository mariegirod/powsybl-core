/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.psse.model.PsseCaseIdentification;
import com.powsybl.psse.model.PsseContext;
import com.powsybl.psse.model.PsseRawModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RawData35 extends RawData33 {

    @Override
    public PsseRawModel read(ReadOnlyDataSource dataSource, String ext, PsseContext context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(null, ext)))) {
            PsseCaseIdentification caseIdentification = new CaseIdentificationData().read1(reader, context);
            caseIdentification.validate();

            PsseRawModel model = new PsseRawModel(caseIdentification);
            // TODO System-Wide data
            Util.readDiscardedRecordBlock(reader);
            model.addBuses(new BusData().read(reader, context));
            model.addLoads(new LoadData().read(reader, context));
            model.addFixedShunts(new FixedBusShuntData().read(reader, context));
            model.addGenerators(new GeneratorData().read(reader, context));
            model.addNonTransformerBranches(new NonTransformerBranchData().read(reader, context));
            // TODO System Switching device data
            Util.readDiscardedRecordBlock(reader);
            model.addTransformers(new TransformerData().read(reader, context));
            model.addAreas(new AreaInterchangeData().read(reader, context));
            // TODO 2-terminal DC data
            Util.readDiscardedRecordBlock(reader);
            // TODO voltage source converter data
            Util.readDiscardedRecordBlock(reader);
            // TODO impedance correction data
            Util.readDiscardedRecordBlock(reader);
            // TODO multi-terminal DC data
            Util.readDiscardedRecordBlock(reader);
            // TODO multi-section line data
            Util.readDiscardedRecordBlock(reader);
            model.addZones(new ZoneData().read(reader, context));
            // TODO inter-area transfer data
            Util.readDiscardedRecordBlock(reader);
            model.addOwners(new OwnerData().read(reader, context));
            // TODO facts control device data
            Util.readDiscardedRecordBlock(reader);
            model.addSwitchedShunts(new SwitchedShuntData().read(reader, context));
            // TODO gne device data
            Util.readDiscardedRecordBlock(reader);
            // TODO Induction Machine data
            Util.readDiscardedRecordBlock(reader);
            // TODO Substation data
            Util.readDiscardedRecordBlock(reader);

            return model;
        }
    }
}
