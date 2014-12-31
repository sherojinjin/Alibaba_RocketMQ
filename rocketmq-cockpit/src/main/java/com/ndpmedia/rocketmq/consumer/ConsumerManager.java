package com.ndpmedia.rocketmq.consumer;

import com.ndpmedia.rocketmq.consumer.model.Consumer;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;

import java.util.List;

/**
 * Created by Administrator on 2014/12/30.
 */
public interface ConsumerManager
{
    public List<Consumer> findConsumersByGroupName(String groupName);

    public List<ConsumerProgress> findProgress(String groupName, String topic, String broker);
}
