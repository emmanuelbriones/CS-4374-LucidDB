/*
// $Id$
// Package org.eigenbase is a class library of data management components.
// Copyright (C) 2006 The Eigenbase Project
// Copyright (C) 2006 SQLstream, Inc.
// Copyright (C) 2006 Dynamo BI Corporation
// Portions Copyright (C) 2006 John V. Sichi
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.eigenbase.util;

/**
 * Holder for a list of constants describing which bugs which have not been
 * fixed.
 *
 * <p>You can use these constants to control the flow of your code. For example,
 * suppose that bug FNL-123 causes the "INSERT" statement to return an incorrect
 * row-count, and you want to disable unit tests. You might use the constant in
 * your code as follows:
 *
 * <blockquote>
 * <pre>Statement stmt = connection.createStatement();
 * int rowCount = stmt.execute(
 *     "INSERT INTO FemaleEmps SELECT * FROM Emps WHERE gender = 'F'");
 * if (Bug.Fnl123Fixed) {
 *    assertEquals(rowCount, 5);
 * }</pre>
 * </blockquote>
 *
 * <p>The usage of the constant is a convenient way to identify the impact of
 * the bug. When someone fixes the bug, they will remove the constant and all
 * usages of it. Also, the constant helps track the propagation of the fix: as
 * the fix is integrated into other branches, the constant will be removed from
 * those branches.</p>
 *
 * @author jhyde
 * @version $Id$
 * @since 2006/3/2
 */
public abstract class Bug
{
    //~ Static fields/initializers ---------------------------------------------

    // -----------------------------------------------------------------------
    // Developers should create new fields here, in their own section. This
    // will make merge conflicts much less likely than if everyone is
    // appending.

    public static final boolean Dt239Fixed = false;

    /**
     * Window Rank functions are supported through the validator but not
     * implenmented by calculator. Disable tests and modified SqlRankFunction to
     * return "Unknown Function".
     */
    public static final boolean Dt561Fixed = false;

    public static final boolean Dt591Fixed = false;

    public static final boolean Dt785Fixed = false;

    // angel

    /**
     * Whether dtbug1446 "Window Rank Functions not fully implemented" is fixed.
     */
    public static final boolean Dt1446Fixed = false;

    // jhyde

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FNL-3">issue
     * Fnl-3</a> is fixed.
     */
    public static final boolean Fnl3Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FNL-77">issue FNL-77:
     * Fennel calc returns CURRENT_TIMESTAMP in UTC, should be local time</a> is
     * fixed.
     */
    public static final boolean Fnl77Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-327">issue
     * FRG-327: AssertionError while translating IN list that contains null</a>
     * is fixed.
     */
    public static final boolean Frg327Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-377">issue
     * FRG-377: Regular character set identifiers defined in SQL:2008 spec like
     * :ALPHA:, * :UPPER:, :LOWER:, ... etc. are not yet implemented in
     * SIMILAR TO expressions.</a> is fixed.
     */
    public static final boolean Frg377Fixed = false;

    /**
     * Whether dtbug1684 "CURRENT_DATE not implemented in fennel calc" is fixed.
     */
    public static final boolean Dt1684Fixed = false;

    /**
     * Whether dtbug1684 "Integration issues" is fixed.
     */
    public static final boolean Dt1847Fixed = false;

    // kkrueger

    // mberkowitz

    // murali

    // rchen

    // schoi

    // stephan

    // tleung

    // xluo

    // zfong

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FNL-25">issue
     * FNL-25</a> is fixed. (also filed as dtbug 153)
     */
    public static final boolean Fnl25Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FNL-54">issue FNL-54:
     * cast time to timestamp should initialize date to current_date</a> is
     * fixed.
     */
    public static final boolean Fnl54Fixed = false;

    // johnk

    // jouellette

    // jpham

    // jvs

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-73">issue FRG-73:
     * miscellaneous bugs with nested comments</a> is fixed.
     */
    public static final boolean Frg73Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-78">issue FRG-78:
     * collation clause should be on expression instead of identifier</a> is
     * fixed.
     */
    public static final boolean Frg78Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-140">issue
     * FRG-140: validator does not accept column qualified by schema name</a> is
     * fixed.
     */
    public static final boolean Frg140Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-187">issue
     * FRG-187: FarragoAutoVmOperatorTest.testOverlapsOperator fails</a> is
     * fixed.
     */
    public static final boolean Frg187Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-189">issue
     * FRG-189: FarragoAutoVmOperatorTest.testSelect fails</a> is fixed.
     */
    public static final boolean Frg189Fixed = false;

    // elin

    // fliang

    // fzhang

    // hersker

    // jack

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-216">issue
     * FRG-216: Java calc 'expression splitting' causes serious problems</a> is
     * fixed.
     */
    public static final boolean Frg216Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-254">issue
     * FRG-254: environment-dependent failure for
     * SqlOperatorTest.testPrefixPlusOperator</a> is fixed.
     */
    public static final boolean Frg254Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-282">issue
     * FRG-282: Support precision in TIME and TIMESTAMP data types</a> is fixed.
     */
    public static final boolean Frg282Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-283">issue
     * FRG-283: Calc cannot cast VARBINARY values</a> is fixed.
     */
    public static final boolean Frg283Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-296">issue
     * FRG-296: SUBSTRING(string FROM regexp FOR regexp)</a> is fixed.
     */
    public static final boolean Frg296Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-375">issue
     * FRG-375: The expression VALUES ('cd' SIMILAR TO '[a-e^c]d') returns TRUE.
     * It should return FALSE.</a> is fixed.
     */
    public static final boolean Frg375Fixed = false;

    /**
     * Whether <a href="http://issues.eigenbase.org/browse/FRG-378">issue
     * FRG-378: Regular expressions in SIMILAR TO predicates
     * potentially dont match SQL:2008 spec in a few cases.</a> is fixed.
     */
    public static final boolean Frg378Fixed = false;
}

// End Bug.java
