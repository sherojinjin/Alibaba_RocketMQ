package com.ndpmedia.rocketmq.producer;

import com.ndpmedia.rocketmq.producer.model.Producer;

import java.util.List;

/**
 * Created by Administrator on 2014/12/30.
 */
public interface ProducerManager
{
    public List<Producer> findProducersByGroupName(String groupName, String topic);
}
