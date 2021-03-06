/*
// $Id$
// Fennel is a library of data storage and processing components.
// Copyright (C) 2006 The Eigenbase Project
// Copyright (C) 2010 SQLstream, Inc.
// Copyright (C) 2006 Dynamo BI Corporation
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

#ifndef Fennel_LbmSegment_Included
#define Fennel_LbmSegment_Included

#include "fennel/lcs/LcsClusterNode.h"

FENNEL_BEGIN_NAMESPACE

/**
 * Class implementing bitmap segments.  Bitmap segments consist of a
 * starting rid value, a bitmap segment descriptor, and then the actual
 * bitmap segments.
 */
class FENNEL_LBM_EXPORT LbmSegment
    : public boost::noncopyable
{
protected:
    /**
     * Starting rid in the bitmap segment (if singleton, startRID == RID
     * column in entryTuple)
     */
    LcsRid startRID;

    /**
     * Increment forward from pSegDescStart.
     */
    PBuffer pSegDescStart;
    PBuffer pSegDescEnd;

    /**
     * Decrement backward from pSegStart.
     */
    PBuffer pSegStart;
    PBuffer pSegEnd;

    /**
     * Use half of a byte to encode the segment length, or the zero bytes
     * length.
     */
    static const uint LbmHalfByteSize = 4;

    /**
     * The upper 4 bits of Segment Descriptor byte is used to store the length
     * of the corresponding segment.
     */
    static const uint8_t LbmSegLengthMask = 0xf0;

    /**
     * The lower 4 bits of Segment Descriptor byte is used to store the length
     * of the "gap" following the corresponding segment(till the next segment
     * or the next LbmEntry).
     */
    static const uint8_t LbmZeroLengthMask = 0x0f;

    /**
     * If the length of zero bytes(a byte composed of 8 bits of 0s) is less
     * than 12, the length can be stored within the segment
     * descriptor. Otherwise, the segment descriptor gives the length of
     * additional bytes(maximumn is 3 bytes) in which the length is stored.
     */
    static const uint LbmZeroLengthCompact = 12;

   /**
    * Additional bytes(maximumn is 3 bytes) in which the length is stored. It
    * is stored with an offset of LbmZeroLengthCompact.
    * LbmZeroLengthExtended = (uint)LbmZeroLengthMask - LbmZeroLengthCompact.
    */
    static const uint LbmZeroLengthExtended =
        (uint)LbmZeroLengthMask - LbmZeroLengthCompact;

    /**
     * Maximum size(in bytes) for a bitmap segment. This size is limited by the
     * number of bits(=4 bits) in SegDesc to describe the segment length.
     */
    static const uint LbmMaxSegSize = 16;

    /**
     * Get value stored in a byte array.
     * The least significant bytes in the value is stored
     * at the first location in the array.
     *
     * @param array a byte array
     * @param arraySize size of the array(number of bytes)
     *
     * @return the value stored in this array.
     */
    static uint byteArray2Value(PBuffer array, uint arraySize);

    /**
     * Store value in a byte array.
     * The least significant bytes in the value is stored
     * at the first location in the array.
     *
     * @param value
     * @param array a byte array
     * @param arraySize size of the array(number of bytes)
     *
     * @return number of bytes used to store the value; 0 if the value requires
     * more than arraySize bytes to store.
     */
    static uint value2ByteArray(uint value, PBuffer array, uint arraySize);

    /**
     * Computes the number of bytes required to store a specified number of
     * zero bytes
     *
     * @param nZeroBytes the number of zero bytes to be stored
     *
     * @return number of bytes required to store the zero bytes; if the length
     * can be encoded in the segment descriptor, 0 is returned.
     */
    uint computeSpaceForZeroBytes(uint nZeroBytes);

    /**
     * Decodes the lengths stored in the descriptor for a segment, based
     * on where the segment descriptor is currently pointing, and advances
     * the segment descriptor to the next descriptor
     *
     * @param pSegDesc pointer to segment descriptor
     *
     * @param bmSegLen returns length of bitmap segment
     *
     * @param zeroBytes returns number of trailing zeros in this segment
     *
     * segment
     */
    static void readSegDescAndAdvance(
        PBuffer &pSegDesc, uint &bmSegLen, uint &zeroBytes);

    /**
     * Computes the length of the remaining segments in the current bitmap
     * segment, starting at the one specified by the input segment descriptor
     *
     * @param segDesc segment descriptor of the first segment that we want
     * to start computing the length from
     *
     * @return number of bytes occupied by the remaining segments in the
     * current entry
     */
    uint computeSegLength(PBuffer segDesc);

    /**
     * Computes the length of the remaining segment descriptors in the current
     * bitmap segment, starting at the one specified by the input segment
     * descriptor
     *
     * @param segDesc segment descriptor of the first segment that we want
     * to start computing the length from
     *
     * @return number of bytes occupied by the remaining segment descriptors
     * in the current entry
     */
    uint computeSegDescLength(PBuffer segDesc);

    /**
     * Returns the number of segments in an entry
     */
    uint countSegments();

public:
    /**
     * Rounds a rid value down to the nearest byte boundary
     *
     * @param rid value to be rounded
     *
     * @return rounded rid value
     */
    static inline LcsRid roundToByteBoundary(LcsRid rid);

    /**
     * Sets the length descriptor for a new segment with zero trailing zeros
     *
     * @param segDescByte byte that will be set with the segment length
     *
     * @param segLen length of the segment
     *
     * @return true if length can be encoded in a segment descriptor
     */
    static inline bool setSegLength(uint8_t &segDescByte, uint segLen);

    /**
     * Set segment length in an existing descriptor with the new
     * segment length.  Also, leaves the current zero trailing bytes count
     * untouched
     *
     * @param segDescByte segment descriptor byte to be modified
     * @param segLen new segment length
     *
     * @return true if length can be encoded in the segment descriptor
     */
    static inline bool adjustSegLength(uint8_t &segDescByte, uint segLen);

    /**
     * Get the segment length encoded in SegmentDescriptor.
     *
     * @param segDescByte the seg desc byte with segment length encoded.
     */
    static inline uint getSegLength(uint8_t segDescByte);

    /**
     * Get the number of bytes to store the length of zero bytes.
     *
     * @param segDescByte the seg desc byte with length of zero bytes encoded.
     */
    inline uint getZeroLengthByteCount(uint8_t segDescByte);

    /**
     * @return startRID of this bitmap segment
     */
    inline LcsRid getStartRID();

    /**
     * One byte in the bitmap encodes 8 RIDs.
     */
    static const uint LbmOneByteSize = 8;
    static const uint LbmOneByteSizeBitShift = 3;
    static const LcsRidPrimitive LbmOneByteSizeBitMask =
        0xfffffffffffffff8ULL;
};

