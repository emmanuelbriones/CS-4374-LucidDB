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

#include "fennel/common/CommonPreamble.h"
#include "fennel/common/FemEnums.h"
#include "fennel/test/ExecStreamUnitTestBase.h"
#include "fennel/lcs/LcsClusterAppendExecStream.h"
#include "fennel/sorter/ExternalSortExecStream.h"
#include "fennel/lbm/LbmGeneratorExecStream.h"
#include "fennel/lbm/LbmSplicerExecStream.h"
#include "fennel/lbm/LbmEntryDump.h"
#include "fennel/btree/BTreeBuilder.h"
#include "fennel/ftrs/BTreeInsertExecStream.h"
#include "fennel/ftrs/BTreeSearchExecStream.h"
#include "fennel/ftrs/BTreeExecStream.h"
#include "fennel/tuple/StandardTypeDescriptor.h"
#include "fennel/tuple/TupleDescriptor.h"
#include "fennel/exec/MockProducerExecStream.h"
#include "fennel/exec/ExecStreamEmbryo.h"
#include "fennel/exec/SplitterExecStream.h"
#include "fennel/exec/BarrierExecStream.h"
#include "fennel/exec/ExecStreamGraph.h"
#include "fennel/cache/Cache.h"
#include "fennel/common/TraceSource.h"
#include <stdarg.h>

#include <boost/test/test_tools.hpp>

using namespace fennel;

/**
 * Testcase for loading multiple clusters.
 */
class LbmLoadBitmapTest : public ExecStreamUnitTestBase
{
protected:
    StandardTypeDescriptorFactory stdTypeFactory;
    TupleAttributeDescriptor attrDesc_int64;

    /**
     * BTrees corresponding to the clusters
     */
    vector<boost::shared_ptr<BTreeDescriptor> > bTreeClusters;

    /**
     * Saved root pageids of btrees corresponding to clusters; used to
     * append to existing table
     */
    vector<PageId> savedBTreeClusterRootIds;

    /**
     * BTrees corresponding to the bitmaps
     */
    vector<boost::shared_ptr<BTreeDescriptor> > bTreeBitmaps;

    /**
     * Saved root pageids of btrees corresponding to bitmaps; used to
     * append to existing table
     */
    vector<PageId> savedBTreeBitmapRootIds;

    /**
     * Entry dumps corresponding to the bitmaps
     */
    vector<boost::shared_ptr<LbmEntryDump> > entryDumps;

    /**
     * Initializes a BTreeExecStreamParam structure
     */
    void initBTreeExecStreamParam(
        BTreeExecStreamParams &param, shared_ptr<BTreeDescriptor> pBTreeDesc);

    /**
     * Initializes BTreeParams structure
     */
    void initBTreeParam(
        BTreeParams &param, shared_ptr<BTreeDescriptor> pBTreeDesc);

    /**
     * Initializes a cluster scan def structure for a generator exec stream
     */
    void initClusterScanDef(
        LbmGeneratorExecStreamParams &generatorParams,
        struct LcsClusterScanDef &clusterScanDef,
        uint bTreeIndex,
        DynamicParamId paramId);

    /**
     * Initializes BTreeExecStreamParam corresponding to a bitmap index
     */
    void  initBTreeBitmapDesc(
        TupleDescriptor &param, TupleProjection &keyProj, uint nKeys);

    /**
     * Initializes a tuple descriptor corresponding to a bitmap index
     */
    void initBTreeTupleDesc(TupleDescriptor &tupleDesc, uint nKeys);

    void testLoad(
        uint nRows, uint nClusters, std::vector<int> &repeatSeqValues,
        bool newRoot, bool dumpEntries, string testName,
        bool dynamicRootPageId);

public:
    explicit LbmLoadBitmapTest()
    {
        FENNEL_UNIT_TEST_CASE(LbmLoadBitmapTest, testLoad50);
        FENNEL_UNIT_TEST_CASE(LbmLoadBitmapTest, testLoad5000);
        FENNEL_UNIT_TEST_CASE(LbmLoadBitmapTest, testLoad10000);
        FENNEL_UNIT_TEST_CASE(LbmLoadBitmapTest, testAppend);
        FENNEL_UNIT_TEST_CASE(LbmLoadBitmapTest, testLoadDynamicRoot50);
    }

