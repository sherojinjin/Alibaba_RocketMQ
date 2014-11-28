package com.alibaba.rocketmq.example.multithread;

import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

public class Producer {

    public static void main(String[] args) {
        MultiThreadMQProducer producer = MultiThreadMQProducer.configure()
                .configureProducerGroup("PG_MultiThread_Test")
                .configureCorePoolSize(20)
                .configureMaximumPoolSize(200)
                .configureRetryTimesBeforeSendingFailureClaimed(3)
                .configureSendMessageTimeOutInMilliSeconds(3000)
                .build();

        producer.send(new Message("MultiThreadTopic", "Test MultiThread message".getBytes()), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println(sendResult);
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
    }

}
