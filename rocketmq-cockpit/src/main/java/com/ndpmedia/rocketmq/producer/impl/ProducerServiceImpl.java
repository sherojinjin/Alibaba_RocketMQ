package com.ndpmedia.rocketmq.producer.impl;

import com.ndpmedia.rocketmq.producer.ProducerManager;
import com.ndpmedia.rocketmq.producer.ProducerService;
import com.ndpmedia.rocketmq.producer.model.Producer;

import java.util.List;

public class ProducerServiceImpl implements ProducerService
{
    private ProducerManager producerManager;

    @Override
    public List<Producer> list(String groupName, String topic)
    {
        if (null == groupName)
        {
            System.err.println(" group name is null ");
            return null;
        }
        if (null == topic)
        {
            System.err.println(" topic is null ");
            return null;
        }
        List<Producer> producers = producerManager.findProducersByGroupName(groupName, topic);
        return producers;
    }

    public ProducerManager getProducerManager() {
        return producerManager;
    }

    public void setProducerManager(ProducerManager producerManager) {
        this.producerManager = producerManager;
    }
}