    void testCaseSetUp();
    void testCaseTearDown();

    void testLoadSmall(bool dynamicRootPageId);

    void testLoad50();
    void testLoad5000();
    void testLoad10000();
    void testAppend();
    void testLoadDynamicRoot50();
};

void LbmLoadBitmapTest::testLoad50()
{
    testLoadSmall(false);
}

void LbmLoadBitmapTest::testLoadDynamicRoot50()
{
    testLoadSmall(true);
}

void LbmLoadBitmapTest::testLoadSmall(bool dynamicRootPageId)
{
    // small load
    uint nRows = 50;
    uint nClusters = 4;
    std::vector<int> repeatSeqValues;

    // first column will contain all unique values; the remaining columns
    // have a repeating sequence based on the value set in the vector
    repeatSeqValues.push_back(nRows);
    repeatSeqValues.push_back(5);
    repeatSeqValues.push_back(9);
    repeatSeqValues.push_back(19);
    testLoad(
        nRows, nClusters, repeatSeqValues, true, true, "testLoad50",
        dynamicRootPageId);
}

void LbmLoadBitmapTest::testLoad5000()
{
    uint nRows = 5000;
    uint nClusters = 2;
    std::vector<int> repeatSeqValues;

    // test with a larger number of distinct values to force buffer flushing
    repeatSeqValues.push_back(nRows);
    repeatSeqValues.push_back(200);
    testLoad(
        nRows, nClusters, repeatSeqValues, true, true, "testLoad5000", false);
}

void LbmLoadBitmapTest::testLoad10000()
{
    // larger rowset to force bitmaps exceeding their buffer sizes
    uint nRows = 10000;
    uint nClusters = 4;
    std::vector<int> repeatSeqValues;

    repeatSeqValues.push_back(nRows);
    repeatSeqValues.push_back(5);
    repeatSeqValues.push_back(9);
    repeatSeqValues.push_back(19);
    testLoad(
        nRows, nClusters, repeatSeqValues, true, true, "testLoad10000", false);
}

void LbmLoadBitmapTest::testAppend()
{
    // parameters for test
    uint nRows = 60;
    uint nClusters = 4;
    std::vector<int> repeatSeqValues1;
    std::vector<int> repeatSeqValues2;

    // Set up the column generators for empty load and append.

    // column 1
    repeatSeqValues1.push_back(nRows);
    repeatSeqValues2.push_back(nRows);

    // column 2
    repeatSeqValues1.push_back(23);
    repeatSeqValues2.push_back(31);

    // column 3
    repeatSeqValues1.push_back(1);
    repeatSeqValues2.push_back(2);

    // column 4
    repeatSeqValues1.push_back(7);
    repeatSeqValues2.push_back(29);

    // load into empty btree
    testLoad(
        nRows, nClusters, repeatSeqValues1, true,  true, "testAppendNewRoot",
        false);

    // append some new values
    resetExecStreamTest();
    testLoad(
        nRows, nClusters, repeatSeqValues2, false, true, "testAppendOldRoot",
        false);
}

/**
 * Loads a table with nClusters clusters, 1 column per cluster, and nRows rows.
 * Each column has a repeating sequence of values based on the value in the
 * repeatSeqValues vector.  E.g., a repeating sequence of n will have values:
 * (0, 1, 2, ..., n-1, 0, 1, 2, ..., n-1, 0, 1, 2, ...).
 *
 * Bitmap indexes are then created on each column as well as a multi-key
 * index that is created on all columns.
 *
 * If newRoot is false, this is testing an append to an existing table.
 */
