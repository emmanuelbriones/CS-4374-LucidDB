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
package org.eigenbase.sql;

import org.eigenbase.sql.util.SqlString;

/**
 * A <code>SqlWriter</code> is the target to construct a SQL statement from a
 * parse tree. It deals with dialect differences; for example, Oracle quotes
 * identifiers as <code>"scott"</code>, while SQL Server quotes them as <code>
 * [scott]</code>.
 *
 * @author Julian Hyde
 * @version $Id$
 * @since 2002/8/8
 */
public interface SqlWriter
{
    //~ Enums ------------------------------------------------------------------

    /**
     * Style of formatting subqueries.
     */
    enum SubqueryStyle
    {
        /**
         * Julian's style of subquery nesting. Like this:
         *
         * <pre>SELECT *
         * FROM (
         *     SELECT *
         *     FROM t
         * )
         * WHERE condition</pre>
         */
        Hyde,

        /**
         * Damian's style of subquery nesting. Like this:
         *
         * <pre>SELECT *
         * FROM
         * (   SELECT *
         *     FROM t
         * )
         * WHERE condition</pre>
         */
        Black;
    }

    /**
     * Enumerates the types of frame.
     */
    enum FrameTypeEnum
        implements FrameType
    {
        /**
         * SELECT query (or UPDATE or DELETE). The items in the list are the
         * clauses: FROM, WHERE, etc.
         */
        Select,

        /**
         * Simple list.
         */
        Simple,

        /**
         * The SELECT clause of a SELECT statement.
         */
        SelectList,

        /**
         * The WINDOW clause of a SELECT statement.
         */
        WindowDeclList,

        /**
         * The SET clause of an UPDATE statement.
         */
        UpdateSetList,

        /**
         * Function declaration.
         */
        FunDecl,

        /**
         * Function call or datatype declaration.
         *
         * <p>Examples:
         * <li>SUBSTRING('foobar' FROM 1 + 2 TO 4)</li>
         * <li>DECIMAL(10, 5)</li>
         */
        FunCall,

        /**
         * Window specification.
         *
         * <p>Examples:
         * <li>SUM(x) OVER (ORDER BY hireDate ROWS 3 PRECEDING)</li>
         * <li>WINDOW w1 AS (ORDER BY hireDate), w2 AS (w1 PARTITION BY gender
         * RANGE BETWEEN INTERVAL '1' YEAR PRECEDING AND '2' MONTH
         * PRECEDING)</li>
         */
        Window,

        /**
         * ORDER BY clause of a SELECT statement. The "list" has only two items:
         * the query and the order by clause, with ORDER BY as the separator.
         */
        OrderBy,

        /**
         * ORDER BY list.
         *
         * <p>Example:
         * <li>ORDER BY x, y DESC, z
         */
        OrderByList,

        /**
         * GROUP BY list.
         *
         * <p>Example:
         * <li>GROUP BY x, FLOOR(y)
         */
        GroupByList,

        /**
         * Sub-query list. Encloses a SELECT, UNION, EXCEPT, INTERSECT query
         * with optional ORDER BY.
         *
         * <p>Example:
         * <li>GROUP BY x, FLOOR(y)
         */
        Subquery,

        /**
         * Set operation.
         *
         * <p>Example:
         * <li>SELECT * FROM a UNION SELECT * FROM b
         */
        Setop,

        /**
         * FROM clause (containing various kinds of JOIN).
         */
        FromList,

        /**
         * WHERE clause.
         */
        WhereList,

        /**
         * Compound identifier.
         *
         * <p>Example:
         * <li>"A"."B"."C"
         */
        Identifier(false);

        private final boolean needsIndent;

        /**
         * Creates a list type.
         */
        FrameTypeEnum()
        {
            this(true);
        }

        /**
         * Creates a list type.
         */
        FrameTypeEnum(boolean needsIndent)
        {
            this.needsIndent = needsIndent;
        }

        public boolean needsIndent()
        {
            return needsIndent;
        }

        /**
         * Creates a frame type.
         *
         * @param name Name
         * @return frame type
         */
        public static FrameType create(final String name)
        {
            return new FrameType() {
                public String getName()
                {
                    return name;
                }

                public boolean needsIndent()
                {
                    return true;
                }
            };
        }

        public String getName()
        {
            return name();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Resets this writer so that it can format another expression. Does not
     * affect formatting preferences (see {@link #resetSettings()}
     */
    void reset();

    /**
     * Resets all properties to their default values.
     */
    void resetSettings();

    /**
     * Returns the dialect of SQL.
     *
     * @return SQL dialect
     */
    SqlDialect getDialect();

    /**
     * Returns the contents of this writer as a 'certified kocher' SQL string.
     *
     * @return SQL string
     */
    SqlString toSqlString();

    /**
     * Prints a literal, exactly as provided. Does not attempt to indent or
     * convert to upper or lower case. Does not add quotation marks. Adds
     * preceding whitespace if necessary.
     */
    void literal(String s);

    /**
     * Prints a sequence of keywords. Must not start or end with space, but may
     * contain a space. For example, <code>keyword("SELECT")</code>, <code>
     * keyword("CHARACTER SET")</code>.
     */
    void keyword(String s);

    /**
     * Prints a string, preceded by whitespace if necessary.
     */
    void print(String s);

    /**
     * Prints an integer.
     *
     * @param x Integer
     */
    void print(int x);

    /**
     * Prints an identifier, quoting as necessary.
     */
    void identifier(String name);

    /**
     * Prints a new line, and indents.
     */
    void newlineAndIndent();

    /**
     * Returns whether this writer should quote all identifiers, even those
     * that do not contain mixed-case identifiers or punctuation.
     *
     * @return whether to quote all identifiers
     */
    boolean isQuoteAllIdentifiers();

    /**
     * Returns whether this writer should start each clause (e.g. GROUP BY) on
     * a new line.
     *
     * @return whether to start each clause on a new line
     */
    boolean isClauseStartsLine();

    /**
     * Returns whether the items in the SELECT clause should each be on a
     * separate line.
     *
     * @return whether to put each SELECT clause item on a new line
     */
    boolean isSelectListItemsOnSeparateLines();

    /**
     * Returns whether to output all keywords (e.g. SELECT, GROUP BY) in lower
     * case.
     *
     * @return whether to output SQL keywords in lower case
     */
    boolean isKeywordsLowerCase();

    /**
     * Starts a list which is a call to a function.
     *
     * @see #endFunCall(Frame)
     */
    Frame startFunCall(String funName);

    /**
     * Ends a list which is a call to a function.
     *
     * @param frame Frame
     *
     * @see #startFunCall(String)
     */
    void endFunCall(Frame frame);

    /**
     * Starts a list.
     */
    Frame startList(String open, String close);

    /**
     * Starts a list with no opening string.
     *
     * @param frameType Type of list. For example, a SELECT list will be
     */
    Frame startList(FrameTypeEnum frameType);

    /**
     * Starts a list.
     *
     * @param frameType Type of list. For example, a SELECT list will be
     * governed according to SELECT-list formatting preferences.
     * @param open String to start the list; typically "(" or the empty string.
     * @param close String to close the list
     */
    Frame startList(FrameType frameType, String open, String close);

    /**
     * Ends a list.
     *
     * @param frame The frame which was created by {@link #startList}.
     */
    void endList(Frame frame);

    /**
     * Writes a list separator, unless the separator is "," and this is the
     * first occurrence in the list.
     *
     * @param sep List separator, typically ",".
     */
    void sep(String sep);

    /**
     * Writes a list separator.
     *
     * @param sep List separator, typically ","
     * @param printFirst Whether to print the first occurrence of the separator
     */
    void sep(String sep, boolean printFirst);

    /**
     * Sets whether whitespace is needed before the next token.
     */
    void setNeedWhitespace(boolean needWhitespace);

    /**
     * Returns the offset for each level of indentation. Default 4.
     */
    int getIndentation();

    /**
     * Returns whether to enclose all expressions in parentheses, even if the
     * operator has high enough precedence that the parentheses are not
     * required.
     *
     * <p>For example, the parentheses are required in the expression <code>(a +
     * b) * c</code> because the '*' operator has higher precedence than the '+'
     * operator, and so without the parentheses, the expression would be
     * equivalent to <code>a + (b * c)</code>. The fully-parenthesized
     * expression, <code>((a + b) * c)</code> is unambiguous even if you don't
     * know the precedence of every operator.
     */
    boolean isAlwaysUseParentheses();

    /**
     * Returns whether we are currently in a query context (SELECT, INSERT,
     * UNION, INTERSECT, EXCEPT, and the ORDER BY operator).
     */
    boolean inQuery();

    //~ Inner Interfaces -------------------------------------------------------

    /**
     * A Frame is a piece of generated text which shares a common indentation
     * level.
     *
     * <p>Every frame has a beginning, a series of clauses and separators, and
     * an end. A typical frame is a comma-separated list. It begins with a "(",
     * consists of expressions separated by ",", and ends with a ")".
     *
     * <p>A select statement is also a kind of frame. The beginning and end are
     * are empty strings, but it consists of a sequence of clauses. "SELECT",
     * "FROM", "WHERE" are separators.
     *
     * <p>A frame is current between a call to one of the {@link
     * SqlWriter#startList} methods and the call to {@link
     * SqlWriter#endList(Frame)}. If other code starts a frame in the mean time,
     * the sub-frame is put onto a stack.
     */
    public interface Frame
    {
    }

    interface FrameType
    {
        /**
         * Returns the name of this frame type.
         *
         * @return name
         */
        String getName();

        /**
         * Returns whether this frame type should cause the code be further
         * indented.
         *
         * @return whether to further indent code within a frame of this type
         */
        boolean needsIndent();
    }
}

// End SqlWriter.java
