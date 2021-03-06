/*
// $Id$
// Fennel is a library of data storage and processing components.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2005 SQLstream, Inc.
// Copyright (C) 2005 Dynamo BI Corporation
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

#ifndef Fennel_FtrsTableWriterExecStream_Included
#define Fennel_FtrsTableWriterExecStream_Included

#include "fennel/ftrs/FtrsTableWriter.h"
#include "fennel/exec/ConduitExecStream.h"

#include <boost/scoped_array.hpp>

FENNEL_BEGIN_NAMESPACE

class FtrsTableWriterFactory;
class SXMutex;

/**
 * FtrsTableWriterExecStreamParams defines parameters for instantiating a
 * FtrsTableWriterExecStream.
 */
struct FENNEL_FTRS_EXPORT FtrsTableWriterExecStreamParams
    : public FtrsTableWriterParams
{
    SharedFtrsTableWriterFactory pTableWriterFactory;
    LogicalActionType actionType;
    SXMutex *pActionMutex;
};

/**
 * FtrsTableWriterExecStream reads tuples from a child stream and uses them
 * to write to all of the indexes making up a table (either INSERT or
 * DEELETE depending on prepared parameters).
 */
class FENNEL_FTRS_EXPORT FtrsTableWriterExecStream
    : public ConduitExecStream
{
    /**
     * Resulting number of rows.
     */
    RecordNum nTuples;

    /**
     * Type of write to perform (FtrsTableWriter::ACTION_INSERT
     * or FtrsTableWriter::ACTION_DELETE).
     */
    LogicalActionType actionType;

    /**
     * SXMutex on which to take a shared lock while action is in progress.
     * Normally used to block checkpoints.
     */
    SXMutex *pActionMutex;

    /**
     * Object which does the real update work.
     */
    SharedFtrsTableWriter pTableWriter;

    /**
     * Id of savepoint marking start of subtransaction, or NULL_SVPT_ID
     * if no subtransaction in progress.
     */
    SavepointId svptId;

    /**
     * Whether row count has been produced.
     */
    bool isDone;

    /**
     * Buffer holding result rowcount.
     */
    boost::scoped_array<FixedBuffer> outputTupleBuffer;

    /**
     * Tuple for producing result rowcount.
     */
    TupleData outputTuple;

    void createSavepoint();
    void commitSavepoint();
    void rollbackSavepoint();

public:
    explicit FtrsTableWriterExecStream();

    // implement ExecStream
    virtual void prepare(FtrsTableWriterExecStreamParams const &params);
    virtual void getResourceRequirements(
        ExecStreamResourceQuantity &minQuantity,
        ExecStreamResourceQuantity &optQuantity);
    virtual void open(bool restart);
    virtual ExecStreamResult execute(ExecStreamQuantum const &quantum);
    virtual void closeImpl();
    virtual ExecStreamBufProvision getOutputBufProvision() const;
};

FENNEL_END_NAMESPACE

#endif

// End FtrsTableWriterExecStream.h
