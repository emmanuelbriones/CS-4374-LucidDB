/*
// $Id$
// Fennel is a library of data storage and processing components.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2004 SQLstream, Inc.
// Copyright (C) 2009 Dynamo BI Corporation
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

#include "fennel/common/CommonPreamble.h"
#include "fennel/calculator/ExtendedInstructionTable.h"
#include "fennel/calculator/SqlDate.h"

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/gregorian/gregorian_types.hpp>

FENNEL_BEGIN_CPPFILE("$Id$");

using namespace boost::local_time;

void
CastDateToStrA(
    RegisterRef<char*>* result,
    RegisterRef<int64_t>* date)
{
    assert(date->type() == STANDARD_TYPE_INT_64);
    assert(StandardTypeDescriptor::isTextArray(result->type()));

    if (date->isNull()) {
        result->toNull();
        result->length(0);
    } else {
        // Produce a result like "2004-05-12"
        int64_t v = date->value() * 1000;
        int len = SqlDateToStr<1, 1, SQLDATE>(
            result->pointer(), result->storage(), v,
            (result->type() == STANDARD_TYPE_CHAR ? true : false));
        result->length(len);
    }
}

void
CastTimeToStrA(
    RegisterRef<char*>* result,
    RegisterRef<int64_t>* time)
{
    assert(time->type() == STANDARD_TYPE_INT_64);
    assert(StandardTypeDescriptor::isTextArray(result->type()));

    if (time->isNull()) {
        result->toNull();
        result->length(0);
    } else {
        int64_t v = time->value() * 1000;
        int len = SqlDateToStr<1, 1, SQLTIME>(
            result->pointer(), result->storage(), v,
            (result->type() == STANDARD_TYPE_CHAR ? true : false));
        result->length(len);
    }
}

void
CastTimestampToStrA(
    RegisterRef<char*>* result,
    RegisterRef<int64_t>* tstamp)
{
    assert(tstamp->type() == STANDARD_TYPE_INT_64);
    assert(StandardTypeDescriptor::isTextArray(result->type()));

    if (tstamp->isNull()) {
        result->toNull();
        result->length(0);
    } else {
        int64_t v = tstamp->value() * 1000;
        int len = SqlDateToStr<1, 1, SQLTIMESTAMP>(
            result->pointer(), result->storage(), v,
            (result->type() == STANDARD_TYPE_CHAR ? true : false));
        result->length(len);
    }
}

void
CastStrAToDate(
    RegisterRef<int64_t>* result,
    RegisterRef<char*>* dateStr)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(StandardTypeDescriptor::isTextArray(dateStr->type()));

    if (dateStr->isNull()) {
        result->toNull();
    } else {
        result->value(
            SqlStrToDate<1, 1, SQLDATE>(
                dateStr->pointer(),
                dateStr->stringLength()));
    }
}

void
CastStrAToTime(
    RegisterRef<int64_t>* result,
    RegisterRef<char*>* timeStr)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(StandardTypeDescriptor::isTextArray(timeStr->type()));

    if (timeStr->isNull()) {
        result->toNull();
    } else {
        result->value(
            SqlStrToDate<1, 1, SQLTIME>(
                timeStr->pointer(),
                timeStr->stringLength()));
    }
}

void
CastStrAToTimestamp(
    RegisterRef<int64_t>* result,
    RegisterRef<char*>* timestampStr)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(StandardTypeDescriptor::isTextArray(timestampStr->type()));

    if (timestampStr->isNull()) {
        result->toNull();
    } else {
        result->value(
            SqlStrToDate<1, 1, SQLTIMESTAMP>(
                timestampStr->pointer(),
                timestampStr->stringLength()));
    }
}

// for debugging - see the millisec value passed through to fennel.
void CastDateTimeToInt64(
    RegisterRef<int64_t>* result,
    RegisterRef<int64_t>* dtime)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(dtime->type() == STANDARD_TYPE_INT_64);

    if (dtime->isNull()) {
        result->toNull();
    } else {
        result->value(dtime->value());
    }
}

void CurrentTime(RegisterRef<int64_t>* result)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    result->value(UniversalTime());
}

void CurrentTimestamp(RegisterRef<int64_t>* result)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    result->value(UniversalTimestamp());
}

void CurrentTime(
    RegisterRef<int64_t>* result,
    RegisterRef<int32_t>* precision)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(precision->type() == STANDARD_TYPE_INT_32);

    // precision is ignored for now
    result->value(UniversalTime());
}

void CurrentTimestamp(
    RegisterRef<int64_t>* result,
    RegisterRef<int32_t>* precision)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(precision->type() == STANDARD_TYPE_INT_32);

    // precision is ignored for now
    result->value(UniversalTimestamp());
}

void LocalTime(
    RegisterRef<int64_t>* result,
    RegisterRef<char *>* tz)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(tz->type() == STANDARD_TYPE_CHAR);

    time_zone_ptr tzPtr(
        new posix_time_zone(
            string(tz->pointer(), tz->stringLength())));

    result->value(LocalTime(tzPtr));
}

void LocalTimestamp(
    RegisterRef<int64_t>* result,
    RegisterRef<char *>* tz)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(tz->type() == STANDARD_TYPE_CHAR);

    time_zone_ptr tzPtr(
        new posix_time_zone(
            string(tz->pointer(), tz->stringLength())));

    result->value(LocalTimestamp(tzPtr));
}

void LocalTime(
    RegisterRef<int64_t>* result,
    RegisterRef<char *>* tz,
    RegisterRef<int32_t>* precision)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(tz->type() == STANDARD_TYPE_CHAR);
    assert(precision->type() == STANDARD_TYPE_INT_32);

    time_zone_ptr tzPtr(
        new posix_time_zone(
            string(tz->pointer(), tz->stringLength())));

    // precision is ignored for now
    result->value(LocalTime(tzPtr));
}

void LocalTimestamp(
    RegisterRef<int64_t>* result,
    RegisterRef<char *>* tz,
    RegisterRef<int32_t>* precision)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(tz->type() == STANDARD_TYPE_CHAR);
    assert(precision->type() == STANDARD_TYPE_INT_32);

    time_zone_ptr tzPtr(
        new posix_time_zone(
            string(tz->pointer(), tz->stringLength())));

    // precision is ignored for now
    result->value(LocalTimestamp(tzPtr));
}

void ExtractFunc(
    RegisterRef<int64_t>* result,
    RegisterRef<int32_t>* timeunit,
    RegisterRef<int64_t>* time)
{
    assert(result->type() == STANDARD_TYPE_INT_64);
    assert(time->type() == STANDARD_TYPE_INT_64);
    assert(timeunit->type() == STANDARD_TYPE_INT_32);

    if (time->isNull()) {
        result->toNull();
        return;
    }
    // Time is in milliseconds since epoch
    int64_t value = time->value();
    int32_t unitValue = timeunit->value();
    result->value(extractFromTimestamp(value, unitValue));
}

void
ExtDateTimeRegister(ExtendedInstructionTable* eit)
{
    assert(eit != NULL);

    vector<StandardTypeDescriptorOrdinal> params_V_I64;
    params_V_I64.push_back(STANDARD_TYPE_VARCHAR);
    params_V_I64.push_back(STANDARD_TYPE_INT_64);

    vector<StandardTypeDescriptorOrdinal> params_C_I64;
    params_C_I64.push_back(STANDARD_TYPE_CHAR);
    params_C_I64.push_back(STANDARD_TYPE_INT_64);

    vector<StandardTypeDescriptorOrdinal> params_I64_V;
    params_I64_V.push_back(STANDARD_TYPE_INT_64);
    params_I64_V.push_back(STANDARD_TYPE_VARCHAR);

    vector<StandardTypeDescriptorOrdinal> params_I64_C;
    params_I64_C.push_back(STANDARD_TYPE_INT_64);
    params_I64_C.push_back(STANDARD_TYPE_CHAR);

    vector<StandardTypeDescriptorOrdinal> params_I64_C_I32;
    params_I64_C_I32.push_back(STANDARD_TYPE_INT_64);
    params_I64_C_I32.push_back(STANDARD_TYPE_CHAR);
    params_I64_C_I32.push_back(STANDARD_TYPE_INT_32);

    vector<StandardTypeDescriptorOrdinal> params_I64_I64;
    params_I64_I64.push_back(STANDARD_TYPE_INT_64);
    params_I64_I64.push_back(STANDARD_TYPE_INT_64);

    vector<StandardTypeDescriptorOrdinal> params_I64;
    params_I64.push_back(STANDARD_TYPE_INT_64);

    vector<StandardTypeDescriptorOrdinal> params_I64_I32;
    params_I64_I32.push_back(STANDARD_TYPE_INT_64);
    params_I64_I32.push_back(STANDARD_TYPE_INT_32);

    // date -> str
    eit->add(
        "CastDateToStrA", params_V_I64,
        (ExtendedInstruction2<char*, int64_t>*) NULL,
        &CastDateToStrA);

    eit->add(
        "CastDateToStrA", params_C_I64,
        (ExtendedInstruction2<char*, int64_t>*) NULL,
        &CastDateToStrA);

    eit->add(
        "CastTimeToStrA", params_V_I64,
        (ExtendedInstruction2<char*, int64_t>*) NULL,
        &CastTimeToStrA);

    eit->add(
        "CastTimeToStrA", params_C_I64,
        (ExtendedInstruction2<char*, int64_t>*) NULL,
        &CastTimeToStrA);

    eit->add(
        "CastTimestampToStrA", params_V_I64,
        (ExtendedInstruction2<char*, int64_t>*) NULL,
        &CastTimestampToStrA);

    eit->add(
        "CastTimestampToStrA", params_C_I64,
        (ExtendedInstruction2<char*, int64_t>*) NULL,
        &CastTimestampToStrA);

    // str -> date
    eit->add(
        "CastStrAToDate", params_I64_V,
        (ExtendedInstruction2<int64_t, char*>*) NULL,
        &CastStrAToDate);

    eit->add(
        "CastStrAToDate", params_I64_C,
        (ExtendedInstruction2<int64_t, char*>*) NULL,
        &CastStrAToDate);

    eit->add(
        "CastStrAToTime", params_I64_V,
        (ExtendedInstruction2<int64_t, char*>*) NULL,
        &CastStrAToTime);

    eit->add(
        "CastStrAToTime", params_I64_C,
        (ExtendedInstruction2<int64_t, char*>*) NULL,
        &CastStrAToTime);

    eit->add(
        "CastStrAToTimestamp", params_I64_V,
        (ExtendedInstruction2<int64_t, char*>*) NULL,
        &CastStrAToTimestamp);

    eit->add(
        "CastStrAToTimestamp", params_I64_C,
        (ExtendedInstruction2<int64_t, char*>*) NULL,
        &CastStrAToTimestamp);

    // others
    eit->add(
        "CastDateTimeToInt64", params_I64_I64,
        (ExtendedInstruction2<int64_t, int64_t>*) NULL,
        &CastDateTimeToInt64);

    eit->add(
        "LocalTime2", params_I64_C,
        (ExtendedInstruction2<int64_t, char *>*) NULL,
        &LocalTime);

    eit->add(
        "LocalTimestamp2", params_I64_C,
        (ExtendedInstruction2<int64_t, char *>*) NULL,
        &LocalTimestamp);

    eit->add(
        "LocalTime3", params_I64_C_I32,
        (ExtendedInstruction3<int64_t, char *, int32_t>*) NULL,
        &LocalTime);

    eit->add(
        "LocalTimestamp3", params_I64_C_I32,
        (ExtendedInstruction3<int64_t, char *, int32_t>*) NULL,
        &LocalTimestamp);

    eit->add(
        "CurrentTime1", params_I64,
        (ExtendedInstruction1<int64_t>*) NULL,
        &CurrentTime);

    eit->add(
        "CurrentTimestamp1", params_I64,
        (ExtendedInstruction1<int64_t>*) NULL,
        &CurrentTimestamp);

    eit->add(
        "CurrentTime2", params_I64_I32,
        (ExtendedInstruction2<int64_t, int32_t>*) NULL,
        &CurrentTime);

    eit->add(
        "CurrentTimestamp2", params_I64_I32,
        (ExtendedInstruction2<int64_t, int32_t>*) NULL,
        &CurrentTimestamp);

    vector<StandardTypeDescriptorOrdinal> params_I64_I32_I64;
    params_I64_I32_I64.push_back(STANDARD_TYPE_INT_64);
    params_I64_I32_I64.push_back(STANDARD_TYPE_INT_32);
    params_I64_I32_I64.push_back(STANDARD_TYPE_INT_64);

    eit->add(
        "ExtractFunc",
        params_I64_I32_I64,
        (ExtendedInstruction3<int64_t, int32_t, int64_t>*) NULL,
        &ExtractFunc);
}

FENNEL_END_CPPFILE("$Id$");

// End ExtDateTime.cpp
