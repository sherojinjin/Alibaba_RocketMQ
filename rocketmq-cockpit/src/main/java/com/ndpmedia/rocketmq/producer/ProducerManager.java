package com.ndpmedia.rocketmq.producer;

import com.ndpmedia.rocketmq.producer.model.Producer;

import java.util.List;

/**
 * the interface of produce manager.
 */
public interface ProducerManager {
    public List<Producer> findProducersByGroupName(String groupName, String topic);
}
