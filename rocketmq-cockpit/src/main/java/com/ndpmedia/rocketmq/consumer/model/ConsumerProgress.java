package com.ndpmedia.rocketmq.consumer.model;

import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.message.MessageQueue;

/**
 * the offset between consumer and broker.
 */
public class ConsumerProgress {
    private String topic;
    private String brokerName;
    private int queueId;
    private long brokerOffset;
    private long consumerOffset;
    // 消费的最后一条消息对应的时间戳
    private long lastTimestamp;
    private long diff;

    public ConsumerProgress() {
    }

    public ConsumerProgress(MessageQueue messageQueue, OffsetWrapper offsetWrapper, long diff) {
        this.topic = messageQueue.getTopic();
        this.brokerName = messageQueue.getBrokerName();
        this.queueId = messageQueue.getQueueId();
        this.brokerOffset = offsetWrapper.getBrokerOffset();
        this.consumerOffset = offsetWrapper.getConsumerOffset();
        this.lastTimestamp = offsetWrapper.getLastTimestamp();
        this.diff = diff;
    }

    public long getDiff() {
        return diff;
    }

    public void setDiff(long diff) {
        this.diff = diff;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public long getBrokerOffset() {
        return brokerOffset;
    }

    public void setBrokerOffset(long brokerOffset) {
        this.brokerOffset = brokerOffset;
    }

    public long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    @Override
    public String toString() {
        return "ConsumerProgress{" +
                "topic='" + topic + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", queueId=" + queueId +
                ", brokerOffset=" + brokerOffset +
                ", consumerOffset=" + consumerOffset +
                ", lastTimestamp=" + lastTimestamp +
                ", diff=" + diff +
                '}';
    }
}
