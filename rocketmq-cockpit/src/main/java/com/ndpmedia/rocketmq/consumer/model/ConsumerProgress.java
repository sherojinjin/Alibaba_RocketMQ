package com.ndpmedia.rocketmq.consumer.model;

import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.message.MessageQueue;

/**
 * the offset between consumer and broker.
 */
public class ConsumerProgress
{
    private MessageQueue messageQueue;
    private OffsetWrapper offsetWrapper;
    private long diff;

    public ConsumerProgress() {
    }

    public ConsumerProgress(MessageQueue messageQueue, OffsetWrapper offsetWrapper, long diff) {
        this.messageQueue = messageQueue;
        this.offsetWrapper = offsetWrapper;
        this.diff = diff;
    }

    public long getDiff() {
        return diff;
    }

    public void setDiff(long diff) {
        this.diff = diff;
    }

    public MessageQueue getMessageQueue() {

        return messageQueue;
    }

    public OffsetWrapper getOffsetWrapper() {
        return offsetWrapper;
    }

    public void setOffsetWrapper(OffsetWrapper offsetWrapper) {
        this.offsetWrapper = offsetWrapper;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public String toString() {
        return "ConsumerProgress{" +
                "messageQueue=" + messageQueue +
                ", offsetWrapper=" + offsetWrapper +
                ", diff=" + diff +
                '}';
    }
}
