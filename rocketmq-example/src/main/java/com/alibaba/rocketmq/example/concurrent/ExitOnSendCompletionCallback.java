package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class ExitOnSendCompletionCallback implements SendCallback {

    private MultiThreadMQProducer producer;

    private AtomicLong successfulSentCounter;

    private long total;

    public ExitOnSendCompletionCallback(MultiThreadMQProducer producer, AtomicLong successfulSentCounter, long total) {
        this.producer = producer;
        this.successfulSentCounter = successfulSentCounter;
        this.total = total;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        System.out.println("ExitOnSendCompletionCallback#onSuccess:" + successfulSentCounter.incrementAndGet() +
                " sent OK. " + sendResult);
        if (successfulSentCounter.longValue() >= total && null != producer) {
            try {
                producer.shutdown();
            } catch (RejectedExecutionException e) {
                System.exit(1);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onException(Throwable e) {
        System.out.println("Failure occurred. Now " + successfulSentCounter.longValue() + " sent OK.");
        e.printStackTrace();
    }
}
