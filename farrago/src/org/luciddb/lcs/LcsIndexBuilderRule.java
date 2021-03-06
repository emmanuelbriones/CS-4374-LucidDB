/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2010 SQLstream, Inc.
// Copyright (C) 2005 Dynamo BI Corporation
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
package org.luciddb.lcs;

import net.sf.farrago.query.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;

/**
 * LcsModificationRule is a rule for converting an abstract {@link
 * FarragoIndexBuilderRel} into a corresponding {@link LcsIndexBuilderRel}.
 *
 * <p>TODO: this rule was copied from FtrsIndexBuilderRule; consider
 * generalizing it.
 *
 * @author John Pham
 * @version $Id$
 */
class LcsIndexBuilderRule
    extends RelOptRule
{
    public static final LcsIndexBuilderRule instance =
        new LcsIndexBuilderRule();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a LcsIndexBuilderRule.
     */
    private LcsIndexBuilderRule()
    {
        super(
            new RelOptRuleOperand(
                FarragoIndexBuilderRel.class,
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
        FarragoIndexBuilderRel builderRel =
            (FarragoIndexBuilderRel) call.rels[0];

        if (!(builderRel.getTable() instanceof LcsTable)) {
            return;
        }

        RelNode inputRel = builderRel.getChild();
        RelNode fennelInput =
            convert(
                inputRel,
                call.rels[0].getTraits().plus(
                    FennelRel.FENNEL_EXEC_CONVENTION));
        if (fennelInput == null) {
            return;
        }

        LcsIndexBuilderRel lcsRel =
            new LcsIndexBuilderRel(
                builderRel.getCluster(),
                fennelInput,
                builderRel.getIndex());

        call.transformTo(lcsRel);
    }
}

// End LcsIndexBuilderRule.java
