package com.alibaba.rocketmq.example.cacheable;

import com.alibaba.rocketmq.client.consumer.cacheable.CacheableConsumer;
import com.alibaba.rocketmq.client.consumer.cacheable.MessageHandler;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.Random;

public class ExampleCacheableConsumer {

    static class ExampleMessageHandler extends MessageHandler {
        private Random random = new Random();
        public ExampleMessageHandler() {
        }

        /**
         * User define processing logic, implemented by ultimate business developer.
         *
         * @param message Message to process.
         * @return 0 if business logic has already properly consumed this message; positive int N if this message is
         * supposed to be consumed again N milliseconds later.
         */
        @Override
        public int handle(MessageExt message) {
            return 0;
        }
    }

    public static void main(String[] args) throws MQClientException, InterruptedException {
        CacheableConsumer cacheableConsumer = new CacheableConsumer("CG_QuickStart");

        MessageHandler exampleMessageHandler = new ExampleMessageHandler();

        /**
         * Topic is strictly required.
         */
        exampleMessageHandler.setTopic("T_QuickStart");

        exampleMessageHandler.setTag("*");

        cacheableConsumer.registerMessageHandler(exampleMessageHandler);

        cacheableConsumer.setCorePoolSizeForWorkTasks(5); // default 10.
        cacheableConsumer.setMaximumPoolSizeForWorkTasks(20); //default 50

        cacheableConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        cacheableConsumer.setMessageModel(MessageModel.CLUSTERING);

        cacheableConsumer.start();

        System.out.println("User client starts.");
    }

}
