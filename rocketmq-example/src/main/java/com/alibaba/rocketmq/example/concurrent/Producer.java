package com.alibaba.rocketmq.example.concurrent;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.Message;

public class Producer {

    private static final AtomicLong SEQUENCE_GENERATOR = new AtomicLong(0L);


    public static void main(String[] args) {
        int count = 0;
        if (args.length > 0) {
            count = Integer.parseInt(args[0]);
        } else {
            count = -1;
        }
        final AtomicLong successCount = new AtomicLong(0L);
        final AtomicLong lastSent = new AtomicLong(0L);
        final MultiThreadMQProducer producer = MultiThreadMQProducer.configure()
                .configureProducerGroup("PG_QuickStart")
                .configureCorePoolSize(200)
                .configureConcurrentSendBatchSize(100)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .configureDefaultTopicQueueNumber(16)
                .build();
                producer.registerCallback(new ExampleSendCallback(successCount));

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("MessageStatThread-")).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long currentSuccessSent = successCount.longValue();
                System.out.println("TPS: " + (currentSuccessSent - lastSent.longValue() + 
                    "left permits: " + producer.getSemaphore().availablePermits()));
                lastSent.set(currentSuccessSent);
            }
        }, 0, 1, TimeUnit.SECONDS);

        if (count < 0) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              while(true){
                Message[] messages = buildMessages(3000);
                System.out.println("sending message at "+ new Date());
                producer.send(messages);
                try {
                  Thread.sleep(3000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }

            }
        }).start();
            /*Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("MessageSenderThread-")).scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Message[] messages = buildMessages(3000);
                    System.out.println("sending message "+System.currentTimeMillis());
                    producer.send(messages);
                }
            }, 3000, 1000, TimeUnit.MILLISECONDS);*/
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
