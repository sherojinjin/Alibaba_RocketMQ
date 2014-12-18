package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

public class SendMessageCallback implements SendCallback {

    private static final Logger LOGGER = ClientLogger.getLog();

    private SendCallback hook;

    private Message message;

    private MultiThreadMQProducer multiThreadMQProducer;

    public SendMessageCallback(MultiThreadMQProducer multiThreadMQProducer, SendCallback sendCallback, Message message) {
        this.hook = sendCallback;
        this.message = message;
        this.multiThreadMQProducer = multiThreadMQProducer;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        //Release the semaphore token.
        multiThreadMQProducer.getSemaphore().release();

        //Update statistical data.
        multiThreadMQProducer.getSuccessSendingCounter().incrementAndGet();

        //Execute user callback.
        if (null != hook) {
            try {
                hook.onSuccess(sendResult);
            } catch (Exception e) {
                LOGGER.error("Error while invoke user callback", e);
            }
        }
    }

    @Override
    public void onException(Throwable e) {
        //Stash the message and log the exception.
        multiThreadMQProducer.handleSendMessageFailure(message, e);
        if (null != hook) {
            try {
                hook.onException(e);
            } catch (Exception ex) {
                LOGGER.error("Error while invoke user callback", ex);
            }

        }
    }
}
