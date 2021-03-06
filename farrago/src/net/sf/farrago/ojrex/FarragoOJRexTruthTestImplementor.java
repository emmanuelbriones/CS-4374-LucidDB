/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2005 SQLstream, Inc.
// Copyright (C) 2005 Dynamo BI Corporation
// Portions Copyright (C) 2003 John V. Sichi
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
package net.sf.farrago.ojrex;

import net.sf.farrago.type.runtime.*;

import openjava.ptree.*;

import org.eigenbase.rex.*;


/**
 * FarragoOJRexTruthTestImplementor implements Farrago specifics of {@link
 * org.eigenbase.oj.rex.OJRexImplementor} for truth-test row expressions <code>
 * IS TRUE</code> and <code>IS FALSE</code>.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class FarragoOJRexTruthTestImplementor
    extends FarragoOJRexImplementor
{
    //~ Instance fields --------------------------------------------------------

    private final boolean isTrue;
    private final boolean negated;

    //~ Constructors -----------------------------------------------------------

    public FarragoOJRexTruthTestImplementor(boolean isTrue, boolean negated)
    {
        this.isTrue = isTrue;
        this.negated = negated;
    }

    //~ Methods ----------------------------------------------------------------

    // implement FarragoOJRexImplementor
    public Expression implementFarrago(
        FarragoRexToOJTranslator translator,
        RexCall call,
        Expression [] operands)
    {
        // Expression     negated isTrue Implementation
        // ============== ======= ====== ===================
        // x is not true  true    true   !x.val || x.isnull
        // x is true      false   true   x.val && !x.isnull
        // x is not false true    false  x.val || x.isnull
        // x is false     false   false  !x.val && !x.isnull
        Expression operand = operands[0];
        if (call.operands[0].getType().isNullable()) {
            Expression val =
                new FieldAccess(operand, NullablePrimitive.VALUE_FIELD_NAME);
            final MethodCall isnull =
                new MethodCall(
                    operand,
                    NullableValue.NULL_IND_ACCESSOR_NAME,
                    new ExpressionList());
            return new BinaryExpression(
                maybeNegate(
                    isnull,
                    !negated),
                negated ? BinaryExpression.LOGICAL_OR
                : BinaryExpression.LOGICAL_AND,
                maybeNegate(
                    val,
                    negated == isTrue));
        } else {
            return maybeNegate(operand, isTrue == negated);
        }
    }

    private Expression maybeNegate(Expression expr, boolean negate)
    {
        if (negate) {
            return new UnaryExpression(UnaryExpression.NOT, expr);
        } else {
            return expr;
        }
    }
}

// End FarragoOJRexTruthTestImplementor.java
