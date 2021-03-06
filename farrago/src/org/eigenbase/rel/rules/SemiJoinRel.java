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

import java.util.*;

import org.eigenbase.rel.*;
import org.eigenbase.rel.metadata.*;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.*;


/**
 * A SemiJoinRel represents two relational expressions joined according to some
 * condition, where the output only contains the columns from the left join
 * input.
 *
 * @author Zelaine Fong
 * @version $Id$
 */
public final class SemiJoinRel
    extends JoinRelBase
{
    //~ Instance fields --------------------------------------------------------

    private List<Integer> leftKeys;
    private List<Integer> rightKeys;

    //~ Constructors -----------------------------------------------------------

    /**
     * @param cluster cluster that join belongs to
     * @param left left join input
     * @param right right join input
     * @param condition join condition
     * @param leftKeys left keys of the semijoin
     * @param rightKeys right keys of the semijoin
     */
    public SemiJoinRel(
        RelOptCluster cluster,
        RelNode left,
        RelNode right,
        RexNode condition,
        List<Integer> leftKeys,
        List<Integer> rightKeys)
    {
        super(
            cluster,
            CallingConvention.NONE.singletonSet,
            left,
            right,
            condition,
            JoinRelType.INNER,
            Collections.<String>emptySet());
        this.leftKeys = leftKeys;
        this.rightKeys = rightKeys;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public JoinRelBase copy(
        RexNode conditionExpr,
        List<RelDataTypeField> systemFieldList,
        RelNode left,
        RelNode right)
    {
        return new SemiJoinRel(
            getCluster(),
            left,
            right,
            conditionExpr,
            // FIXME: If condition has changed, keys might have also
            new ArrayList<Integer>(getLeftKeys()),
            new ArrayList<Integer>(getRightKeys()));
    }

    // implement RelNode
    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        // REVIEW jvs 9-Apr-2006:  Just for now...
        return planner.makeTinyCost();
    }

    // implement RelNode
    public double getRows()
    {
        // TODO:  correlation factor
        return RelMetadataQuery.getRowCount(left)
            * RexUtil.getSelectivity(condition);
    }

    /**
     * @return returns rowtype representing only the left join input
     */
    public RelDataType deriveRowType()
    {
        return deriveJoinRowType(
            left.getRowType(),
            null,
            JoinRelType.INNER,
            getCluster().getTypeFactory(),
            null,
            Collections.<RelDataTypeField>emptyList());
    }

    public List<Integer> getLeftKeys()
    {
        return leftKeys;
    }

    public List<Integer> getRightKeys()
    {
        return rightKeys;
    }
}

// End SemiJoinRel.java
