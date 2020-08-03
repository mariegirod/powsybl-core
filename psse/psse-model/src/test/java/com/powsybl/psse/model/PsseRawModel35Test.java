/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseRawModel35Test {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void loadsTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseLoad> list = new ArrayList<>();
        list.add(new PsseLoad35());
        list.add(new PsseLoad35());

        model.addLoads(list);
        assertEquals(2, model.getLoads().size());
    }

    @Test
    public void loadsExceptionTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseLoad> list = new ArrayList<>();
        list.add(new PsseLoad35());
        list.add(new PsseLoad());

        exception.expect(PsseException.class);
        exception.expectMessage("PsseRawModel35. Unexpected instanceof load");
        model.addLoads(list);
    }

    @Test
    public void generatorsTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseGenerator> list = new ArrayList<>();
        list.add(new PsseGenerator35());
        list.add(new PsseGenerator35());

        model.addGenerators(list);
        assertEquals(2, model.getGenerators().size());
    }

    @Test
    public void generatorsExceptionTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseGenerator> list = new ArrayList<>();
        list.add(new PsseGenerator());
        list.add(new PsseGenerator35());

        exception.expect(PsseException.class);
        exception.expectMessage("PsseRawModel35. Unexpected instanceof generator");
        model.addGenerators(list);
    }

    @Test
    public void nonTransformerBranchesTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseNonTransformerBranch> list = new ArrayList<>();
        list.add(new PsseNonTransformerBranch35());
        list.add(new PsseNonTransformerBranch35());

        model.addNonTransformerBranches(list);
        assertEquals(2, model.getNonTransformerBranches().size());
    }

    @Test
    public void nonTransformerBranchesExceptionTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseNonTransformerBranch> list = new ArrayList<>();
        list.add(new PsseNonTransformerBranch());
        list.add(new PsseNonTransformerBranch());

        exception.expect(PsseException.class);
        exception.expectMessage("PsseRawModel35. Unexpected instanceof nonTransformerBranch");
        model.addNonTransformerBranches(list);
    }

    @Test
    public void transformersTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseTransformer> list = new ArrayList<>();
        list.add(new PsseTransformer35());
        list.add(new PsseTransformer35());

        model.addTransformers(list);
        assertEquals(2, model.getTransformers().size());
    }

    @Test
    public void transformersExceptionTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseTransformer> list = new ArrayList<>();
        list.add(new PsseTransformer35());
        list.add(new PsseTransformer());

        exception.expect(PsseException.class);
        exception.expectMessage("PsseRawModel35. Unexpected instanceof transformer");
        model.addTransformers(list);
    }

    @Test
    public void switchedShuntsTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseSwitchedShunt> list = new ArrayList<>();
        list.add(new PsseSwitchedShunt35());
        list.add(new PsseSwitchedShunt35());

        model.addSwitchedShunts(list);
        assertEquals(2, model.getSwitchedShunts().size());
    }

    @Test
    public void switchedShuntsExceptionTest() {

        PsseRawModel35 model = new PsseRawModel35(new PsseCaseIdentification());
        List<PsseSwitchedShunt> list = new ArrayList<>();
        list.add(new PsseSwitchedShunt35());
        list.add(new PsseSwitchedShunt());

        exception.expect(PsseException.class);
        exception.expectMessage("PsseRawModel35. Unexpected instanceof switchedShunt");
        model.addSwitchedShunts(list);
    }
}
