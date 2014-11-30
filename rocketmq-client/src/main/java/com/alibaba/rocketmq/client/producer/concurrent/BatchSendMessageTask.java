package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;

class BatchSendMessageTask implements Runnable {

    private Message[] messages;

    private SendCallback sendCallback;

    private MultiThreadMQProducer multiThreadMQProducer;

    public BatchSendMessageTask(Message[] messages, SendCallback sendCallback, MultiThreadMQProducer multiThreadMQProducer) {
        this.messages = messages;
        this.sendCallback = sendCallback;
        this.multiThreadMQProducer = multiThreadMQProducer;
    }

    @Override
    public void run() {
        for (Message message : messages) {

            if (null == message) {
                continue;
            }

            try {
                multiThreadMQProducer.getDefaultMQProducer().send(message,
                        new SendMessageCallback(multiThreadMQProducer, sendCallback, message));
            } catch (MQClientException e) {
                multiThreadMQProducer.handleSendMessageFailure(message, e);
            } catch (RemotingException e) {
                multiThreadMQProducer.handleSendMessageFailure(message, e);
            } catch (InterruptedException e) {
                multiThreadMQProducer.handleSendMessageFailure(message, e);
            }
        }

    }
}