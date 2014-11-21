/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.store.index;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 索引文件头
 *
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class IndexHeader {
    public static final int INDEX_HEADER_SIZE = 40;
    private static int BEGIN_TIMESTAMP_INDEX = 0;
    private static int END_TIMESTAMP_INDEX = 8;
    private static int BEGIN_PHY_OFFSET_INDEX = 16;
    private static int END_PHY_OFFSET_INDEX = 24;
    private static int HASH_SLOT_COUNT_INDEX = 32;
    private static int INDEX_COUNT_INDEX = 36;
    private final ByteBuffer byteBuffer;
    private AtomicLong beginTimestamp = new AtomicLong(0);
    private AtomicLong endTimestamp = new AtomicLong(0);
    private AtomicLong beginPhyOffset = new AtomicLong(0);
    private AtomicLong endPhyOffset = new AtomicLong(0);
    private AtomicInteger hashSlotCount = new AtomicInteger(0);
    // 第一个索引是无效索引
    private AtomicInteger indexCount = new AtomicInteger(1);


    public IndexHeader(final ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }


    public void load() {
        this.beginTimestamp.set(byteBuffer.getLong(BEGIN_TIMESTAMP_INDEX));
        this.endTimestamp.set(byteBuffer.getLong(END_TIMESTAMP_INDEX));
        this.beginPhyOffset.set(byteBuffer.getLong(BEGIN_PHY_OFFSET_INDEX));
        this.endPhyOffset.set(byteBuffer.getLong(END_PHY_OFFSET_INDEX));

        this.hashSlotCount.set(byteBuffer.getInt(HASH_SLOT_COUNT_INDEX));
        this.indexCount.set(byteBuffer.getInt(INDEX_COUNT_INDEX));

        if (this.indexCount.get() <= 0) {
            this.indexCount.set(1);
        }
    }


    /**
     * 更新byteBuffer
     */
    public void updateByteBuffer() {
        this.byteBuffer.putLong(BEGIN_TIMESTAMP_INDEX, this.beginTimestamp.get());
        this.byteBuffer.putLong(END_TIMESTAMP_INDEX, this.endTimestamp.get());
        this.byteBuffer.putLong(BEGIN_PHY_OFFSET_INDEX, this.beginPhyOffset.get());
        this.byteBuffer.putLong(END_PHY_OFFSET_INDEX, this.endPhyOffset.get());
        this.byteBuffer.putInt(HASH_SLOT_COUNT_INDEX, this.hashSlotCount.get());
        this.byteBuffer.putInt(INDEX_COUNT_INDEX, this.indexCount.get());
    }


    public long getBeginTimestamp() {
        return beginTimestamp.get();
    }


    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp.set(beginTimestamp);
        this.byteBuffer.putLong(BEGIN_TIMESTAMP_INDEX, beginTimestamp);
    }


    public long getEndTimestamp() {
        return endTimestamp.get();
    }


    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp.set(endTimestamp);
        this.byteBuffer.putLong(END_TIMESTAMP_INDEX, endTimestamp);
    }


    public long getBeginPhyOffset() {
        return beginPhyOffset.get();
    }


    public void setBeginPhyOffset(long beginPhyOffset) {
        this.beginPhyOffset.set(beginPhyOffset);
        this.byteBuffer.putLong(BEGIN_PHY_OFFSET_INDEX, beginPhyOffset);
    }


    public long getEndPhyOffset() {
        return endPhyOffset.get();
    }


    public void setEndPhyOffset(long endPhyOffset) {
        this.endPhyOffset.set(endPhyOffset);
        this.byteBuffer.putLong(END_PHY_OFFSET_INDEX, endPhyOffset);
    }


    public AtomicInteger getHashSlotCount() {
        return hashSlotCount;
    }


    public void incHashSlotCount() {
        int value = this.hashSlotCount.incrementAndGet();
        this.byteBuffer.putInt(HASH_SLOT_COUNT_INDEX, value);
    }


    public int getIndexCount() {
        return indexCount.get();
    }


    public void incIndexCount() {
        int value = this.indexCount.incrementAndGet();
        this.byteBuffer.putInt(INDEX_COUNT_INDEX, value);
    }
}