inline LcsRid LbmSegment::roundToByteBoundary(LcsRid rid)
{
    return LcsRid(opaqueToInt(rid) & LbmOneByteSizeBitMask);
}

inline bool LbmSegment::setSegLength(uint8_t &segDescByte, uint segLen)
{
    if (segLen > LbmMaxSegSize) {
        return false;
    }
    segDescByte = (uint8_t) ((segLen - 1) << LbmHalfByteSize);
    return true;
}

inline bool LbmSegment::adjustSegLength(uint8_t &segDescByte, uint segLen)
{
    if (segLen > LbmMaxSegSize) {
        return false;
    }
    segDescByte &= ~LbmSegLengthMask;
    segDescByte |= (uint8_t) ((segLen - 1) << LbmHalfByteSize);
    return true;
}

inline uint LbmSegment::getSegLength(uint8_t segDescByte)
{
    return (((segDescByte & LbmSegLengthMask) >> LbmHalfByteSize) + 1);
}

inline uint LbmSegment::getZeroLengthByteCount(uint8_t segDescByte)
{
    uint lengthBytes = segDescByte & LbmZeroLengthMask;
    if (lengthBytes > LbmZeroLengthCompact) {
        return (lengthBytes - LbmZeroLengthCompact);
    } else {
        return 0;
    }
}

inline LcsRid LbmSegment::getStartRID()
{
    return startRID;
}

/**
 * Offset into the byte representation of row ids, in which each
 * row id is encoded as a single bit
 */
DEFINE_OPAQUE_INTEGER(LbmByteNumber, uint64_t);

/**
 * Returns the byte number of a rid
 */
inline LbmByteNumber ridToByteNumber(LcsRid rid)
{
    return LbmByteNumber(
        opaqueToInt(rid) >> LbmSegment::LbmOneByteSizeBitShift);
}

/**
 * Returns the rid corresponding to a byte offset
 */
inline LcsRid byteNumberToRid(LbmByteNumber byteNum)
{
    return LcsRid(
        opaqueToInt(byteNum) << LbmSegment::LbmOneByteSizeBitShift);
}

FENNEL_END_NAMESPACE

#endif

// End LbmSegment.h