void LbmLoadBitmapTest::testLoad(
    uint nRows, uint nClusters, std::vector<int> &repeatSeqValues, bool newRoot,
    bool dumpEntries, string testName, bool dynamicRootPageId)
{
    // 0. reset member fields.
    for (uint i = 0; i < bTreeClusters.size(); i++) {
        bTreeClusters[i]->segmentAccessor.reset();
    }
    for (uint i = 0; i < bTreeBitmaps.size(); i++) {
        bTreeBitmaps[i]->segmentAccessor.reset();
    }
    bTreeClusters.clear();
    bTreeBitmaps.clear();
    entryDumps.clear();

    // 1. setup mock input stream

    MockProducerExecStreamParams mockParams;
    for (uint i = 0; i < nClusters; i++) {
        mockParams.outputTupleDesc.push_back(attrDesc_int64);
    }
    mockParams.nRows = nRows;

    vector<boost::shared_ptr<ColumnGenerator<int64_t> > > columnGenerators;
    SharedInt64ColumnGenerator col;
    assert(repeatSeqValues.size() == nClusters);
    for (uint i = 0; i < repeatSeqValues.size(); i++) {
        col =
            SharedInt64ColumnGenerator(
                new RepeatingSeqColumnGenerator(repeatSeqValues[i]));
        columnGenerators.push_back(col);
    }
    mockParams.pGenerator.reset(
        new CompositeExecStreamGenerator(columnGenerators));

    ExecStreamEmbryo mockStreamEmbryo;
    mockStreamEmbryo.init(new MockProducerExecStream(), mockParams);
    mockStreamEmbryo.getStream()->setName("MockProducerExecStream");

    // 2. setup splitter stream for cluster loads

    SplitterExecStreamParams splitterParams;
    ExecStreamEmbryo splitterStreamEmbryo;
    splitterStreamEmbryo.init(new SplitterExecStream(), splitterParams);
    splitterStreamEmbryo.getStream()->setName("ClusterSplitterExecStream");

    // 3. setup loader streams

    vector<ExecStreamEmbryo> lcsAppendEmbryos;
    for (uint i = 0; i < nClusters; i++) {
        LcsClusterAppendExecStreamParams lcsAppendParams;
        boost::shared_ptr<BTreeDescriptor> pBTreeDesc =
            boost::shared_ptr<BTreeDescriptor> (new BTreeDescriptor());
        bTreeClusters.push_back(pBTreeDesc);

        // initialize the btree parameter portion of lcsAppendParams
        // BTree tuple desc has two columns (rid, clusterPageid)
        (lcsAppendParams.tupleDesc).push_back(attrDesc_int64);
        (lcsAppendParams.tupleDesc).push_back(attrDesc_int64);

        // BTree key only has one column which is the first column.
        (lcsAppendParams.keyProj).push_back(0);

        initBTreeExecStreamParam(lcsAppendParams, pBTreeDesc);

        // output two values (rows inserted, starting rid value)
        lcsAppendParams.outputTupleDesc.push_back(attrDesc_int64);
        lcsAppendParams.outputTupleDesc.push_back(attrDesc_int64);

        lcsAppendParams.inputProj.push_back(i);

        // create an empty page to start the btree

        if (newRoot) {
            BTreeBuilder builder(*pBTreeDesc, pRandomSegment);
            builder.createEmptyRoot();
            savedBTreeClusterRootIds.push_back(builder.getRootPageId());
        }
        lcsAppendParams.rootPageId = pBTreeDesc->rootPageId =
            savedBTreeClusterRootIds[i];

        // Now use the above initialized parameter

        ExecStreamEmbryo lcsAppendStreamEmbryo;
        lcsAppendStreamEmbryo.init(
            new LcsClusterAppendExecStream(), lcsAppendParams);
        std::ostringstream oss;
        oss << "LcsClusterAppendExecStream" << "#" << i;
        lcsAppendStreamEmbryo.getStream()->setName(oss.str());
        lcsAppendEmbryos.push_back(lcsAppendStreamEmbryo);
    }

    // 4. setup barrier stream for cluster loads

    BarrierExecStreamParams barrierParams;
    barrierParams.outputTupleDesc.push_back(attrDesc_int64);
    barrierParams.outputTupleDesc.push_back(attrDesc_int64);
    barrierParams.returnMode = BARRIER_RET_ANY_INPUT;

    ExecStreamEmbryo clusterBarrierStreamEmbryo;
    clusterBarrierStreamEmbryo.init(new BarrierExecStream(), barrierParams);
    clusterBarrierStreamEmbryo.getStream()->setName("ClusterBarrierExecStream");

    // create a DAG with the above
    prepareDAG(
        mockStreamEmbryo, splitterStreamEmbryo, lcsAppendEmbryos,
        clusterBarrierStreamEmbryo);

    // 5. setup splitter stream for create bitmaps

    splitterStreamEmbryo.init(
        new SplitterExecStream(), splitterParams);
    splitterStreamEmbryo.getStream()->setName("BitmapSplitterExecStream");

    // create streams for bitmap generator, sort, and bitmap splicer,
    // 1 index on each column and then an index on all columns

    std::vector<std::vector<ExecStreamEmbryo> > createBitmapStreamList;
    for (uint i = 0; i < nClusters + 1; i++) {
        if (i == 1 && nClusters == 1) {
            /*
             * There's only one column.
             * Do not bother to build the composite index.
             */
            break;
        }

        std::vector<ExecStreamEmbryo> createBitmapStream;

        // 6. setup generator

        LbmGeneratorExecStreamParams generatorParams;
        struct LcsClusterScanDef clusterScanDef;
        clusterScanDef.clusterTupleDesc.push_back(attrDesc_int64);

        // first nCluster generators only scan a single column; the
        // last one scans all columns
        if (i < nClusters) {
            DynamicParamId paramId =
                (dynamicRootPageId)
                ? DynamicParamId(nClusters + i + 2)
                : DynamicParamId(0);
            initClusterScanDef(generatorParams, clusterScanDef, i, paramId);
        } else {
            for (uint j = 0; j < nClusters; j++) {
                DynamicParamId paramId =
                    (dynamicRootPageId)
                    ? DynamicParamId(nClusters + 2 + j)
                    : DynamicParamId(0);
                initClusterScanDef(generatorParams, clusterScanDef, j, paramId);
            }
        }

        TupleProjection proj;
        if (i < nClusters) {
            proj.push_back(0);
        } else {
            for (uint j = 0; j < nClusters; j++) {
                proj.push_back(j);
            }
        }
        generatorParams.outputProj = proj;
        generatorParams.insertRowCountParamId = DynamicParamId(i + 1);
        generatorParams.createIndex = false;

        boost::shared_ptr<BTreeDescriptor> pBTreeDesc =
            boost::shared_ptr<BTreeDescriptor> (new BTreeDescriptor());
        bTreeBitmaps.push_back(pBTreeDesc);

        /*
         * Setup the dump objects.
         */
        if (dumpEntries) {
            ostringstream traceName;
            traceName << testName << " Index " << i;
            boost::shared_ptr<LbmEntryDump> pEntryDump =
                boost::shared_ptr<LbmEntryDump>(
                    new LbmEntryDump(
                        TRACE_INFO,
                        shared_from_this(),
                        traceName.str()));
            entryDumps.push_back(pEntryDump);
        }

        // BTree tuple desc has the key columns + starting Rid + varbinary
        // field for bit segments/bit descriptors
        uint nKeys;
        if (i < nClusters) {
            nKeys = 2;
        } else {
            nKeys = nClusters + 1;
        }
        initBTreeTupleDesc(generatorParams.outputTupleDesc, nKeys);

        initBTreeBitmapDesc(
            generatorParams.tupleDesc, generatorParams.keyProj, nKeys);
        initBTreeExecStreamParam(generatorParams, pBTreeDesc);

        // create an empty page to start the btree

        if (newRoot) {
            BTreeBuilder builder(*pBTreeDesc, pRandomSegment);
            builder.createEmptyRoot();
            savedBTreeBitmapRootIds.push_back(builder.getRootPageId());
        }
        generatorParams.rootPageId = pBTreeDesc->rootPageId =
            savedBTreeBitmapRootIds[i];
        if (dynamicRootPageId && i < nClusters) {
            SharedDynamicParamManager pDynamicParamManager =
                pGraph->getDynamicParamManager();
            DynamicParamId paramId = DynamicParamId(nClusters + i + 2);
            pDynamicParamManager->createParam(paramId, attrDesc_int64);
            TupleDatum pageIdDatum;
            PageId rootPageId = savedBTreeBitmapRootIds[i];
            pageIdDatum.pData = (PConstBuffer) &rootPageId;
            pageIdDatum.cbData = sizeof(PageId);
            pDynamicParamManager->writeParam(paramId, pageIdDatum);
        }

        ExecStreamEmbryo generatorStreamEmbryo;
        generatorStreamEmbryo.init(
            new LbmGeneratorExecStream(), generatorParams);
        std::ostringstream oss;
        oss << "LbmGeneratorExecStream" << "#" << i;
        generatorStreamEmbryo.getStream()->setName(oss.str());
        createBitmapStream.push_back(generatorStreamEmbryo);

        // 7. setup sorter

        ExternalSortExecStreamParams sortParams;
        initBTreeBitmapDesc(
            sortParams.outputTupleDesc, sortParams.keyProj, nKeys);
        sortParams.distinctness = DUP_ALLOW;
        sortParams.pTempSegment = pRandomSegment;
        sortParams.pCacheAccessor = pCache;
        sortParams.scratchAccessor =
            pSegmentFactory->newScratchSegment(pCache, 10);
        sortParams.storeFinalRun = false;
        sortParams.partitionKeyCount = 0;
        sortParams.estimatedNumRows = MAXU;
        sortParams.earlyClose = false;

        ExecStreamEmbryo sortStreamEmbryo;
        sortStreamEmbryo.init(
            ExternalSortExecStream::newExternalSortExecStream(), sortParams);
        sortStreamEmbryo.getStream()->setName("ExternalSortExecStream");
        std::ostringstream oss2;
        oss2 << "ExternalSortExecStream" << "#" << i;
        sortStreamEmbryo.getStream()->setName(oss2.str());
        createBitmapStream.push_back(sortStreamEmbryo);

        // 8. setup splicer

        LbmSplicerExecStreamParams splicerParams;
        splicerParams.createNewIndex = false;
        splicerParams.scratchAccessor =
            pSegmentFactory->newScratchSegment(pCache, 15);
        splicerParams.pCacheAccessor = pCache;
        BTreeParams bTreeParams;
        initBTreeBitmapDesc(
            bTreeParams.tupleDesc, bTreeParams.keyProj, nKeys);
        initBTreeParam(bTreeParams, pBTreeDesc);
        bTreeParams.rootPageId = pBTreeDesc->rootPageId;
        splicerParams.bTreeParams.push_back(bTreeParams);
        splicerParams.insertRowCountParamId = DynamicParamId(i + 1);
        splicerParams.writeRowCountParamId = DynamicParamId(0);
        splicerParams.outputTupleDesc.push_back(attrDesc_int64);

        ExecStreamEmbryo splicerStreamEmbryo;
        splicerStreamEmbryo.init(new LbmSplicerExecStream(), splicerParams);
        std::ostringstream oss3;
        oss3 << "LbmSplicerExecStream" << "#" << i;
        splicerStreamEmbryo.getStream()->setName(oss3.str());
        createBitmapStream.push_back(splicerStreamEmbryo);

        // connect the sorter and splicer to generator and then add this
        // newly connected stream to the list of create bitmap stream embryos
        createBitmapStreamList.push_back(createBitmapStream);
    }

    // 9. setup barrier stream for create bitmaps

    barrierParams.outputTupleDesc.clear();
    barrierParams.outputTupleDesc.push_back(attrDesc_int64);

    ExecStreamEmbryo barrierStreamEmbryo;
    barrierStreamEmbryo.init(
        new BarrierExecStream(), barrierParams);
    barrierStreamEmbryo.getStream()->setName("BitmapBarrierExecStream");

    // create the bitmap stream graph, with the load stream graph from
    // above as the source
    SharedExecStream pOutputStream = prepareDAG(
        clusterBarrierStreamEmbryo, splitterStreamEmbryo,
        createBitmapStreamList, barrierStreamEmbryo, false);

    // set up a generator which can produce the expected output
    RampExecStreamGenerator expectedResultGenerator(mockParams.nRows);

    verifyOutput(*pOutputStream, 1, expectedResultGenerator);

    if (dumpEntries) {
        for (uint i = 0; i < entryDumps.size(); i++) {
            entryDumps[i]->dump(*(bTreeBitmaps[i].get()), true);
        }
    }
}

