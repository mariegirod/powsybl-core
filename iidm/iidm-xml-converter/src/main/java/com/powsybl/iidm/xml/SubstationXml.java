/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationXml extends AbstractIdentifiableXml<Substation, SubstationAdder, Network> {

    static final SubstationXml INSTANCE = new SubstationXml();

    static final String ROOT_ELEMENT_NAME = "substation";

    private static final String COUNTRY = "country";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(Substation s) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(Substation s, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        Optional<Country> country = s.getCountry();
        if (country.isPresent()) {
            context.getWriter().writeAttribute(COUNTRY, context.getAnonymizer().anonymizeCountry(country.get()).toString());
        }
        if (s.getTso() != null) {
            context.getWriter().writeAttribute("tso", context.getAnonymizer().anonymizeString(s.getTso()));
        }
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_3, context, () -> {
            if (!s.getGeographicalTags().isEmpty()) {
                try {
                    context.getWriter().writeAttribute(Substation.GEOGRAPHICAL_TAGS_KEY, s.getGeographicalTags().stream()
                            .map(tag -> context.getAnonymizer().anonymizeString(tag))
                            .collect(Collectors.joining(",")));
                } catch (XMLStreamException e) {
                    throw new UncheckedXmlStreamException(e);
                }
            }
        });
    }

    @Override
    protected void writeSubElements(Substation s, Network n, NetworkXmlWriterContext context) throws XMLStreamException {
        for (VoltageLevel vl : s.getVoltageLevels()) {
            VoltageLevelXml.INSTANCE.write(vl, null, context);
        }
        Iterable<TwoWindingsTransformer> twts = s.getTwoWindingsTransformers();
        for (TwoWindingsTransformer twt : twts) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            TwoWindingsTransformerXml.INSTANCE.write(twt, null, context);
        }
        Iterable<ThreeWindingsTransformer> twts2 = s.getThreeWindingsTransformers();
        for (ThreeWindingsTransformer twt : twts2) {
            if (!context.getFilter().test(twt)) {
                continue;
            }
            ThreeWindingsTransformerXml.INSTANCE.write(twt, null, context);
        }
    }

    @Override
    protected SubstationAdder createAdder(Network network) {
        return network.newSubstation();
    }

    @Override
    protected Substation readRootElementAttributes(SubstationAdder adder, NetworkXmlReaderContext context) {

        Country country = Optional.ofNullable(context.getReader().getAttributeValue(null, COUNTRY))
                .map(c -> context.getAnonymizer().deanonymizeCountry(Country.valueOf(c)))
                .orElse(null);
        String tso = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "tso"));
        String geographicalTags = context.getReader().getAttributeValue(null, "geographicalTags");
        if (geographicalTags != null) {
            adder.setGeographicalTags(Arrays.stream(geographicalTags.split(","))
                    .map(tag -> context.getAnonymizer().deanonymizeString(tag))
                    .toArray(size -> new String[size]));
        }
        return adder.setCountry(country)
                .setTso(tso)
                .add();
    }

    @Override
    protected void readSubElements(Substation s, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case VoltageLevelXml.ROOT_ELEMENT_NAME:
                    VoltageLevelXml.INSTANCE.read(s, context);
                    break;

                case TwoWindingsTransformerXml.ROOT_ELEMENT_NAME:
                    TwoWindingsTransformerXml.INSTANCE.read(s, context);
                    break;

                case ThreeWindingsTransformerXml.ROOT_ELEMENT_NAME:
                    ThreeWindingsTransformerXml.INSTANCE.read(s, context);
                    break;

                default:
                    super.readSubElements(s, context);
            }
        });
    }
}
