package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.consumer.cacheable.CacheableConsumer;
import com.alibaba.rocketmq.client.producer.concurrent.LocalMessageStore;

public class CustomCacheableConsumer extends CacheableConsumer {


    public CustomCacheableConsumer(String consumerGroupName) {
        super(consumerGroupName);
    }

    public LocalMessageStore getLocalMessageStore() {
        return localMessageStore;
    }

    public void stopReceiving() throws InterruptedException {
        super.stopReceiving();
    }
}