void LbmLoadBitmapTest::initBTreeExecStreamParam(
    BTreeExecStreamParams &param, shared_ptr<BTreeDescriptor> pBTreeDesc)
{
    param.scratchAccessor = pSegmentFactory->newScratchSegment(pCache, 15);
    param.pCacheAccessor = pCache;
    initBTreeParam(param, pBTreeDesc);
}

void LbmLoadBitmapTest::initBTreeParam(
    BTreeParams &param, shared_ptr<BTreeDescriptor> pBTreeDesc)
{
    param.pSegment = pRandomSegment;
    param.pRootMap = 0;
    param.rootPageIdParamId = DynamicParamId(0);

    pBTreeDesc->segmentAccessor.pSegment = param.pSegment;
    pBTreeDesc->segmentAccessor.pCacheAccessor = pCache;
    pBTreeDesc->tupleDescriptor = param.tupleDesc;
    pBTreeDesc->keyProjection = param.keyProj;
    param.pageOwnerId = pBTreeDesc->pageOwnerId;
    param.segmentId = pBTreeDesc->segmentId;
}

void LbmLoadBitmapTest::initClusterScanDef(
    LbmGeneratorExecStreamParams &generatorParams,
    struct LcsClusterScanDef &clusterScanDef,
    uint bTreeIndex,
    DynamicParamId paramId)
{
    clusterScanDef.pSegment =
        bTreeClusters[bTreeIndex]->segmentAccessor.pSegment;
    clusterScanDef.pCacheAccessor =
        bTreeClusters[bTreeIndex]->segmentAccessor.pCacheAccessor;
    clusterScanDef.tupleDesc = bTreeClusters[bTreeIndex]->tupleDescriptor;
    clusterScanDef.keyProj = bTreeClusters[bTreeIndex]->keyProjection;
    clusterScanDef.rootPageIdParamId = paramId;
    clusterScanDef.rootPageId =
        (opaqueToInt(paramId) > 0)
        ? NULL_PAGE_ID
        : bTreeClusters[bTreeIndex]->rootPageId;
    clusterScanDef.pageOwnerId = bTreeClusters[bTreeIndex]->pageOwnerId;
    clusterScanDef.segmentId = bTreeClusters[bTreeIndex]->segmentId;
    clusterScanDef.pRootMap = 0;
    generatorParams.lcsClusterScanDefs.push_back(clusterScanDef);
}

