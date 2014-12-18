package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;

public interface LocalMessageStore {

    /**
     * Stash a new message.
     * @param message Message to stash.
     */
    void stash(Message message);

    /**
     * This method returns numbers of messages stashed.
     * @return number of messages stashed.
     */
    public int getNumberOfMessageStashed();

    /**
     * This method would pop out at most <code>n</code> messages from local store.
     * @param n Number of messages assumed to be popped out.
     * @return Array of messages.
     */
    Message[] pop(int n);

    /**
     * Close this message store.
     */
    void close() throws InterruptedException;

    boolean isReady();
}
