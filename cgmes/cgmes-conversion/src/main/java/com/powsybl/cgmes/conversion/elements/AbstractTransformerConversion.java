/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.conversion.elements.TapChangerConversion.StepAdder;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractTransformerConversion
    extends AbstractConductingEquipmentConversion {

    private enum TapChangerType {
        NULL, FIXED, NON_REGULATING, REGULATING
    }

    public AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

    // propertyBag
    protected static PropertyBag getTransformerTapChanger(PropertyBag end, String id,
        Map<String, PropertyBag> powerTransformerTapChanger) {

        String tcId = end.getId(id);
        return powerTransformerTapChanger.get(tcId);
    }

    // Ratio Tap Changer
    protected TapChangerConversion getRatioTapChanger(PropertyBag ratioTapChanger) {
        if (ratioTapChanger == null) {
            return null;
        }
        TapChangerConversion tapChanger = new TapChangerConversion();
        int lowStep = ratioTapChanger.asInt(CgmesNames.LOW_STEP);
        int highStep = ratioTapChanger.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = ratioTapChanger.asInt(CgmesNames.NEUTRAL_STEP);
        int normalStep = ratioTapChanger.asInt(CgmesNames.NORMAL_STEP, neutralStep);
        int position = getTapPosition(ratioTapChanger, normalStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition(position);

        boolean ltcFlag = ratioTapChanger.asBoolean(CgmesNames.LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addRatioRegulationData(ratioTapChanger, tapChanger);

        addRatioSteps(ratioTapChanger, tapChanger);
        return tapChanger;
    }

    private int getTapPosition(PropertyBag tapChanger, int defaultStep) {
        switch (context.config().getProfileUsedForInitialStateValues()) {
            case SSH:
                return fromContinuous(tapChanger.asDouble(CgmesNames.STEP, tapChanger.asDouble(CgmesNames.SV_TAP_STEP, defaultStep)));
            case SV:
                return fromContinuous(tapChanger.asDouble(CgmesNames.SV_TAP_STEP, tapChanger.asDouble(CgmesNames.STEP, defaultStep)));
            default:
                throw new CgmesModelException("Unexpected profile used for initial flows values: " + context.config().getProfileUsedForInitialStateValues());
        }
    }

    private void addRatioRegulationData(PropertyBag ratioTapChanger, TapChangerConversion tapChanger) {
        String regulatingControlId = context.regulatingControlMapping().forTransformers().getRegulatingControlId(ratioTapChanger);
        tapChanger.setId(ratioTapChanger.getId(CgmesNames.RATIO_TAP_CHANGER))
            .setRegulating(context.regulatingControlMapping().forTransformers().getRegulating(regulatingControlId))
            .setRegulatingControlId(regulatingControlId)
            .setTculControlMode(ratioTapChanger.get(CgmesNames.TCUL_CONTROL_MODE))
            .setTapChangerControlEnabled(ratioTapChanger.asBoolean(CgmesNames.TAP_CHANGER_CONTROL_ENABLED, false));
    }

    private void addRatioSteps(PropertyBag ratioTapChanger, TapChangerConversion tapChanger) {
        String tableId = ratioTapChanger.getId(CgmesNames.RATIO_TAP_CHANGER_TABLE);
        if (tableId != null) {
            addTabularRatioSteps(tapChanger, tableId);
        } else {
            addNonTabularRatioSteps(ratioTapChanger, tapChanger);
        }
    }

    private void addTabularRatioSteps(TapChangerConversion tapChanger, String tableId) {
        PropertyBags table = context.ratioTapChangerTable(tableId);
        Comparator<PropertyBag> byStep = Comparator
            .comparingInt((PropertyBag p) -> p.asInt(CgmesNames.STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(CgmesNames.STEP);
            double ratio = fixing(point, CgmesNames.RATIO, 1.0, tableId, step);
            double r = fixing(point, CgmesNames.R, 0, tableId, step);
            double x = fixing(point, CgmesNames.X, 0, tableId, step);
            double g = fixing(point, CgmesNames.G, 0, tableId, step);
            double b = fixing(point, CgmesNames.B, 0, tableId, step);
            tapChanger.beginStep()
                .setRatio(ratio)
                .setR(r)
                .setX(x)
                .setG1(g)
                .setB1(b)
                .endStep();
        }
    }

    private static void addNonTabularRatioSteps(PropertyBag ratioTapChanger, TapChangerConversion tapChanger) {
        double stepVoltageIncrement = ratioTapChanger.asDouble(CgmesNames.STEP_VOLTAGE_INCREMENT);
        int highStep = ratioTapChanger.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = ratioTapChanger.asInt(CgmesNames.NEUTRAL_STEP);
        for (int step = tapChanger.getLowTapPosition(); step <= highStep; step++) {
            double ratio = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0);
            tapChanger.beginStep()
                .setRatio(ratio)
                .endStep();
        }
    }

    private double fixing(PropertyBag point, String attr, double defaultValue, String tableId,
        int step) {
        double value = point.asDouble(attr, defaultValue);
        if (Double.isNaN(value)) {
            fixed(
                "RatioTapChangerTablePoint " + attr + " for step " + step + " in table "
                    + tableId,
                "invalid value " + point.get(attr));
            return defaultValue;
        }
        return value;
    }

    // Phase Tap Changer
    protected TapChangerConversion getPhaseTapChanger(PropertyBag phaseTapChanger, double xtx) {
        if (phaseTapChanger == null) {
            return null;
        }
        if (!validPhaseTapChangerType(phaseTapChanger)) {
            String type = phaseTapChanger.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
            invalid(String.format("Unexpected phaseTapChangerType %s", type));
            return null;
        }
        TapChangerConversion tapChanger = new TapChangerConversion();
        int lowStep = phaseTapChanger.asInt(CgmesNames.LOW_STEP);
        int highStep = phaseTapChanger.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(CgmesNames.NEUTRAL_STEP);
        int normalStep = phaseTapChanger.asInt(CgmesNames.NORMAL_STEP, neutralStep);
        int position = getTapPosition(phaseTapChanger, normalStep);
        if (position > highStep || position < lowStep) {
            position = neutralStep;
        }
        tapChanger.setLowTapPosition(lowStep).setTapPosition(position);

        boolean ltcFlag = phaseTapChanger.asBoolean(CgmesNames.LTC_FLAG, false);
        tapChanger.setLtcFlag(ltcFlag);

        addPhaseRegulationData(phaseTapChanger, tapChanger);

        addPhaseSteps(phaseTapChanger, tapChanger, xtx);
        return tapChanger;
    }

    private static boolean validPhaseTapChangerType(PropertyBag phaseTapChanger) {
        String tableId = phaseTapChanger.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String type = phaseTapChanger.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
        return isTabular(type, tableId) || isSymmetrical(type) || isAsymmetrical(type);
    }

    private void addPhaseRegulationData(PropertyBag phaseTapChanger, TapChangerConversion tapChanger) {
        String regulatingControlId = context.regulatingControlMapping().forTransformers().getRegulatingControlId(phaseTapChanger);
        tapChanger.setId(phaseTapChanger.getId(CgmesNames.PHASE_TAP_CHANGER))
            .setRegulating(context.regulatingControlMapping().forTransformers().getRegulating(regulatingControlId))
            .setRegulatingControlId(regulatingControlId)
            .setTapChangerControlEnabled(phaseTapChanger.asBoolean(CgmesNames.TAP_CHANGER_CONTROL_ENABLED, false));
    }

    private void addPhaseSteps(PropertyBag phaseTapChanger, TapChangerConversion tapChanger, double xtx) {
        String tableId = phaseTapChanger.getId(CgmesNames.PHASE_TAP_CHANGER_TABLE);
        String phaseTapChangerType = phaseTapChanger.getLocal(CgmesNames.PHASE_TAP_CHANGER_TYPE).toLowerCase();
        if (isTabular(phaseTapChangerType, tableId)) {
            addTabularPhaseSteps(tapChanger, tableId);
        } else if (isAsymmetrical(phaseTapChangerType)) {
            addAsymmetricalPhaseSteps(phaseTapChanger, tapChanger, xtx);
        } else if (isSymmetrical(phaseTapChangerType)) {
            addSymmetricalPhaseSteps(phaseTapChanger, tapChanger, xtx);
        } else {
            getPhaseDefaultStep(tapChanger);
        }
    }

    private void addTabularPhaseSteps(TapChangerConversion tapChanger, String tableId) {
        PropertyBags table = context.phaseTapChangerTable(tableId);
        if (table == null) {
            return;
        }
        Comparator<PropertyBag> byStep = Comparator
            .comparingInt((PropertyBag p) -> p.asInt(CgmesNames.STEP));
        table.sort(byStep);
        for (PropertyBag point : table) {
            int step = point.asInt(CgmesNames.STEP);
            double angle = fixing(point, CgmesNames.ANGLE, 0.0, tableId, step);
            double ratio = fixing(point, CgmesNames.RATIO, 1.0, tableId, step);
            double r = fixing(point, CgmesNames.R, 0, tableId, step);
            double x = fixing(point, CgmesNames.X, 0, tableId, step);
            double g = fixing(point, CgmesNames.G, 0, tableId, step);
            double b = fixing(point, CgmesNames.B, 0, tableId, step);
            tapChanger.beginStep()
                .setAngle(angle)
                .setRatio(ratio)
                .setR(r)
                .setX(x)
                .setG1(g)
                .setB1(b)
                .endStep();
        }
    }

    private static void addAsymmetricalPhaseSteps(PropertyBag phaseTapChanger, TapChangerConversion tapChanger, double xtx) {
        int lowStep = phaseTapChanger.asInt(CgmesNames.LOW_STEP);
        int highStep = phaseTapChanger.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(CgmesNames.NEUTRAL_STEP);
        double stepVoltageIncrement = phaseTapChanger.asDouble(CgmesNames.VOLTAGE_STEP_INCREMENT);
        double windingConnectionAngle = phaseTapChanger.asDouble(CgmesNames.WINDING_CONNECTION_ANGLE);
        for (int step = lowStep; step <= highStep; step++) {
            double dx = 1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0)
                * Math.cos(Math.toRadians(windingConnectionAngle));
            double dy = (step - neutralStep) * (stepVoltageIncrement / 100.0)
                * Math.sin(Math.toRadians(windingConnectionAngle));
            double ratio = Math.hypot(dx, dy);
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            tapChanger.beginStep()
                .setAngle(angle)
                .setRatio(ratio)
                .endStep();
        }
        double xMin = phaseTapChanger.asDouble(CgmesNames.X_STEP_MIN, phaseTapChanger.asDouble(CgmesNames.X_MIN));
        double xMax = phaseTapChanger.asDouble(CgmesNames.X_STEP_MAX, phaseTapChanger.asDouble(CgmesNames.X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(step -> step.getAngle())
            .mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        tapChanger.getSteps().forEach(step -> {
            double alpha = step.getAngle();
            double x = getStepXforAsymmetrical(xMin, xMax, alpha, alphaMax, windingConnectionAngle);
            step.setX((x - xtx) / xtx * 100);
        });
    }

    private static double getStepXforAsymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
        double alphaMaxDegrees, double thetaDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        double theta = Math.toRadians(thetaDegrees);
        double numer = Math.sin(theta) - Math.tan(alphaMax) * Math.cos(theta);
        double denom = Math.sin(theta) - Math.tan(alpha) * Math.cos(theta);
        return xStepMin + (xStepMax - xStepMin)
            * Math.pow(Math.tan(alpha) / Math.tan(alphaMax) * numer / denom, 2);
    }

    private static void addSymmetricalPhaseSteps(PropertyBag phaseTapChanger, TapChangerConversion tapChanger, double xtx) {
        int lowStep = phaseTapChanger.asInt(CgmesNames.LOW_STEP);
        int highStep = phaseTapChanger.asInt(CgmesNames.HIGH_STEP);
        int neutralStep = phaseTapChanger.asInt(CgmesNames.NEUTRAL_STEP);
        double stepVoltageIncrement = phaseTapChanger.asDouble(CgmesNames.VOLTAGE_STEP_INCREMENT);
        double stepPhaseShiftIncrement = phaseTapChanger.asDouble(CgmesNames.STEP_PHASE_SHIFT_INCREMENT);
        for (int step = lowStep; step <= highStep; step++) {
            double angle;
            if (!Double.isNaN(stepPhaseShiftIncrement) && stepPhaseShiftIncrement != 0.0) {
                angle = (step - neutralStep) * stepPhaseShiftIncrement;
            } else {
                double dy = (step - neutralStep) * (stepVoltageIncrement / 100.0);
                angle = Math.toDegrees(2 * Math.asin(dy / 2));
            }
            tapChanger.beginStep()
                .setRatio(1.0)
                .setAngle(angle)
                .endStep();
        }
        double xMin = phaseTapChanger.asDouble(CgmesNames.X_STEP_MIN, phaseTapChanger.asDouble(CgmesNames.X_MIN));
        double xMax = phaseTapChanger.asDouble(CgmesNames.X_STEP_MAX, phaseTapChanger.asDouble(CgmesNames.X_MAX));
        if (Double.isNaN(xMin) || Double.isNaN(xMax) || xMin < 0 || xMax <= 0 || xMin > xMax) {
            return;
        }
        double alphaMax = tapChanger.getSteps().stream().map(step -> step.getAngle())
            .mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        tapChanger.getSteps().forEach(step -> {
            double alpha = step.getAngle();
            double x = getStepXforSymmetrical(xMin, xMax, alpha, alphaMax);
            step.setX(100 * (x - xtx) / xtx);
        });
    }

    private static double getStepXforSymmetrical(double xStepMin, double xStepMax, double alphaDegrees,
        double alphaMaxDegrees) {
        double alpha = Math.toRadians(alphaDegrees);
        double alphaMax = Math.toRadians(alphaMaxDegrees);
        return xStepMin
            + (xStepMax - xStepMin) * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private static void getPhaseDefaultStep(TapChangerConversion tapChanger) {
        tapChanger.beginStep()
            .endStep();
    }

    private static boolean isTabular(String tapChangerType, String tableId) {
        return tableId != null && tapChangerType != null && tapChangerType.endsWith(CgmesNames.TABULAR);
    }

    private static boolean isSymmetrical(String tapChangerType) {
        return tapChangerType != null && !tapChangerType.endsWith(CgmesNames.ASYMMETRICAL) && tapChangerType.endsWith(CgmesNames.SYMMETRICAL);
    }

    private static boolean isAsymmetrical(String tapChangerType) {
        return tapChangerType != null && tapChangerType.endsWith(CgmesNames.ASYMMETRICAL);
    }

    // combine two tap changers
    protected TapChangerConversion combineTapChangers(TapChangerConversion tc1, TapChangerConversion tc2) {
        switch (tapChangerType(tc1)) {
            case NULL:
                return combineTapChangerNull(tc2);
            case FIXED:
                return combineTapChangerFixed(tc1, tc2);
            case NON_REGULATING:
                return combineTapChangerNonRegulating(tc1, tc2);
            case REGULATING:
                return combineTapChangerRegulating(tc1, tc2);
        }
        return null;
    }

    private static TapChangerConversion combineTapChangerNull(TapChangerConversion tc) {
        switch (tapChangerType(tc)) {
            case NULL:
                return null;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return tc;
        }
        return null;
    }

    private static TapChangerConversion combineTapChangerFixed(TapChangerConversion fixTc, TapChangerConversion tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return fixTc;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return combineTapChanger(tc2, fixTc);
        }
        return null;
    }

    private TapChangerConversion combineTapChangerNonRegulating(TapChangerConversion tcNonRegulating, TapChangerConversion tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return tcNonRegulating;
            case FIXED:
                return combineTapChanger(tcNonRegulating, tc2);
            case NON_REGULATING:
                TapChangerConversion ntc2 = tapChangerFixPosition(tc2);
                return combineTapChanger(tcNonRegulating, ntc2);
            case REGULATING:
                TapChangerConversion ntcNonRegulating = tapChangerFixPosition(tcNonRegulating);
                return combineTapChanger(tc2, ntcNonRegulating);
        }
        return null;
    }

    private TapChangerConversion combineTapChangerRegulating(TapChangerConversion tcRegulating, TapChangerConversion tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return tcRegulating;
            case FIXED:
                return combineTapChanger(tcRegulating, tc2);
            case NON_REGULATING:
            case REGULATING:
                TapChangerConversion ntc2 = tapChangerFixPosition(tc2);
                return combineTapChanger(tcRegulating, ntc2);
        }
        return null;
    }

    private static TapChangerConversion combineTapChanger(TapChangerConversion tc1, TapChangerConversion tc2) {
        TapChangerConversion tapChanger = baseCloneTapChanger(tc1);
        combineTapChangerFixTapChangerSteps(tapChanger, tc1, tc2);
        return tapChanger;
    }

    private TapChangerConversion tapChangerFixPosition(TapChangerConversion tc) {
        if (tc.getLowTapPosition() != tc.getHighTapPosition()) {
            fixed(String.format("TapChanger Id %s fixed tap at position %d ", tc.getId(), tc.getTapPosition()), "");
        }
        TapChangerConversion tapChanger = baseCloneTapChanger(tc);
        tapChanger.setLowTapPosition(tapChanger.getTapPosition());
        StepAdder step = getFixStepTapChanger(tc);
        double ratio = step.getRatio();
        double angle = step.getAngle();
        double r = step.getR();
        double x = step.getX();
        double g1 = step.getG1();
        double b1 = step.getB1();
        double g2 = step.getG2();
        double b2 = step.getB2();
        tapChanger.beginStep()
            .setRatio(ratio)
            .setAngle(angle)
            .setR(r)
            .setX(x)
            .setG1(g1)
            .setB1(b1)
            .setG2(g2)
            .setB2(b2)
            .endStep();
        return tapChanger;
    }

    private static void combineTapChangerFixTapChangerSteps(TapChangerConversion tapChanger, TapChangerConversion tc1, TapChangerConversion fixTc) {
        StepAdder stepFixed = getFixStepTapChanger(fixTc);
        double ratioFixed = stepFixed.getRatio();
        double angleFixed = stepFixed.getAngle();
        double rFixed = stepFixed.getR();
        double xFixed = stepFixed.getX();
        double g1Fixed = stepFixed.getG1();
        double b1Fixed = stepFixed.getB1();
        double g2Fixed = stepFixed.getG2();
        double b2Fixed = stepFixed.getB2();
        Complex aFixed = new Complex(ratioFixed * Math.cos(Math.toRadians(angleFixed)),
            ratioFixed * Math.sin(Math.toRadians(angleFixed)));
        tc1.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double g1 = step.getG1();
            double b1 = step.getB1();
            double g2 = step.getG2();
            double b2 = step.getB2();
            Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)),
                ratio * Math.sin(Math.toRadians(angle)));
            Complex na = a.multiply(aFixed);
            tapChanger.beginStep()
                .setRatio(na.abs())
                .setAngle(Math.toDegrees(na.getArgument()))
                .setR(combineTapChangerCorrection(rFixed, r))
                .setX(combineTapChangerCorrection(xFixed, x))
                .setG1(combineTapChangerCorrection(g1Fixed, g1))
                .setB1(combineTapChangerCorrection(b1Fixed, b1))
                .setG2(combineTapChangerCorrection(g2Fixed, g2))
                .setB2(combineTapChangerCorrection(b2Fixed, b2))
                .endStep();
        });
    }

    private static double combineTapChangerCorrection(double fixedCorrection, double correction) {
        if (fixedCorrection != 0.0 && correction != 0.0) {
            return fixedCorrection * correction;
        } else if (fixedCorrection != 0.0) {
            return fixedCorrection;
        } else if (correction != 0.0) {
            return correction;
        } else {
            return 0.0;
        }
    }

    // Move tapChanger
    protected static TapChangerConversion  moveTapChangerFrom1To2(TapChangerConversion tc) {
        return moveTapChangerFromOneEndToTheOther(tc);
    }

    protected static TapChangerConversion moveTapChangerFrom2To1(TapChangerConversion tc) {
        return moveTapChangerFromOneEndToTheOther(tc);
    }

    private static TapChangerConversion moveTapChangerFromOneEndToTheOther(TapChangerConversion tc) {
        switch (tapChangerType(tc)) {
            case NULL:
                return null;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return moveTapChanger(tc);
        }
        return null;
    }

    private static TapChangerConversion moveTapChanger(TapChangerConversion tc) {
        TapChangerConversion tapChanger = baseCloneTapChanger(tc);
        moveTapChangerSteps(tapChanger, tc);
        return tapChanger;
    }

    private static void moveTapChangerSteps(TapChangerConversion tapChanger, TapChangerConversion tc) {
        tc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double g1 = step.getG1();
            double b1 = step.getB1();
            double g2 = step.getG2();
            double b2 = step.getB2();
            TapChangerStepConversion convertedStep = calculateConversionStep(ratio, angle, r, x, g1, b1, g2, b2);
            tapChanger.beginStep()
                .setRatio(convertedStep.ratio)
                .setAngle(convertedStep.angle)
                .setR(convertedStep.r)
                .setX(convertedStep.x)
                .setG1(convertedStep.g1)
                .setB1(convertedStep.b1)
                .setG2(convertedStep.g2)
                .setB2(convertedStep.b2)
                .endStep();
        });
    }

    private static TapChangerStepConversion calculateConversionStep(double ratio, double angle, double r,
        double x, double g1, double b1, double g2, double b2) {
        TapChangerStepConversion step = new TapChangerStepConversion();
        Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)),
            ratio * Math.sin(Math.toRadians(angle)));
        Complex na = a.reciprocal();
        step.ratio = na.abs();
        step.angle = Math.toDegrees(na.getArgument());
        step.r = 100 * (impedanceConversion(1 + r / 100, a) - 1);
        step.x = 100 * (impedanceConversion(1 + x / 100, a) - 1);
        step.g1 = 100 * (admittanceConversion(1 + g1 / 100, a) - 1);
        step.b1 = 100 * (admittanceConversion(1 + b1 / 100, a) - 1);
        step.g2 = 100 * (admittanceConversion(1 + g2 / 100, a) - 1);
        step.b2 = 100 * (admittanceConversion(1 + b2 / 100, a) - 1);
        return step;
    }

    protected static RatioConversion identityRatioConversion(double r, double x, double g1,
        double b1, double g2, double b2) {
        RatioConversion ratio = new RatioConversion();
        ratio.r = r;
        ratio.x = x;
        ratio.g1 = g1;
        ratio.b1 = b1;
        ratio.g2 = g2;
        ratio.b2 = b2;
        return ratio;
    }

    protected static RatioConversion moveRatioFrom2To1(double a0, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        return moveRatio(a0, angle, r, x, g1, b1, g2, b2);
    }

    protected static RatioConversion moveRatioFrom1To2(double a0, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        return moveRatio(a0, angle, r, x, g1, b1, g2, b2);
    }

    private static RatioConversion moveRatio(double a0, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        RatioConversion ratio = new RatioConversion();
        Complex a = new Complex(a0 * Math.cos(Math.toRadians(angle)),
            a0 * Math.sin(Math.toRadians(angle)));
        ratio.r = impedanceConversion(r, a);
        ratio.x = impedanceConversion(x, a);
        ratio.g1 = admittanceConversion(g1, a);
        ratio.b1 = admittanceConversion(b1, a);
        ratio.g2 = admittanceConversion(g2, a);
        ratio.b2 = admittanceConversion(b2, a);
        return ratio;
    }

    private static double admittanceConversion(double correction, Complex a) {
        double a2 = a.abs() * a.abs();
        return correction / a2;
    }

    private static double impedanceConversion(double correction, Complex a) {
        double a2 = a.abs() * a.abs();
        return correction * a2;
    }

    private static TapChangerConversion baseCloneTapChanger(TapChangerConversion rtc) {
        TapChangerConversion tapChanger = new TapChangerConversion();
        String id = rtc.getId();
        boolean isLtcFlag = rtc.isLtcFlag();
        boolean isRegulating = rtc.isRegulating();
        String regulatingControlId = rtc.getRegulatingControlId();
        String tculControlMode = rtc.getTculControlMode();
        boolean isTapChangerControlEnabled = rtc.isTapChangerControlEnabled();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        tapChanger.setLowTapPosition(lowStep)
            .setTapPosition(position)
            .setLtcFlag(isLtcFlag)
            .setId(id)
            .setRegulating(isRegulating)
            .setRegulatingControlId(regulatingControlId)
            .setTculControlMode(tculControlMode)
            .setTapChangerControlEnabled(isTapChangerControlEnabled);
        return tapChanger;
    }

    private static StepAdder getFixStepTapChanger(TapChangerConversion tc) {
        if (isTapChangerFixed(tc)) {
            return tc.getSteps().get(0);
        }
        int position = tc.getTapPosition();
        int lowPosition = tc.getLowTapPosition();
        return tc.getSteps().get(position - lowPosition);
    }

    protected static void negatePhaseTapChanger(TapChangerConversion tc) {
        if (tc == null) {
            return;
        }
        tc.getSteps().forEach(step -> {
            double angle = step.getAngle();
            step.setAngle(-angle);
        });
    }

    private static TapChangerType tapChangerType(TapChangerConversion tc) {
        if (isTapChangerNull(tc)) {
            return TapChangerType.NULL;
        }
        if (isTapChangerFixed(tc)) {
            return TapChangerType.FIXED;
        }
        if (!isTapChangerRegulating(tc)) {
            return TapChangerType.NON_REGULATING;
        } else {
            return TapChangerType.REGULATING;
        }
    }

    private static boolean isTapChangerNull(TapChangerConversion tc) {
        return tc == null;
    }

    private static boolean isTapChangerFixed(TapChangerConversion tc) {
        return tc.getSteps().size() == 1;
    }

    private static boolean isTapChangerRegulating(TapChangerConversion tc) {
        return tc.isRegulating();
    }

    protected static void setToIidmRatioTapChanger(TapChangerConversion rtc, RatioTapChangerAdder rtca) {
        boolean isLtcFlag = rtc.isLtcFlag();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        rtca.setLoadTapChangingCapabilities(isLtcFlag).setLowTapPosition(lowStep).setTapPosition(position);

        rtc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double r = step.getR();
            double x = step.getX();
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            rtca.beginStep()
                .setRho(1 / ratio)
                .setR(r)
                .setX(x)
                .setB(b1)
                .setG(g1)
                .endStep();
        });
        rtca.add();
    }

    protected static void setToIidmPhaseTapChanger(TapChangerConversion ptc, PhaseTapChangerAdder ptca) {
        int lowStep = ptc.getLowTapPosition();
        int position = ptc.getTapPosition();
        ptca.setLowTapPosition(lowStep).setTapPosition(position);

        ptc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double b1 = step.getB1();
            double g1 = step.getG1();
            // double b2 = step.getB2();
            // double g2 = step.getG2();
            // Only b1 and g1 instead of b1 + b2 and g1 + g2
            ptca.beginStep()
                .setRho(1 / ratio)
                .setAlpha(-angle)
                .setR(r)
                .setX(x)
                .setB(b1)
                .setG(g1)
                .endStep();
        });
        ptca.add();
    }

    protected CgmesRegulatingControlRatio setContextRegulatingDataRatio(TapChangerConversion tc) {
        CgmesRegulatingControlRatio rcRtc = null;
        if (tc != null) {
            rcRtc = context.regulatingControlMapping().forTransformers().buildRegulatingControlRatio(tc.getId(),
                tc.getRegulatingControlId(), tc.getTculControlMode(), tc.isTapChangerControlEnabled());
        }
        return rcRtc;
    }

    protected CgmesRegulatingControlPhase setContextRegulatingDataPhase(TapChangerConversion tc) {
        CgmesRegulatingControlPhase rcPtc = null;
        if (tc != null) {
            return context.regulatingControlMapping().forTransformers().buildRegulatingControlPhase(
                tc.getRegulatingControlId(), tc.isTapChangerControlEnabled(), tc.isLtcFlag());
        }
        return rcPtc;
    }

    static class TapChangerStepConversion {
        double ratio;
        double angle;
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class RatioConversion {
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }

    protected static class ConvertedEnd1 {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        String terminal;
    }

    static class AllTapChanger {
        TapChangerConversion ratioTapChanger1;
        TapChangerConversion phaseTapChanger1;
        TapChangerConversion ratioTapChanger2;
        TapChangerConversion phaseTapChanger2;
    }

    static class AllShunt {
        double g1;
        double b1;
        double g2;
        double b2;
    }
}

