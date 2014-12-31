package com.ndpmedia.rocketmq.consumer.impl;

import com.ndpmedia.rocketmq.consumer.ConsumerManager;
import com.ndpmedia.rocketmq.consumer.ConsumerService;
import com.ndpmedia.rocketmq.consumer.model.Consumer;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;

import java.util.List;

public class ConsumerServiceImpl implements ConsumerService
{
    private ConsumerManager consumerManager;

    @Override
    public List<Consumer> list(String groupName)
    {
        if (null != groupName && !groupName.isEmpty())
        {
            List<Consumer> consumers = consumerManager.findConsumersByGroupName(groupName);
            return consumers;
        }
        return null;
    }

    @Override
    public List<ConsumerProgress> list(String groupName, String topic, String broker)
    {
        if (null == groupName)
        {
            System.err.println("");
            return null;
        }

        List<ConsumerProgress> progressList = consumerManager.findProgress(groupName, topic, broker);

        return progressList;
    }

    public void setConsumerManager(ConsumerManager consumerManager)
    {
        this.consumerManager = consumerManager;
    }

    public ConsumerManager getConsumerManager()
    {
        return consumerManager;
    }
}
