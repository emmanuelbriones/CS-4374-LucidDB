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
package com.lucidera.luciddb.applib.string;

import java.sql.Types;
import java.nio.CharBuffer;
import com.lucidera.luciddb.applib.resource.*;
import net.sf.farrago.runtime.*;

/**
 * leftN returns the first N characters of the string
 *
 * Ported from //bb/bb713/server/SQL/leftN.java
 */
public class LeftNUdf
{

    public static String execute( String in, int len )
    {
        if ( len < 0 ) {
            throw ApplibResourceObject.get().LenSpecifyNonNegative.ex();
        }

        char[] chars;
        CharBuffer cb = (CharBuffer)FarragoUdrRuntime.getContext();
        if (cb == null) {
            chars = new char[ 2048 ];
            cb = CharBuffer.wrap(chars);
            FarragoUdrRuntime.setContext(cb);
        }
        chars = cb.array();

        // we want either the left len characters or all the characters if the
        // total length of the string is already less than len
        int maxLen = Math.min( in.length(), len );

        // make sure we don't overflow our buffer
        // save one space for a null terminator
        if ( maxLen >= chars.length ) {
            maxLen = chars.length - 1;
        }

        // get the characters
        in.getChars( 0, maxLen, chars, 0 );

        // null terminate
        chars[ maxLen ] = 0;

        return new String( chars, 0, maxLen );
    }
}

// End LeftNUdf.java
