package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.consumer.cacheable.CacheableConsumer;

public class CustomCacheableConsumer extends CacheableConsumer {


    public CustomCacheableConsumer(String consumerGroupName) {
        super(consumerGroupName);
    }

    public void stopReceiving() throws InterruptedException {
        super.stopReceiving();
    }
}


