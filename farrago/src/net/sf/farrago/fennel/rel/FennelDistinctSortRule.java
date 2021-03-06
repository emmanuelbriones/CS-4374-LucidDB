/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2003 SQLstream, Inc.
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
package net.sf.farrago.fennel.rel;

import net.sf.farrago.query.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;

/**
 * FennelDistinctSortRule is a rule for implementing DISTINCT via a Fennel sort.
 *
 * <p>A DISTINCT is recognized as an {@link AggregateRel} with no
 * {@link org.eigenbase.rel.AggregateCall}s and the same number
 * of outputs as inputs.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class FennelDistinctSortRule
    extends RelOptRule
{
    public static final FennelDistinctSortRule instance =
        new FennelDistinctSortRule();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a FennelDistinctSortRule.
     */
    private FennelDistinctSortRule()
    {
        super(
            new RelOptRuleOperand(
                AggregateRel.class,
                ANY));
    }

    //~ Methods ----------------------------------------------------------------

    // implement RelOptRule
    public CallingConvention getOutConvention()
    {
        return FennelRel.FENNEL_EXEC_CONVENTION;
    }

    // implement RelOptRule
    public void onMatch(RelOptRuleCall call)
    {
        AggregateRel agg = (AggregateRel) call.rels[0];
        if (agg.getAggCallList().size() > 0) {
            return;
        }
        RelNode relInput = agg.getChild();
        int n = relInput.getRowType().getFieldList().size();
        if (agg.getGroupSet().cardinality() < n) {
            return;
        }

        RelNode fennelInput =
            convert(
                relInput,
                agg.getTraits().plus(FennelRel.FENNEL_EXEC_CONVENTION));
        if (fennelInput == null) {
            return;
        }

        Integer [] keyProjection = FennelRelUtil.newIotaProjection(n);

        // REVIEW:  should cluster be from agg or relInput?
        boolean discardDuplicates = true;
        FennelSortRel sort =
            new FennelSortRel(
                agg.getCluster(),
                fennelInput,
                keyProjection,
                discardDuplicates);
        call.transformTo(sort);
    }
}

// End FennelDistinctSortRule.java
