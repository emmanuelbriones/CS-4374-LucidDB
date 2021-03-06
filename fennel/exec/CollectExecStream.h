/*
// $Id$
// Fennel is a library of data storage and processing components.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2004 SQLstream, Inc.
// Copyright (C) 2009 Dynamo BI Corporation
// Portions Copyright (C) 1999 John V. Sichi
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

#ifndef Fennel_CollectExecStream_Included
#define Fennel_CollectExecStream_Included

#include "fennel/exec/ConduitExecStream.h"
#include "fennel/tuple/TupleAccessor.h"
#include "fennel/tuple/TupleData.h"
#include <boost/scoped_array.hpp>

FENNEL_BEGIN_NAMESPACE

/**
 * CollectExecStreamParams defines parameters for instantiating a
 * CollectExecStream.
 * @author Wael Chatila
 */
struct FENNEL_EXEC_EXPORT CollectExecStreamParams
    : public ConduitExecStreamParams
{
    //empty
};

/**
 * CollectExecStream reads all tuples from a child stream and collects them
 * into a single tuple which is written to one output tuple.
 *
 * @author Wael Chatila
 * @version $Id$
 */
class FENNEL_EXEC_EXPORT CollectExecStream
    : public ConduitExecStream
{
private:
    TupleData outputTupleData;
    TupleData inputTupleData;
    boost::scoped_array<FixedBuffer> pOutputBuffer;
    uint bytesWritten;
    bool alreadyWrittenToOutput;

public:
    virtual void prepare(CollectExecStreamParams const &params);
    virtual ExecStreamResult execute(ExecStreamQuantum const &quantum);
    virtual void open(bool restart);
    virtual void close();

};

FENNEL_END_NAMESPACE

#endif

// End CollectExecStream.h
