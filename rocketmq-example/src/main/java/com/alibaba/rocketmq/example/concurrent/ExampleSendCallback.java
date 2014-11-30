package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;

import java.util.concurrent.atomic.AtomicLong;

public class ExampleSendCallback implements SendCallback {

    private AtomicLong successfulSentCounter;

    private long total;

    private long start;

    public ExampleSendCallback(AtomicLong successfulSentCounter, long total, long startTime) {
        this.successfulSentCounter = successfulSentCounter;
        this.total = total;
        this.start = startTime;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        //System.out.println("ExampleSendCallback#onSuccess:" + successfulSentCounter.incrementAndGet() + " sent OK. " + sendResult);
        if (successfulSentCounter.incrementAndGet() >= total) {
            System.out.println("All Messages Sent Successfully. TPS:" + (total * 1000L / (System.currentTimeMillis() - start)));
        }
    }

    @Override
    public void onException(Throwable e) {
        System.out.println("Failure occurred. Now " + successfulSentCounter.longValue() + " sent OK.");
        e.printStackTrace();
    }
}
