package com.alibaba.rocketmq.example.cacheable;

import com.alibaba.rocketmq.client.consumer.cacheable.CacheableConsumer;
import com.alibaba.rocketmq.client.consumer.cacheable.MessageHandler;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.Message;

public class ExampleCacheableConsumer {

    static class ExampleMessageHandler extends MessageHandler {

        /**
         * User define processing logic, implemented by ultimate business developer.
         *
         * @param message Message to process.
         * @return 0 if business logic has already properly consumed this message; positive int N if this message is
         * supposed to be consumed again N milliseconds later.
         */
        @Override
        public int handle(Message message) {
            System.out.println(message.getTopic());
            return 0;
        }
    }

    public static void main(String[] args) throws MQClientException, InterruptedException {
        CacheableConsumer cacheableConsumer = new CacheableConsumer("CG_ExampleCacheableConsumer");

        MessageHandler exampleMessageHandler = new ExampleMessageHandler();

        /**
         * Topic is strictly required.
         */
        exampleMessageHandler.setTopic("TopicTest_Lien");

        exampleMessageHandler.setTag("*");

        cacheableConsumer.registerMessageHandler(exampleMessageHandler);

        cacheableConsumer.setCorePoolSizeForDelayTasks(1); // default 2.
        cacheableConsumer.setCorePoolSizeForWorkTasks(5); // default 10.

        cacheableConsumer.start();

        System.out.println("User client starts.");
    }

}
