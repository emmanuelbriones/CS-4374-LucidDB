/*
// $Id$
// Package org.eigenbase is a class library of data management components.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
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
package org.eigenbase.sql.type;

import org.eigenbase.reltype.*;
import org.eigenbase.sql.*;
import org.eigenbase.sql.validate.*;

/**
 * SqlTypeTransforms defines a number of reusable instances of
 * {@link SqlTypeTransform}.
 *
 *<p>
 *
 * NOTE: avoid anonymous inner classes here except for unique,
 * non-generalizable strategies; anything else belongs in a reusable top-level
 * class.  If you find yourself copying and pasting an existing strategy's
 * anonymous inner class, you're making a mistake.
 *
 * @author Wael Chatila
 * @version $Id$
 */
public abstract class SqlTypeTransforms
{
    /**
     * Parameter type-inference transform strategy where a derived type is
     * transformed into the same type but nullable if any of a calls operands
     * is nullable
     */
    public static final SqlTypeTransform toNullable =
        new SqlTypeTransform()
        {
            public RelDataType transformType(
                SqlOperatorBinding opBinding,
                RelDataType typeToTransform)
            {
                return SqlTypeUtil.makeNullableIfOperandsAre(
                    opBinding.getTypeFactory(),
                    opBinding.collectOperandTypes(),
                    typeToTransform);
            }
        };

    /**
     * Type-inference strategy whereby the result type of a call is VARYING
     * the type given.
     * The length returned is the same as length of the
     * first argument.
     * Return type will have same nullablilty as input type nullablility.
     * First Arg must be of string type.
     */
    public static final SqlTypeTransform toVarying =
        new SqlTypeTransform()
        {
            public RelDataType transformType(
                SqlOperatorBinding opBinding,
                RelDataType typeToTransform)
            {
                switch (typeToTransform.getSqlTypeName().getOrdinal()) {
                case SqlTypeName.Varchar_ordinal:
                case SqlTypeName.Varbinary_ordinal:
                    return typeToTransform;
                }

                SqlTypeName retTypeName = toVar(typeToTransform);

                RelDataType ret =
                    opBinding.getTypeFactory().createSqlType(
                        retTypeName,
                        typeToTransform.getPrecision());
                if (SqlTypeUtil.inCharFamily(typeToTransform)) {
                    ret = opBinding.getTypeFactory().
                        createTypeWithCharsetAndCollation(
                            ret,
                            typeToTransform.getCharset(),
                            typeToTransform.getCollation());
                }
                return opBinding.getTypeFactory().createTypeWithNullability(
                    ret,
                    typeToTransform.isNullable());
            }

            private SqlTypeName toVar(RelDataType type)
            {
                final SqlTypeName sqlTypeName = type.getSqlTypeName();
                switch (sqlTypeName.getOrdinal()) {
                case SqlTypeName.Char_ordinal:
                    return SqlTypeName.Varchar;
                case SqlTypeName.Binary_ordinal:
                    return SqlTypeName.Varbinary;
                default:
                    throw sqlTypeName.unexpected();
                }
            }
        };
    
    /**
     * Parameter type-inference transform strategy where a derived type
     * must be a multiset type and the returned type is the multiset's
     * element type.
     * @see {@link MultisetSqlType#getComponentType}
     */
    public static final SqlTypeTransform toMultisetElementType =
        new SqlTypeTransform()
        {
            public RelDataType transformType(
                SqlOperatorBinding opBinding,
                RelDataType typeToTransform)
            {
                return typeToTransform.getComponentType();
            }
        };
}

// End SqlTypeTransforms.java