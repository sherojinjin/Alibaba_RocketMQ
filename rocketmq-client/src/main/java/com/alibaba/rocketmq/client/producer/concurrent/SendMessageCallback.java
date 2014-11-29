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
        if (null != hook) {
            hook.onSuccess(sendResult);
        }
    }

    @Override
    public void onException(Throwable e) {
        multiThreadMQProducer.handleSendMessageFailure(message, e);
    }
}
