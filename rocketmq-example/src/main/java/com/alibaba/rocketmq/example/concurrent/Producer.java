package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import com.alibaba.rocketmq.common.message.Message;

import java.util.concurrent.atomic.AtomicLong;

public class Producer {

    public static void main(String[] args) {
        int count = 1000;
        if (args.length > 0) {
            count = Integer.parseInt(args[0]);
        }
        AtomicLong successCount = new AtomicLong(0L);
        MultiThreadMQProducer producer = MultiThreadMQProducer.configure()
                .configureProducerGroup("PG_Benchmark")
                .configureCorePoolSize(200)
                .configureConcurrentSendBatchSize(100)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .build();

                producer.registerCallback(new ExampleSendCallback(successCount, count, System.currentTimeMillis()));

        Message[] messages = new Message[count];

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            messages[i] = new Message("T_Benchmark", "Test MultiThread message".getBytes());
        }
        producer.send(messages);

        System.out.println("Messages are sent in async manner. Cost " + (System.currentTimeMillis() - start) + "ms");
    }
}
