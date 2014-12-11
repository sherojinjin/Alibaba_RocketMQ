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

    private boolean hasTokens;

    private static final int WARNING_INTERVAL = 100;

    public BatchSendMessageTask(Message[] messages, SendCallback sendCallback,
                                MultiThreadMQProducer multiThreadMQProducer, boolean hasTokens) {
        this.messages = messages;
        this.sendCallback = sendCallback;
        this.multiThreadMQProducer = multiThreadMQProducer;
        this.hasTokens = hasTokens;
    }

    @Override
    public void run() {

        LOGGER.debug("Batch sending " + (null == messages ? 0 : messages.length) + " messages. "
                + (hasTokens ? "Without": "With ") + " tokens from semaphore");

        for (Message message : messages) {
            if (null == message) {
                continue;
            }
            try {
                if (hasTokens) { //We already got token from semaphore somewhere.
                    multiThreadMQProducer.getDefaultMQProducer().send(message,
                            new SendMessageCallback(multiThreadMQProducer, sendCallback, message));
                } else if (multiThreadMQProducer.getSemaphore().tryAcquire()) { //Try to acquire a token from semaphore.
                    multiThreadMQProducer.getDefaultMQProducer().send(message,
                            new SendMessageCallback(multiThreadMQProducer, sendCallback, message));
                } else { //Stash message if no token is available.
                    long  num = multiThreadMQProducer.getNumberOfMessageStashedDueToLackOfSemaphoreToken().incrementAndGet();
                    if (num % WARNING_INTERVAL == 0) {
                        LOGGER.warn("Messages stashed due to lack of semaphore token. Total: {}", num);
                    }
                    multiThreadMQProducer.getLocalMessageStore().stash(message);
                }
            } catch (Exception e) {
                multiThreadMQProducer.handleSendMessageFailure(message, e);
            }
        }

    }
}