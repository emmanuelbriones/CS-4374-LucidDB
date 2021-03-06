/*
// Saffron preprocessor and data engine.
// Copyright (C) 2002-2004 Disruptive Tech
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
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

package net.sf.saffron.ext;

import java.lang.reflect.Field;
import java.util.*;

import net.sf.saffron.oj.*;
import net.sf.saffron.oj.rel.ExpressionReaderRel;

import openjava.ptree.Expression;
import openjava.ptree.FieldAccess;

import org.eigenbase.oj.util.*;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.CalcRel;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.RexNode;
import org.eigenbase.util.Util;


/**
 * A <code>ClassSchema</code> is a schema whose tables are reflections of the
 * the public fields of a given class.
 */
public class ClassSchema implements RelOptSchema
{
    private final Class clazz;
    private final boolean ignoreCase;

    public ClassSchema(
        Class clazz,
        boolean ignoreCase)
    {
        this.clazz = clazz;
        this.ignoreCase = ignoreCase;
    }

    public RelOptTable getTableForMember(String [] names)
    {
        assert (names.length == 1);
        String name = names[0];
        final Field field = findField(name);
        if (field == null) {
            return null;
        }
        final Class rowType = Util.guessRowType(field.getType());
        RelDataType type = getTypeFactory().createJavaType(rowType);
        return new RelOptAbstractTable(
            this, name, type, Collections.<RelDataTypeField>emptyList())
        {
                public RelNode toRel(
                    RelOptCluster cluster,
                    RelOptConnection connection)
                {
                    Util.pre(cluster != null, "cluster != null");
                    Util.pre(connection != null, "connection != null");

                    final OJConnectionRegistry.ConnectionInfo info =
                        OJConnectionRegistry.instance.get(connection, true);
                    final Expression connectionExpr = info.expr;
                    final FieldAccess expr =
                        new FieldAccess(
                            getTarget(connectionExpr),
                            field.getName());
                    final JavaRexBuilder javaRexBuilder =
                        (JavaRexBuilder) cluster.getRexBuilder();
                    final RexNode rex =
                        javaRexBuilder.makeJava(info.env, expr);
                    final ExpressionReaderRel exprReader =
                        new ExpressionReaderRel(
                            cluster,
                            rex,
                            getRowType());
                    if (true) {
                        return exprReader; // todo: cleanup
                    }
                    final RelDataTypeField [] exprReaderFields =
                        exprReader.getRowType().getFields();
                    assert exprReaderFields.length == 1;

                    // Create a project "$f0.name, $f0.empno, $f0.gender".
                    RexNode fieldAccess =
                        cluster.getRexBuilder().makeInputRef(
                            exprReaderFields[0].getType(),
                            0);
                    final RelDataTypeField [] fields =
                        fieldAccess.getType().getFields();
                    final String [] fieldNames = new String[fields.length];
                    final RexNode [] exps = new RexNode[fields.length];
                    for (int i = 0; i < exps.length; i++) {
                        exps[i] =
                            cluster.getRexBuilder().makeFieldAccess(
                                fieldAccess, i);
                        fieldNames[i] = fields[i].getName();
                    }
                    final RelNode project =
                        CalcRel.createProject(exprReader, exps, fieldNames);
                    return project;
                }
            };
    }

    public RelDataTypeFactory getTypeFactory()
    {
        return OJUtil.threadTypeFactory();
    }

    public void registerRules(RelOptPlanner planner)
        throws Exception
    {
    }

    /**
     * Given the expression which yields the current connection, returns an
     * expression which yields the object which holds the schema data.
     *
     * <p>By default, returns the connection expression. So if the connection
     * expression is <code>Variable("connection")</code>, it will return the
     * same variable, and the planner will expect to be able to cast this value
     * to the required class and find a field for each 'table' in the
     * schema.</p>
     */
    protected Expression getTarget(Expression connectionExp)
    {
        return connectionExp;
    }

    private Field findField(String name)
    {
        Field field;
        try {
            field = clazz.getField(name);
        } catch (NoSuchFieldException e) {
            field = null;
        } catch (SecurityException e) {
            field = null;
        }
        if ((field != null) || !ignoreCase) {
            return field;
        }
        final Field [] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equalsIgnoreCase(name)) {
                return fields[i];
            }
        }
        return null;
    }
}


// End ClassSchema.java
