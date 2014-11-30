package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import com.alibaba.rocketmq.common.message.Message;

import java.util.concurrent.atomic.AtomicLong;

public class Producer {

    public static void main(String[] args) {
        final AtomicLong numberOfMessageSentSuccessfully = new AtomicLong(0L);
        final int count = 1000;
        MultiThreadMQProducer producer = null;
        producer = MultiThreadMQProducer.configure()
                .configureProducerGroup("PG_MultiThread_Test")
                .configureCorePoolSize(20)
                .configureMaximumPoolSize(200)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .configureSendCallback(new CustomSendCallback(producer, numberOfMessageSentSuccessfully, count))
                .build();

        Message[] messages = new Message[count];

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            messages[i] = new Message("MultiThreadTopic", "Test MultiThread message".getBytes());
        }
        producer.send(messages);

        System.out.println("Messages are sent in async manner" + (System.currentTimeMillis() - start));
    }

    static class CustomSendCallback implements SendCallback {

        private MultiThreadMQProducer producer;

        private AtomicLong successfulSentCounter;

        private long total;

        public CustomSendCallback(MultiThreadMQProducer producer, AtomicLong successfulSentCounter, long total) {
            this.producer = producer;
            this.successfulSentCounter = successfulSentCounter;
            this.total = total;
        }

        @Override
        public void onSuccess(SendResult sendResult) {

            System.out.println(successfulSentCounter.incrementAndGet() + " sent OK:" + sendResult);
            if (successfulSentCounter.longValue() >= total) {
                producer.shutdown();
            }
        }

        @Override
        public void onException(Throwable e) {
            System.out.println("Failure occurred. Now " + successfulSentCounter.longValue() + " sent OK.");
        }
    }
}
