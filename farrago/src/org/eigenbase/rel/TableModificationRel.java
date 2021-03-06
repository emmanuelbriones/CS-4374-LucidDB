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
package org.eigenbase.rel;

import java.util.*;

import org.eigenbase.relopt.*;


/**
 * TableModificationRel is like TableAccessRel, but represents a request to
 * modify a table rather than read from it. It takes one child which produces
 * the modified rows. (For INSERT, the new values; for DELETE, the old values;
 * for UPDATE, all old values plus updated new values.)
 *
 * @version $Id$
 */
public final class TableModificationRel
    extends TableModificationRelBase
{
    //~ Constructors -----------------------------------------------------------

    public TableModificationRel(
        RelOptCluster cluster,
        RelOptTable table,
        RelOptConnection connection,
        RelNode child,
        Operation operation,
        List<String> updateColumnList,
        boolean flattened)
    {
        super(
            cluster,
            CallingConvention.NONE.singletonSet,
            table,
            connection,
            child,
            operation,
            updateColumnList,
            flattened);
    }

    //~ Methods ----------------------------------------------------------------

    // implement Cloneable
    public TableModificationRel clone()
    {
        return copy(getChild().clone())
            .inheritTraitsFrom(this);
    }

    @Override
    public TableModificationRel copy(RelNode... inputs)
    {
        assert inputs.length == 1;
        return new TableModificationRel(
            getCluster(),
            table,
            connection,
            inputs[0],
            getOperation(),
            getUpdateColumnList(),
            isFlattened());
    }
}

// End TableModificationRel.java
