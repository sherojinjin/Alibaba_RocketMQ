package com.ndpmedia.rocketmq.consumer;

import com.ndpmedia.rocketmq.consumer.model.Consumer;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;

import java.util.List;

/**
 * the interface of consumer manager.
 */
public interface ConsumerManager {
    public List<Consumer> findConsumersByGroupName(String groupName);

    public List<ConsumerProgress> findProgress(String groupName, String topic, String broker);

    public List<String> findConsumerGroupNames();
}
