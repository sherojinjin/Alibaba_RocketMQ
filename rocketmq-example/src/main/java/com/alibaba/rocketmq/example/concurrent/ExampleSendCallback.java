package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class ExampleSendCallback implements SendCallback {

    private AtomicLong successfulSentCounter;

    private long total;

    private long start;

    private long previousStatsTime;

    private static final long COUNT_INTERVAL = 1000L;


    public ExampleSendCallback(AtomicLong successfulSentCounter, long total, long startTime) {
        this.successfulSentCounter = successfulSentCounter;
        this.total = total;
        this.start = startTime;
        previousStatsTime = startTime;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        successfulSentCounter.incrementAndGet();

        if (successfulSentCounter.longValue() % COUNT_INTERVAL == 0) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(new Date()) + " TPS:" + COUNT_INTERVAL * 1000 / (System.currentTimeMillis() - previousStatsTime));
            previousStatsTime = System.currentTimeMillis();
        }


        //System.out.println("ExampleSendCallback#onSuccess:" + successfulSentCounter.incrementAndGet() + " sent OK. " + sendResult);
        if (successfulSentCounter.longValue() >= total) {
            long interval = System.currentTimeMillis() - start;
            System.out.println("All Messages Sent Successfully in " + interval + "ms. Average TPS:" + (total * 1000L / interval));
        }
    }

    @Override
    public void onException(Throwable e) {
        System.out.println("Failure occurred. Now " + successfulSentCounter.longValue() + " sent OK.");
        e.printStackTrace();
    }
}
