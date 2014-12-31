package com.ndpmedia.rocketmq.consumer.model;

import com.alibaba.rocketmq.common.message.MessageQueue;

/**
 * Created by Administrator on 2014/12/31.
 */
public class ConsumerProgress
{
    private MessageQueue messageQueue;
    private long diff;

    public ConsumerProgress() {
    }

    public ConsumerProgress(MessageQueue messageQueue, long diff) {
        this.messageQueue = messageQueue;
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

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }
}
