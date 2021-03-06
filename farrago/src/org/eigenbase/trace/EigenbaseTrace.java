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
package org.eigenbase.trace;

import java.io.File;
import java.util.logging.*;

import org.eigenbase.oj.rel.*;
import org.eigenbase.oj.stmt.*;
import org.eigenbase.oj.util.*;
import org.eigenbase.relopt.*;
import org.eigenbase.runtime.*;
import org.eigenbase.util.Util;
import org.eigenbase.util.property.*;


/**
 * Contains all of the {@link java.util.logging.Logger tracers} used within
 * org.eigenbase class libraries.
 *
 * <h3>Note to developers</h3>
 *
 * <p>Please ensure that every tracer used in org.eigenbase is added to this
 * class as a <em>public static final</em> member called <code>
 * <i>component</i>Tracer</code>. For example, {@link #getPlannerTracer} is the
 * tracer used by all classes which take part in the query planning process.
 *
 * <p>The javadoc in this file is the primary source of information on what
 * tracers are available, so the javadoc against each tracer member must be an
 * up-to-date description of what that tracer does. Be sure to describe what
 * {@link Level tracing level} is required to obtain each category of tracing.
 *
 * <p>In the class where the tracer is used, create a <em>private</em> (or
 * perhaps <em>protected</em>) <em>static final</em> member called <code>
 * tracer</code>.
 *
 * @author jhyde
 * @version $Id$
 * @since May 24, 2004
 */
public abstract class EigenbaseTrace
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * The "org.eigenbase.sql.parser" tracer reports parser events in {@link
     * org.eigenbase.sql.parser.SqlParser} and other classes (at level {@link
     * Level#FINE} or higher).
     */
    public static final Logger parserTracer = getParserTracer();

    private static final ThreadLocal<Util.Function2<Void, File, String>>
        DYNAMIC_HANDLER =
        new ThreadLocal<Util.Function2<Void, File, String>>()
        {
            @Override
            protected Util.Function2<Void, File, String> initialValue()
            {
                return Util.Functions.ignore2();
            }
        };

    //~ Methods ----------------------------------------------------------------

    /**
     * The "org.eigenbase.relopt.RelOptPlanner" tracer prints the query
     * optimization process.
     *
     * <p>Levels:
     *
     * <ul>
     * <li>{@link Level#FINE} prints rules as they fire;
     * <li>{@link Level#FINER} prints and validates the whole expression pool
     * and rule queue as each rule fires;
     * <li>{@link Level#FINEST} prints finer details like rule importances.
     * </ul>
     */
    public static Logger getPlannerTracer()
    {
        return Logger.getLogger(RelOptPlanner.class.getName());
    }

    /**
     * The "org.eigenbase.oj.stmt.OJPreparingStmt" tracer prints the generated
     * program at level {@link java.util.logging.Level#FINE} or higher.
     */
    public static Logger getStatementTracer()
    {
        return Logger.getLogger(OJPreparingStmt.class.getName());
    }

    /**
     * The "org.eigenbase.oj.rel.JavaRelImplementor" tracer reports when
     * expressions are bound to variables ({@link Level#FINE})
     */
    public static Logger getRelImplementorTracer()
    {
        return Logger.getLogger(JavaRelImplementor.class.getName());
    }

    /**
     * The tracer "org.eigenbase.sql.timing" traces timing for various stages of
     * query processing.
     *
     * @see EigenbaseTimingTracer
     */
    public static Logger getSqlTimingTracer()
    {
        return Logger.getLogger("org.eigenbase.sql.timing");
    }

    /**
     * The "org.eigenbase.sql.parser" tracer reports parse events.
     */
    public static Logger getParserTracer()
    {
        return Logger.getLogger("org.eigenbase.sql.parser");
    }

    /**
     * The "org.eigenbase.sql2rel" tracer reports parse events.
     */
    public static Logger getSqlToRelTracer()
    {
        return Logger.getLogger("org.eigenbase.sql2rel");
    }

    /**
     * The "org.eigenbase.jmi.JmiChangeSet" tracer reports JmiChangeSet events.
     */
    public static Logger getJmiChangeSetTracer()
    {
        return Logger.getLogger("org.eigenbase.jmi.JmiChangeSet");
    }

    /**
     * The "org.eigenbase.oj.util.OJClassMap" tracer reports when synthetic
     * classes are created ({@link Level#FINE})
     */
    public static Logger getClassMapTracer()
    {
        return Logger.getLogger(OJClassMap.class.getName());
    }

    /**
     * The "org.eigenbase.util.property.Property" tracer reports errors related
     * to all manner of properties.
     */
    public static Logger getPropertyTracer()
    {
        return Logger.getLogger(Property.class.getName());
    }

    /**
     * The "org.eigenbase.runtime.CompoundIterator" tracer traces {@link
     * CompoundIterator}:
     *
     * <ul>
     * <li>{@link Level#FINE} shows the transition to the next child Iterator.
     * <li>{@link Level#FINER} shows every element.
     * </ul>
     */
    public static Logger getCompoundIteratorTracer()
    {
        return Logger.getLogger(CompoundIterator.class.getName());
    }

    /**
     * Thread-local handler that is called with dynamically generated Java code.
     * It exists for unit-testing.
     * The handler is never null; the default handler does nothing.
     */
    public static ThreadLocal<Util.Function2<Void, File, String>>
    getDynamicHandler()
    {
        return DYNAMIC_HANDLER;
    }
}

// End EigenbaseTrace.java