void LbmLoadBitmapTest::initBTreeBitmapDesc(
    TupleDescriptor &tupleDesc, TupleProjection &keyProj, uint nKeys)
{
    initBTreeTupleDesc(tupleDesc, nKeys);

    // btree key consists of the key columns + starting rid
    for (uint j = 0; j < nKeys; j++) {
        keyProj.push_back(j);
    }
}

void LbmLoadBitmapTest::initBTreeTupleDesc(
    TupleDescriptor &tupleDesc, uint nKeys)
{
    for (uint i = 0; i < nKeys; i++) {
        tupleDesc.push_back(attrDesc_int64);
    }
    uint varColSize;

    // The default page size is 4K.
    varColSize = pRandomSegment->getUsablePageSize() / 8;
    // varColSize = 256;

    tupleDesc.push_back(
        TupleAttributeDescriptor(
            stdTypeFactory.newDataType(STANDARD_TYPE_VARBINARY), true,
            varColSize));
    tupleDesc.push_back(
        TupleAttributeDescriptor(
            stdTypeFactory.newDataType(STANDARD_TYPE_VARBINARY), true,
            varColSize));
}

void LbmLoadBitmapTest::testCaseSetUp()
{
    ExecStreamUnitTestBase::testCaseSetUp();

    attrDesc_int64 = TupleAttributeDescriptor(
        stdTypeFactory.newDataType(STANDARD_TYPE_INT_64));
}

void LbmLoadBitmapTest::testCaseTearDown()
{
    for (uint i = 0; i < bTreeClusters.size(); i++) {
        bTreeClusters[i]->segmentAccessor.reset();
    }
    for (uint i = 0; i < bTreeBitmaps.size(); i++) {
        bTreeBitmaps[i]->segmentAccessor.reset();
    }
    bTreeClusters.clear();
    bTreeBitmaps.clear();
    savedBTreeClusterRootIds.clear();
    savedBTreeBitmapRootIds.clear();
    entryDumps.clear();

    ExecStreamUnitTestBase::testCaseTearDown();
}

FENNEL_UNIT_TEST_SUITE(LbmLoadBitmapTest);

// End LbmLoadBitmapTest.cpp
