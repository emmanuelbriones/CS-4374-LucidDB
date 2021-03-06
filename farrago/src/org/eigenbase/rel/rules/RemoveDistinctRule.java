/*
// $Id$
// Package org.eigenbase is a class library of data management components.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2002 SQLstream, Inc.
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
package org.eigenbase.rel.rules;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;


/**
 * Rule to remove an {@link AggregateRel} implementing DISTINCT if the
 * underlying relational expression is already distinct.
 */
public class RemoveDistinctRule
    extends RelOptRule
{
    public static final RemoveDistinctRule instance =
        new RemoveDistinctRule();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a RemoveDistinctRule.
     */
    private RemoveDistinctRule()
    {
        // REVIEW jvs 14-Mar-2006: We have to explicitly mention the child here
        // to make sure the rule re-fires after the child changes (e.g. via
        // RemoveTrivialProjectRule), since that may change our information
        // about whether the child is distinct.  If we clean up the inference of
        // distinct to make it correct up-front, we can get rid of the reference
        // to the child here.
        super(
            new RelOptRuleOperand(
                AggregateRel.class,
                new RelOptRuleOperand(RelNode.class, ANY)));
    }

    //~ Methods ----------------------------------------------------------------

    public void onMatch(RelOptRuleCall call)
    {
        AggregateRel distinct = (AggregateRel) call.rels[0];
        if (!distinct.isDistinct()) {
            return;
        }
        RelNode child = distinct.getChild();
        if (child.isDistinct()) {
            child = call.getPlanner().register(child, distinct, call);
            child =
                convert(
                    child,
                    distinct.getTraits());
            if (child != null) {
                call.transformTo(child);
            }
        }
    }
}

// End RemoveDistinctRule.java
