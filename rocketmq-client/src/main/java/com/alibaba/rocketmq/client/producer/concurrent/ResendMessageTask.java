package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;

public class ResendMessageTask implements Runnable {

    private LocalMessageStore localMessageStore;

    private MultiThreadMQProducer multiThreadMQProducer;

    public ResendMessageTask(LocalMessageStore localMessageStore, MultiThreadMQProducer multiThreadMQProducer) {
        this.localMessageStore = localMessageStore;
        this.multiThreadMQProducer = multiThreadMQProducer;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        Message[] messages = localMessageStore.pop();
        if (null != messages) {
            multiThreadMQProducer.send(messages);
        }
    }
}
