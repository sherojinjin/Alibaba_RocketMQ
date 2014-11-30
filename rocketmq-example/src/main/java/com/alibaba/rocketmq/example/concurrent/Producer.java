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
                .configureProducerGroup("PG_MultiThread_Test")
                .configureCorePoolSize(20)
                .configureMaximumPoolSize(200)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .build();

                producer.registerCallback(new ExitOnSendCompletionCallback(producer, successCount, count));

        Message[] messages = new Message[count];

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            messages[i] = new Message("MultiThreadTopic", "Test MultiThread message".getBytes());
        }
        producer.send(messages);

        System.out.println("Messages are sent in async manner. Cost: " + (System.currentTimeMillis() - start) + "ms");
    }


}
