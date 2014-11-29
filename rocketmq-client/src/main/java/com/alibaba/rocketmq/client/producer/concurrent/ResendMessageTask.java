package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;

public class ResendMessageTask implements Runnable {

    private LocalMessageStore localMessageStore;

    private MultiThreadMQProducer multiThreadMQProducer;

    public ResendMessageTask(LocalMessageStore localMessageStore, MultiThreadMQProducer multiThreadMQProducer) {
        this.localMessageStore = localMessageStore;
        this.multiThreadMQProducer = multiThreadMQProducer;
    }

    @Override
    public void run() {
        System.out.println("Begin to resend!");
        Message[] messages = localMessageStore.pop();
        System.out.println("Found " + (null == messages ? 0 : messages.length));
        if (null != messages) {
            multiThreadMQProducer.send(messages);
        }
    }
}
