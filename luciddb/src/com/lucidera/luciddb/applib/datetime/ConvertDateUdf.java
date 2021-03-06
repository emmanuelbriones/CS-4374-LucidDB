/*
// $Id$
// LucidDB is a DBMS optimized for business intelligence.
// Copyright (C) 2006-2007 LucidEra, Inc.
// Copyright (C) 2006-2007 The Eigenbase Project
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
package com.lucidera.luciddb.applib.datetime;

import java.sql.*;
import com.lucidera.luciddb.applib.resource.*;
import net.sf.farrago.runtime.*;

/**
 * Convert a string to a date, using a specified date format mask.
 *
 * Ported from //bb/bb713/server/SQL/toDate.java
 */
public class ConvertDateUdf
{

    /**
     * @param in convert
     * @param mask mask for the string
     * @param reject the date is invalid, this parameter says whether to
     *     an exception or return null.
     * @return datatype
     * @exception ApplibException thrown when the mask is invalid, or the
     *     string is invalid for the specified mask AND <code>reject</code> is
     *     true.
     */
    public static Date execute( String in, String mask, boolean reject )
        throws ApplibException
    {
        Date ret;
        DateConversionHelper dch = (DateConversionHelper)FarragoUdrRuntime.getContext();
        if (dch == null) {
            dch = new DateConversionHelper();
            FarragoUdrRuntime.setContext(dch);
        }

        try
        {
            ret = dch.toDate( in, mask );
        } catch( ApplibException e ) {
            if( reject ) {
                throw e;
            } else {
                ret = null;
            }
        }

        return ret;
    }

    /**
     * @param in String to convert
     * @param mask Format mask for the string
     * @return Date datatype
     * @exception ApplibException thrown when the mask is invalid, or the
     *     string is invalid for the specified mask.
     */
    public static Date execute( String in, String mask ) throws ApplibException
    {
        return execute( in, mask, true );
    }
}

// End ConvertDateUdf.java
