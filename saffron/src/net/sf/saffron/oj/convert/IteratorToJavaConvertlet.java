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

package net.sf.saffron.oj.convert;

import openjava.mop.Toolbox;
import openjava.ptree.*;

import org.eigenbase.oj.rel.JavaRel;
import org.eigenbase.oj.rel.JavaRelImplementor;
import org.eigenbase.oj.util.OJUtil;
import org.eigenbase.rel.convert.ConverterRel;
import org.eigenbase.relopt.CallingConvention;
import org.eigenbase.util.Util;


/**
 * Thunk to convert between {@link CallingConvention#ITERATOR iterator}
 * and {@link CallingConvention#JAVA java} calling-conventions.
 *
 * @author jhyde
 * @since May 27, 2004
 * @version $Id$
 **/
public class IteratorToJavaConvertlet extends JavaConvertlet
{
    public IteratorToJavaConvertlet()
    {
        super(CallingConvention.ITERATOR, CallingConvention.JAVA);
    }

    public ParseTree implement(
        JavaRelImplementor implementor,
        ConverterRel converter)
    {
        // Generate
        //   Iterator iter = <<exp>>;
        //   while (iter.hasNext()) {
        //     V row = (Type) iter.next();
        //     <<body>>
        //   }
        //
        StatementList stmtList = implementor.getStatementList();
        StatementList whileBody = new StatementList();
        Variable variable_iter = implementor.newVariable();
        Expression exp =
            implementor.visitJavaChild(
                converter, 0, (JavaRel) converter.getChild());
        stmtList.add(
            new VariableDeclaration(
                new TypeName("java.util.Iterator"),
                variable_iter.toString(),
                exp));
        stmtList.add(
            new WhileStatement(
                new MethodCall(variable_iter, "hasNext", null),
                whileBody));
        Variable variable_row =
            implementor.bind(
                converter,
                whileBody,
                Util.castObject(
                    new MethodCall(variable_iter, "next", null),
                    Toolbox.clazzObject,
                    OJUtil.typeToOJClass(
                        converter.getChild().getRowType(),
                        implementor.getTypeFactory())));
        Util.discard(variable_row);
        implementor.generateParentBody(converter, whileBody);
        return null;
    }
}


// End IteratorToJavaConvertlet.java
