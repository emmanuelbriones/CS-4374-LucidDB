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
package net.sf.farrago.util;

import org.eigenbase.util.*;


/**
 * FarragoAllocation represents an object which requires a call in order to
 * release resources early rather than waiting for finalization.
 *
 * <p>REVIEW: SWZ: 2/22/2006: New code should use ClosableAllocation directly
 * when possible. Eventually remove this interface and replace all usages with
 * ClosableAllocation.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public interface FarragoAllocation
    extends ClosableAllocation
{
}

// End FarragoAllocation.java
