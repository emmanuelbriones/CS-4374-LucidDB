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
package net.sf.farrago.db;

import java.util.*;

import net.sf.farrago.catalog.*;
import net.sf.farrago.fem.sql2003.*;
import net.sf.farrago.fennel.*;
import net.sf.farrago.plugin.*;
import net.sf.farrago.resource.*;
import net.sf.farrago.session.*;
import net.sf.farrago.util.*;


/**
 * FarragoDbSessionFactory is a basic implementation for the {@link
 * net.sf.farrago.session.FarragoSessionFactory} interface.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class FarragoDbSessionFactory
    implements FarragoSessionFactory
{
    private FarragoPluginClassLoader pluginClassLoader;

    //~ Methods ----------------------------------------------------------------

    public FarragoDbSessionFactory()
    {
        this.pluginClassLoader = new FarragoPluginClassLoader();
    }

    // implement FarragoSessionFactory
    public FarragoSession newSession(
        String url,
        Properties info)
    {
        return new FarragoDbSession(url, info, this);
    }

    // implement FarragoSessionFactory
    public FarragoSession newReentrantSession(FarragoSession session)
    {
        // REVIEW jvs 9-Jan-2007:  Seems like this is redundant,
        // since cloneSession is going to clone the variables again.
        FarragoSessionVariables sessionVars =
            session.getSessionVariables().cloneVariables();

        FarragoSession reentrantSession = session.cloneSession(sessionVars);

        return reentrantSession;
    }

    // implement FarragoSessionFactory
    public void releaseReentrantSession(FarragoSession session)
    {
        session.closeAllocation();
    }

    // implement FarragoSessionPersonalityFactory
    public FarragoSessionPersonality newSessionPersonality(
        FarragoSession session,
        FarragoSessionPersonality defaultPersonality)
    {
        if (defaultPersonality == null) {
            throw new UnsupportedOperationException(
                "no default session personality defined");
        } else {
            return defaultPersonality;
        }
    }

    // implement FarragoSessionFactory
    public void setPluginClassLoader(
        FarragoPluginClassLoader pluginClassLoader)
    {
        this.pluginClassLoader = pluginClassLoader;
    }

    // implement FarragoSessionFactory
    public FarragoPluginClassLoader getPluginClassLoader()
    {
        return pluginClassLoader;
    }

    // implement FarragoSessionFactory
    public FarragoSessionModelExtension newModelExtension(
        FarragoPluginClassLoader pcl,
        FemJar femJar)
    {
        String url = FarragoCatalogUtil.getJarUrl(femJar);
        try {
            String attr =
                FarragoPluginClassLoader.PLUGIN_FACTORY_CLASS_ATTRIBUTE;
            Class factoryClass =
                pcl.loadClassFromJarUrlManifest(url, attr);
            FarragoSessionModelExtensionFactory factory =
                (FarragoSessionModelExtensionFactory) pcl
                .newPluginInstance(factoryClass);

            // TODO:  trace info about extension
            FarragoSessionModelExtension modelExtension =
                factory.newModelExtension();
            return modelExtension;
        } catch (Throwable ex) {
            throw FarragoResource.instance().PluginInitFailed.ex(url, ex);
        }
    }

    // implement FarragoSessionFactory
    public FennelCmdExecutor newFennelCmdExecutor()
    {
        return new FennelCmdExecutorImpl();
    }

    // implement FarragoSessionFactory
    public FarragoRepos newRepos(
        FarragoAllocationOwner owner,
        boolean userRepos)
    {
        return new FarragoMdrReposImpl(
            owner,
            new FarragoModelLoader(),
            userRepos);
    }

    // implement FarragoSessionFactory
    public FennelTxnContext newFennelTxnContext(
        FarragoRepos repos,
        FennelDbHandle fennelDbHandle)
    {
        return new FennelTxnContext(repos, fennelDbHandle);
    }

    // implement FarragoSessionFactory
    public FarragoSessionTxnMgr newTxnMgr()
    {
        return new FarragoDbNullTxnMgr();
    }

    // implement FarragoSessionFactory
    public void applyFennelExtensionParameters(Properties map)
    {
    }

    // implement FarragoSessionFactory
    public void specializedInitialization(FarragoAllocationOwner owner)
    {
    }

    // implement FarragoSessionFactory
    public void specializedShutdown()
    {
    }

    // implement FarragoSessionFactory
    public void cleanupSessions()
    {
        FarragoDbSingleton.shutdownConditional(0);
    }

    // implement FarragoSessionFactory
    public void defineResourceBundles(List<ResourceBundle> bundleList)
    {
        bundleList.add(FarragoResource.instance());
    }
}

// End FarragoDbSessionFactory.java
