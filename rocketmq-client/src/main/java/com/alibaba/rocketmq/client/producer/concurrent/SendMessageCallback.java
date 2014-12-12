package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

public class SendMessageCallback implements SendCallback {

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
            hook.onSuccess(sendResult);
        }
    }

    @Override
    public void onException(Throwable e) {
        //We need to release the semaphore token.
        //multiThreadMQProducer.getSemaphore().release();

        //Stash the message and log the exception.
        multiThreadMQProducer.handleSendMessageFailure(message, e);
        if (null != hook) {
            hook.onException(e);
        }
    }
}
