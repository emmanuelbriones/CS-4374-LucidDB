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

#ifndef Fennel_DeviceAccessSchedulerParams_Included
#define Fennel_DeviceAccessSchedulerParams_Included

FENNEL_BEGIN_NAMESPACE

class ConfigMap;

/**
 * DeviceAccessSchedulerParams defines parameters used to create a
 * DeviceAccessScheduler.
 */
class FENNEL_DEVICE_EXPORT DeviceAccessSchedulerParams
{
public:
    static ParamName paramSchedulerType;
    static ParamName paramThreadCount;
    static ParamName paramMaxRequests;

    static ParamVal valThreadPoolScheduler;
    static ParamVal valIoCompletionPortScheduler;
    static ParamVal valAioPollingScheduler;
    static ParamVal valAioSignalScheduler;
    static ParamVal valAioLinuxScheduler;

    /**
     * Enumeration of available scheduler implementations
     */
    enum SchedulerType {
        THREAD_POOL_SCHEDULER,
        IO_COMPLETION_PORT_SCHEDULER,
        AIO_POLLING_SCHEDULER,
        AIO_SIGNAL_SCHEDULER,
        AIO_LINUX_SCHEDULER
    };

    /**
     * Type of scheduler to create.
     */
    SchedulerType schedulerType;

    /**
     * True if using the default scheduler type, as opposed to the one that was
     * explicitly specified in the configuration file
     */
    bool usingDefaultSchedulerType;

    /**
     * Suggested number of threads to dedicate to scheduling
     * activities; the scheduler may adjust this number based on
     * maxRequests.
     */
    uint nThreads;

    /**
     * The maximum number of simultaneous requests that this scheduler should
     * be able to handle; additional requests will be queued.
     */
    uint maxRequests;

    /**
     * Defines a default set of scheduler parameters.
     */
    DeviceAccessSchedulerParams();

    /**
     * Reads parameter settings from a ConfigMap.
     */
    void readConfig(ConfigMap const &configMap);
};

FENNEL_END_NAMESPACE

#endif

// End DeviceAccessSchedulerParams.h
