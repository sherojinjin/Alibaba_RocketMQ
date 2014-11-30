package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;

import java.util.concurrent.atomic.AtomicLong;

public class ExampleSendCallback implements SendCallback {

    private MultiThreadMQProducer producer;

    private AtomicLong successfulSentCounter;

    private long total;

    public ExampleSendCallback(MultiThreadMQProducer producer, AtomicLong successfulSentCounter, long total) {
        this.producer = producer;
        this.successfulSentCounter = successfulSentCounter;
        this.total = total;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        System.out.println("ExampleSendCallback#onSuccess:" + successfulSentCounter.incrementAndGet() +
                " sent OK. " + sendResult);
        if (successfulSentCounter.longValue() >= total && null != producer) {
            System.out.println("All Messages Sent Successfully");
        }
    }

    @Override
    public void onException(Throwable e) {
        System.out.println("Failure occurred. Now " + successfulSentCounter.longValue() + " sent OK.");
        e.printStackTrace();
    }
}
