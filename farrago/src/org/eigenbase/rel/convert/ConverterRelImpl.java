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
package org.eigenbase.rel.convert;

import org.eigenbase.rel.*;
import org.eigenbase.rel.metadata.*;
import org.eigenbase.relopt.*;
import org.eigenbase.util.*;


/**
 * Abstract implementation of {@link org.eigenbase.rel.convert.ConverterRel}.
 *
 * @author jhyde
 * @version $Id$
 */
public abstract class ConverterRelImpl
    extends SingleRel
    implements ConverterRel
{
    //~ Instance fields --------------------------------------------------------

    protected RelTraitSet inTraits;
    protected final RelTraitDef traitDef;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a ConverterRelImpl.
     *
     * @param cluster planner's cluster
     * @param traitDef the RelTraitDef this converter converts
     * @param traits the output traits of this converter
     * @param child child rel (provides input traits)
     */
    protected ConverterRelImpl(
        RelOptCluster cluster,
        RelTraitDef traitDef,
        RelTraitSet traits,
        RelNode child)
    {
        super(cluster, traits, child);
        this.inTraits = child.getTraits();
        this.traitDef = traitDef;
    }

    //~ Methods ----------------------------------------------------------------

    // implement RelNode
    public RelOptCost computeSelfCost(RelOptPlanner planner)
    {
        // Bump up the conversion cost by multiplying by the number of columns.
        // JVS wants IterCalcRel to be 2x cheaper than FennelCalcRel (see
        // comment in FennelCalcRel.computeSelfCost); well, that's fine, but we
        // have to price conversion realistically otherwise we end up going
        // into and out of java for trivial computations. -- jhyde, 2009/4/11
        double dRows = RelMetadataQuery.getRowCount(getChild());
        double dCpu = dRows * getRowType().getFieldCount();
        double dIo = 0;
        return planner.makeCost(dRows, dCpu, dIo);
    }

    protected Error cannotImplement()
    {
        return Util.newInternal(
            getClass() + " cannot convert from "
            + inTraits + " traits");
    }

    public boolean isDistinct()
    {
        return getChild().isDistinct();
    }

    protected CallingConvention getInputConvention()
    {
        return (CallingConvention) inTraits.getTrait(
            CallingConventionTraitDef.instance);
    }

    public RelTraitSet getInputTraits()
    {
        return inTraits;
    }

    public RelTraitDef getTraitDef()
    {
        return traitDef;
    }

}

// End ConverterRelImpl.java
