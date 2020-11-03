/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * An equipment with two terminals.
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">R</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series resistance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">X</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series reactance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt susceptance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt susceptance</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Branch<I extends Branch<I>> extends Connectable<I> {

    enum Side {
        ONE,
        TWO
    }

    /**
     * Represents a current overload on a {@link Branch}.
     */
    interface Overload {

        /**
         * The temporary limit under which the current is.
         * In particular, it determines the duration during which
         * the current current value may be sustained.
         */
        CurrentLimits.TemporaryLimit getTemporaryLimit();

        /**
         * The value of the current limit which has been overloaded, in Amperes.
         */
        double getPreviousLimit();

        /**
         * The name of the current limit which has been overloaded.
         */
        String getPreviousLimitName();
    }

    /**
     * Get the first terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the second terminal.
     */
    Terminal getTerminal2();

    Terminal getTerminal(Side side);

    Terminal getTerminal(String voltageLevelId);

    Side getSide(Terminal terminal);

    CurrentLimits getCurrentLimits(Side side);

    CurrentLimits getCurrentLimits1();

    CurrentLimitsAdder newCurrentLimits1();

    CurrentLimits getCurrentLimits2();

    CurrentLimitsAdder newCurrentLimits2();

    boolean isOverloaded();

    boolean isOverloaded(float limitReduction);

    int getOverloadDuration();

    boolean checkPermanentLimit(Side side, float limitReduction);

    boolean checkPermanentLimit(Side side);

    boolean checkPermanentLimit1(float limitReduction);

    boolean checkPermanentLimit1();

    boolean checkPermanentLimit2(float limitReduction);

    boolean checkPermanentLimit2();

    Overload checkTemporaryLimits(Side side, float limitReduction);

    Overload checkTemporaryLimits(Side side);

    Overload checkTemporaryLimits1(float limitReduction);

    Overload checkTemporaryLimits1();

    Overload checkTemporaryLimits2(float limitReduction);

    Overload checkTemporaryLimits2();

    /**
     * Get the active power in MW injected at side one of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getP1() {
        return getTerminal1().getP();
    }

    /**
     * Set the active power in MW injected at side one of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default Branch<I> setP1(double p) {
        getTerminal1().setP(p);
        return this;
    }

    /**
     * Get the reactive power in MVAR injected at side one of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getQ1() {
        return getTerminal1().getQ();
    }

    /**
     * Set the reactive power in MVAR injected at side one of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default Branch<I> setQ1(double q) {
        getTerminal1().setQ(q);
        return this;
    }

    /**
     * Get the current in A at side one of this equipment.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    default double getI1() {
        return getTerminal1().getI();
    }

    /**
     * Try to connect this equipment at side one.
     * <p>Depends on the working variant.
     * @return true if the side one terminal has been connected, false otherwise
     * @see VariantManager
     */
    default boolean connect1() {
        return getTerminal1().connect();
    }

    /**
     * Disconnect this equipment at side one.
     * <p>Depends on the working variant.
     * @return true if the side one terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    default boolean disconnect1() {
        return getTerminal1().disconnect();
    }

    /**
     * Test if this equipment is connected at side one.
     * @return true if the side one terminal is connected, false otherwise
     */
    default boolean isConnected1() {
        return getTerminal1().isConnected();
    }

    /**
     * Get the active power in MW injected at side two of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getP2() {
        return getTerminal2().getP();
    }

    /**
     * Set the active power in MW injected at side two of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default Branch<I> setP2(double p) {
        getTerminal2().setP(p);
        return this;
    }

    /**
     * Get the reactive power in MVAR injected at side two of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getQ2() {
        return getTerminal2().getQ();
    }

    /**
     * Set the reactive power in MVAR injected at side two of this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default Branch<I> setQ2(double q) {
        getTerminal2().setQ(q);
        return this;
    }

    /**
     * Get the current in A at side two of this equipment.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    default double getI2() {
        return getTerminal2().getI();
    }

    /**
     * Try to connect this equipment at side two.
     * <p>Depends on the working variant.
     * @return true if the side two terminal has been connected, false otherwise
     * @see VariantManager
     */
    default boolean connect2() {
        return getTerminal2().connect();
    }

    /**
     * Disconnect this equipment at side two.
     * <p>Depends on the working variant.
     * @return true if the side two terminal has been disconnected, false otherwise
     * @see VariantManager
     */
    default boolean disconnect2() {
        return getTerminal2().disconnect();
    }

    /**
     * Test if this equipment is connected at side two.
     * @return true if the side two terminal is connected, false otherwise
     */
    default boolean isConnected2() {
        return getTerminal2().isConnected();
    }
}
