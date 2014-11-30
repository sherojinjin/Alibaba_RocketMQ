package com.alibaba.rocketmq.example.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import com.alibaba.rocketmq.common.message.Message;

public class Producer {

    public static void main(String[] args) {
        MultiThreadMQProducer producer = MultiThreadMQProducer.configure()
                .configureProducerGroup("PG_MultiThread_Test")
                .configureCorePoolSize(20)
                .configureMaximumPoolSize(200)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .configureSendCallback(new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.println(sendResult);
                    }

                    @Override
                    public void onException(Throwable e) {
                        e.printStackTrace();
                    }
                })
                .build();

        int count = 1000;

        Message[] messages = new Message[count];

        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            messages[i] = new Message("MultiThreadTopic", "Test MultiThread message".getBytes());
        }
        producer.send(messages);

        System.out.println("Messages are sent in async manner" + (System.currentTimeMillis() - start));
    }
}
