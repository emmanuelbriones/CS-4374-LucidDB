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
package net.sf.farrago.namespace.impl;

import java.sql.*;

import java.util.*;

import javax.sql.*;

import net.sf.farrago.namespace.*;

import org.eigenbase.rel.metadata.*;
import org.eigenbase.relopt.*;


/**
 * MedAbstractDataServer is an abstract base class for implementations of the
 * {@link FarragoMedDataServer} interface.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public abstract class MedAbstractDataServer
    extends MedAbstractBase
    implements FarragoMedDataServer
{
    //~ Instance fields --------------------------------------------------------

    private String serverMofId;
    private Properties props;
    private DataSource loopbackDataSource;

    //~ Constructors -----------------------------------------------------------

    protected MedAbstractDataServer(
        String serverMofId,
        Properties props)
    {
        this.serverMofId = serverMofId;
        this.props = props;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @return the MofId of the catalog definition for this server
     */
    public String getServerMofId()
    {
        return serverMofId;
    }

    /**
     * @return the options specified by CREATE SERVER
     */
    public Properties getProperties()
    {
        return props;
    }

    /**
     * @return current loopback data source
     */
    public DataSource getLoopbackDataSource()
    {
        return loopbackDataSource;
    }

    // implement FarragoMedDataServer
    public void setLoopbackDataSource(DataSource loopbackDataSource)
    {
        this.loopbackDataSource = loopbackDataSource;
    }

    // implement FarragoMedDataServer
    public FarragoMedNameDirectory getNameDirectory()
        throws SQLException
    {
        return null;
    }

    // implement FarragoMedDataServer
    public Object getRuntimeSupport(Object param)
        throws SQLException
    {
        return null;
    }

    // implement FarragoMedDataServer
    public void registerRules(RelOptPlanner planner)
    {
    }

    // implement FarragoMedDataServer
    public void registerRelMetadataProviders(ChainedRelMetadataProvider chain)
    {
    }

    // implement FarragoAllocation
    public void closeAllocation()
    {
    }

    // implement FarragoMedDataServer
    public void releaseResources()
    {
    }
}

// End MedAbstractDataServer.java
