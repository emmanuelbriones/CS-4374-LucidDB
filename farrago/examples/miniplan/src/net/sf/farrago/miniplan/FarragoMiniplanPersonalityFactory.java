/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2008 The Eigenbase Project
// Copyright (C) 2008 SQLstream, Inc.
// Copyright (C) 2008 Dynamo BI Corporation
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
package net.sf.farrago.miniplan;

import net.sf.farrago.fennel.rel.*;
import org.eigenbase.relopt.volcano.*;
import org.luciddb.optimizer.*;

import net.sf.farrago.fem.config.*;
import net.sf.farrago.query.*;
import net.sf.farrago.db.*;
import net.sf.farrago.session.*;
import net.sf.farrago.defimpl.*;

import org.eigenbase.oj.rel.*;
import org.eigenbase.rel.*;
import org.eigenbase.rel.rules.*;
import org.eigenbase.relopt.*;
import org.eigenbase.relopt.hep.*;

import java.util.*;

/**
 * FarragoMiniplanPersonalityFactory implements the {@link
 * FarragoSessionPersonalityFactory} interface by plugging in
 * a "mini" planner meant only for tutorial purposes.
 *
 *<p>
 *
 * If you modify this class, please update <a
 * href="http://pub.eigenbase.org/wiki/HowToWriteAnOptimizer">the
 * corresponding wiki page</a> as well.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class FarragoMiniplanPersonalityFactory
    implements FarragoSessionPersonalityFactory
{
    //~ Methods ----------------------------------------------------------------

    // implement FarragoSessionPersonalityFactory
    public FarragoSessionPersonality newSessionPersonality(
        FarragoSession session,
        FarragoSessionPersonality defaultPersonality)
    {
        return new FarragoMiniplanSessionPersonality(
            (FarragoDbSession) session);
    }

    private static void addMiniplannerRules(FarragoSessionPlanner planner)
    {
        planner.addRule(PushProjectPastSetOpRule.instance);
        planner.addRule(LhxAggRule.instance);
        planner.addRule(RemoveTrivialProjectRule.instance);
        planner.addRule(FennelUnionRule.instance);
        planner.addRule(FennelReshapeRule.instance);
        planner.addRule(PushAggThroughUnionAllRule.instance);
        FennelToIteratorConverter.register(planner);
        IteratorToFennelConverter.register(planner);
    }

    private static HepProgram createMiniplannerHepProgram(
        Collection<RelOptRule> medPluginRules)
    {
        HepProgramBuilder builder = new HepProgramBuilder();

        builder.addGroupBegin();
        builder.addRuleInstance(RemoveTrivialProjectRule.instance);
        builder.addRuleInstance(PushProjectPastSetOpRule.instance);
        builder.addRuleInstance(MergeProjectRule.instance);
        builder.addGroupEnd();

        builder.addRuleInstance(PushAggThroughUnionAllRule.instance);

        builder.addRuleCollection(medPluginRules);

        builder.addRuleInstance(RemoveTrivialProjectRule.instance);
        builder.addRuleInstance(LhxAggRule.instance);

        builder.addRuleInstance(FennelReshapeRule.instance);
        builder.addRuleInstance(FennelUnionRule.instance);

        builder.addConverters(true);

        return builder.createProgram();
    }

    //~ Inner Classes ----------------------------------------------------------

    private static class FarragoMiniplanSessionPersonality
        extends FarragoDefaultSessionPersonality
    {
        private static final String MINIPLAN_VOLCANO = "volcano";

        protected FarragoMiniplanSessionPersonality(FarragoDbSession session)
        {
            super(session);
            paramValidator.registerBoolParam(MINIPLAN_VOLCANO, false);
        }

        // implement FarragoSessionPersonality
        public void loadDefaultSessionVariables(
            FarragoSessionVariables variables)
        {
            super.loadDefaultSessionVariables(variables);
            variables.setDefault(MINIPLAN_VOLCANO, "false");
        }

        // implement FarragoSessionPersonality
        public FarragoSessionPlanner newPlanner(
            FarragoSessionPreparingStmt stmt,
            boolean init)
        {
            boolean useVolcano =
                stmt.getSession().getSessionVariables().getBoolean(
                    MINIPLAN_VOLCANO);

            if (useVolcano) {
                FarragoVolcanoMiniplanner planner =
                    new FarragoVolcanoMiniplanner(stmt);
                if (init) {
                    planner.init();
                }
                return planner;
            } else {
                Collection<RelOptRule> medPluginRules =
                    new LinkedHashSet<RelOptRule>();

                HepProgram program =
                    createMiniplannerHepProgram(
                        medPluginRules);

                FarragoDefaultHeuristicPlanner planner =
                    new FarragoDefaultHeuristicPlanner(
                        program,
                        stmt,
                        medPluginRules);
                addMiniplannerRules(planner);
                return planner;
            }
        }
    }

    private static class FarragoVolcanoMiniplanner
        extends FarragoDefaultPlanner
    {
        public FarragoVolcanoMiniplanner(FarragoSessionPreparingStmt stmt)
        {
            super(stmt);
        }

        public void init()
        {
            addMiniplannerRules(this);
        }
    }
}

// End FarragoMiniplanPersonalityFactory.java
