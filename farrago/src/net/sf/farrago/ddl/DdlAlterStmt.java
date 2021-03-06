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
package net.sf.farrago.ddl;

import net.sf.farrago.catalog.*;
import net.sf.farrago.cwm.core.*;
import net.sf.farrago.cwm.relational.*;
import net.sf.farrago.resource.*;
import net.sf.farrago.session.*;


/**
 * DdlAlterStmt represents some but not all DDL ALTER statements. For others,
 * see {@link DdlAlterTableStructureStmt}, {@link DdlRebuildTableStmt}, and
 * {@link DdlSetSystemParamStmt}.
 *
 * @author Stephan Zuercher
 * @version $Id$
 */
public abstract class DdlAlterStmt
    extends DdlStmt
{
    //~ Enums ------------------------------------------------------------------

    private enum ActionType
    {
        ALTER_IDENTITY
    }

    //~ Instance fields --------------------------------------------------------

    private ActionType action;
    private CwmColumn column;
    private FarragoSequenceOptions identityOptions;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructs a new DdlAlterStmt.
     *
     * @param alterElement top-level element altered by this stmt
     */
    public DdlAlterStmt(CwmModelElement alterElement)
    {
        super(alterElement);
    }

    // REVIEW: SWZ: 2008-02-26: Eliminate this constructor if no red-zone
    // subclasses require it.
    /**
     * @deprecated
     */
    public DdlAlterStmt(CwmModelElement alterElement, boolean runsAsDml)
    {
        super(alterElement, runsAsDml);
    }

    //~ Methods ----------------------------------------------------------------

    public void setColumn(CwmColumn column)
    {
        this.column = column;
    }

    public void alterIdentityColumn(FarragoSequenceOptions options)
    {
        action = ActionType.ALTER_IDENTITY;
        identityOptions = options;
    }

    // implement DdlStmt
    public void visit(DdlVisitor visitor)
    {
        visitor.visit(this);
    }

    // implement DdlStmt
    public void preValidate(FarragoSessionDdlValidator ddlValidator)
    {
        // REVIEW: SWZ: 2008-02-26: It may be possible to eliminate this
        // reentrant session if DdlAlterGenericStmt (and its red-zone
        // subclasses) don't require it.

        // Use a reentrant session to simplify cleanup.
        FarragoSession session = ddlValidator.newReentrantSession();
        try {
            execute(ddlValidator, session);
        } catch (Throwable ex) {
            throw FarragoResource.instance().ValidatorAlterFailed.ex(ex);
        } finally {
            ddlValidator.releaseReentrantSession(session);
        }
    }

    // refine DdlStmt
    public void postCommit(FarragoSessionDdlValidator ddlValidator)
    {
        if (ddlValidator instanceof DdlValidator) {
            DdlValidator val = (DdlValidator) ddlValidator;
            val.handlePostCommit(getModelElement(), "Alter");
        }
    }

    /**
     * Execute the alter statement
     *
     * @param ddlValidator the session validator
     * @param session a reentrant session
     */
    protected abstract void execute(
        FarragoSessionDdlValidator ddlValidator,
        FarragoSession session);
}

// End DdlAlterStmt.java
