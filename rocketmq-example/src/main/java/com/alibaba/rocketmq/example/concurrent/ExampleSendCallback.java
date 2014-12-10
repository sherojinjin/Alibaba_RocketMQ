package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * <strong>Warning:</strong>If the message is not sent successfully for the first time, the statistics won't be correct.
 * </p>
 * @author Li Zhanhui
 */
public class ExampleSendCallback implements SendCallback {

    private AtomicLong successfulSentCounter;

    private long total;

    private long start;

    private long previousStatsTime;

    private static final long COUNT_INTERVAL = 1000L;

    public ExampleSendCallback(AtomicLong successfulSentCounter, long total) {
        this.successfulSentCounter = successfulSentCounter;
        this.total = total;
    }

    @Override
    public void onSuccess(SendResult sendResult) {
        if (0 == previousStatsTime) {
            start = previousStatsTime = System.currentTimeMillis();
        }
        successfulSentCounter.incrementAndGet();

        if (successfulSentCounter.longValue() % COUNT_INTERVAL == 0) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(dateFormat.format(new Date()) + " TPS:" + COUNT_INTERVAL * 1000.0F / (System.currentTimeMillis() - previousStatsTime));
            previousStatsTime = System.currentTimeMillis();
        }


        //System.out.println("ExampleSendCallback#onSuccess:" + successfulSentCounter.incrementAndGet() + " sent OK. " + sendResult);
        if (total > 0 && successfulSentCounter.longValue() >= total) {
            long interval = System.currentTimeMillis() - start;
            System.out.println("All Messages Sent Successfully in " + interval + "ms. Average TPS:" + (total * 1000.0F / interval));
        }
    }

    @Override
    public void onException(Throwable e) {
        System.out.println("Failure occurred. Now " + successfulSentCounter.longValue() + " sent OK.");
        e.printStackTrace();
    }
}
