package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

class BatchSendMessageTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private Message[] messages;

    private SendCallback sendCallback;

    private MultiThreadMQProducer multiThreadMQProducer;

    public BatchSendMessageTask(Message[] messages, SendCallback sendCallback,
                                MultiThreadMQProducer multiThreadMQProducer) {
        this.messages = messages;
        this.sendCallback = sendCallback;
        this.multiThreadMQProducer = multiThreadMQProducer;
    }

    @Override
    public void run() {

        LOGGER.debug("Batch sending " + (null == messages ? 0 : messages.length) + " messages. ");

        for (Message message : messages) {
            if (null == message) {
                continue;
            }
            try {
                multiThreadMQProducer.getDefaultMQProducer().send(message,
                        new SendMessageCallback(multiThreadMQProducer, sendCallback, message));
            } catch (Exception e) {
                multiThreadMQProducer.handleSendMessageFailure(message, e);
            }
        }

    }
}