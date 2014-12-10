package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import com.alibaba.rocketmq.common.message.Message;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Producer {

    private static final AtomicLong SEQUENCE_GENERATOR = new AtomicLong(0L);

    public static void main(String[] args) {
        int count = 0;
        if (args.length > 0) {
            count = Integer.parseInt(args[0]);
        } else {
            count = -1;
        }
        AtomicLong successCount = new AtomicLong(0L);
        final MultiThreadMQProducer producer = MultiThreadMQProducer.configure()
                .configureProducerGroup("PG_QuickStart")
                .configureCorePoolSize(200)
                .configureConcurrentSendBatchSize(100)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .configureDefaultTopicQueueNumber(16)
                .build();
                producer.registerCallback(new ExampleSendCallback(successCount, count, System.currentTimeMillis()));
        if (count < 0) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Message[] messages = buildMessages(3000);
                    producer.send(messages);
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
        } else {
            long start = System.currentTimeMillis();
            Message[] messages = buildMessages(count);
            producer.send(messages);
            System.out.println("Messages are sent in async manner. Cost " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public static Message[] buildMessages(int n) {
        Message[] messages = new Message[n];
        for (int i = 0; i < n; i++) {
            messages[i] = new Message("T_QuickStart", "Test MultiThread message".getBytes());
            messages[i].putUserProperty("sequenceId", String.valueOf(SEQUENCE_GENERATOR.incrementAndGet()));
        }
        return messages;
    }

}
