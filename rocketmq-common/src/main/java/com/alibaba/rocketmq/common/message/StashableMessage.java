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
package com.alibaba.rocketmq.common.message;

import com.alibaba.rocketmq.common.TopicFilterType;
import com.alibaba.rocketmq.common.sysflag.MessageSysFlag;

import java.io.Serializable;

public class StashableMessage extends Message implements Serializable{
    private static final long serialVersionUID = 5720810158625748049L;

    // 队列ID <PUT>
    private int queueId;
    // 存储记录大小
    private int storeSize;
    // 队列偏移量
    private long queueOffset;
    // 消息标志位 <PUT>
    private int sysFlag;
    // 消息在客户端创建时间戳 <PUT>
    private long bornTimestamp;
    // 消息在服务器存储时间戳
    private long storeTimestamp;
    // 消息ID
    private String msgId;
    // 消息对应的Commit Log Offset
    private long commitLogOffset;
    // 消息体CRC
    private int bodyCRC;
    // 当前消息被某个订阅组重新消费了几次（订阅组之间独立计数）
    private int reconsumeTimes;

    private long preparedTransactionOffset;

    public StashableMessage() {
    }

    @Override
    public StashableMessage buildStashableMessage() {
        return this;
    }

    public StashableMessage(int queueId, long bornTimestamp, long storeTimestamp, String msgId) {
        this.queueId = queueId;
        this.bornTimestamp = bornTimestamp;
        this.storeTimestamp = storeTimestamp;
        this.msgId = msgId;
    }

    public MessageExt buildMessageExt() {
        MessageExt messageExt = new MessageExt();
        messageExt.setTopic(getTopic());
        messageExt.setBody(getBody());
        messageExt.setFlag(getFlag());
        messageExt.setProperties(getProperties());

        messageExt.setQueueId(queueId);
        messageExt.setStoreSize(storeSize);
        messageExt.setQueueOffset(queueOffset);
        messageExt.setSysFlag(sysFlag);
        messageExt.setBornTimestamp(bornTimestamp);
        messageExt.setStoreTimestamp(storeTimestamp);
        messageExt.setMsgId(msgId);
        messageExt.setCommitLogOffset(commitLogOffset);
        messageExt.setBodyCRC(bodyCRC);
        messageExt.setReconsumeTimes(reconsumeTimes);
        messageExt.setPreparedTransactionOffset(preparedTransactionOffset);

        return messageExt;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }


    public long getBornTimestamp() {
        return bornTimestamp;
    }


    public void setBornTimestamp(long bornTimestamp) {
        this.bornTimestamp = bornTimestamp;
    }

    public long getStoreTimestamp() {
        return storeTimestamp;
    }


    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }


    public String getMsgId() {
        return msgId;
    }


    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    public int getSysFlag() {
        return sysFlag;
    }


    public void setSysFlag(int sysFlag) {
        this.sysFlag = sysFlag;
    }


    public int getBodyCRC() {
        return bodyCRC;
    }


    public void setBodyCRC(int bodyCRC) {
        this.bodyCRC = bodyCRC;
    }


    public long getQueueOffset() {
        return queueOffset;
    }


    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }


    public long getCommitLogOffset() {
        return commitLogOffset;
    }


    public void setCommitLogOffset(long physicOffset) {
        this.commitLogOffset = physicOffset;
    }


    public int getStoreSize() {
        return storeSize;
    }


    public void setStoreSize(int storeSize) {
        this.storeSize = storeSize;
    }


    public static TopicFilterType parseTopicFilterType(final int sysFlag) {
        if ((sysFlag & MessageSysFlag.MultiTagsFlag) == MessageSysFlag.MultiTagsFlag) {
            return TopicFilterType.MULTI_TAG;
        }

        return TopicFilterType.SINGLE_TAG;
    }


    public int getReconsumeTimes() {
        return reconsumeTimes;
    }


    public void setReconsumeTimes(int reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }


    public long getPreparedTransactionOffset() {
        return preparedTransactionOffset;
    }


    public void setPreparedTransactionOffset(long preparedTransactionOffset) {
        this.preparedTransactionOffset = preparedTransactionOffset;
    }


    @Override
    public String toString() {
        return "MessageExt [queueId=" + queueId + ", storeSize=" + storeSize + ", queueOffset=" + queueOffset
                + ", sysFlag=" + sysFlag + ", bornTimestamp=" + bornTimestamp
                + ", storeTimestamp=" + storeTimestamp + ", msgId=" + msgId
                + ", commitLogOffset=" + commitLogOffset + ", bodyCRC=" + bodyCRC + ", reconsumeTimes="
                + reconsumeTimes + ", preparedTransactionOffset=" + preparedTransactionOffset
                + ", toString()=" + super.toString() + "]";
    }
}
