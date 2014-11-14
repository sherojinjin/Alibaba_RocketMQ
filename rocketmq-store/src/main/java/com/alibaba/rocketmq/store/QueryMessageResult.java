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
package com.alibaba.rocketmq.store;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * 通过Key查询消息，返回结果
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-21
 */
public class QueryMessageResult {
    // 多个连续的消息集合
    private final List<SelectMappedBufferResult> messageMappedList =
            new ArrayList<SelectMappedBufferResult>(100);
    // 用来向Consumer传送消息
    private final List<ByteBuffer> messageBufferList = new ArrayList<ByteBuffer>(100);
    private long indexLastUpdateTimestamp;
    private long indexLastUpdatePhyOffset;
    // ByteBuffer 总字节数
    private int bufferTotalSize = 0;


    public void addMessage(final SelectMappedBufferResult mappedBuffer) {
        this.messageMappedList.add(mappedBuffer);
        this.messageBufferList.add(mappedBuffer.getByteBuffer());
        this.bufferTotalSize += mappedBuffer.getSize();
    }


    public void release() {
        for (SelectMappedBufferResult select : this.messageMappedList) {
            select.release();
        }
    }


    public long getIndexLastUpdateTimestamp() {
        return indexLastUpdateTimestamp;
    }


    public void setIndexLastUpdateTimestamp(long indexLastUpdateTimestamp) {
        this.indexLastUpdateTimestamp = indexLastUpdateTimestamp;
    }


    public long getIndexLastUpdatePhyOffset() {
        return indexLastUpdatePhyOffset;
    }


    public void setIndexLastUpdatePhyOffset(long indexLastUpdatePhyOffset) {
        this.indexLastUpdatePhyOffset = indexLastUpdatePhyOffset;
    }


    public List<ByteBuffer> getMessageBufferList() {
        return messageBufferList;
    }


    public int getBufferTotalSize() {
        return bufferTotalSize;
    }
}
