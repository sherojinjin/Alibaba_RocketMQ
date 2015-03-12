package com.ndpmedia.rocketmq.cockpit.mybatis.mapper;

import com.ndpmedia.rocketmq.cockpit.model.ConsumeProgress;
import com.ndpmedia.rocketmq.cockpit.model.ConsumerGroup;

import java.util.List;

public interface ConsumeProgressMapper {

    long insert(ConsumeProgress consumeProgress);

    void delete(long id);

    List<ConsumeProgress> list();

    List<ConsumeProgress> listByConsumerGroup(String consumerGroup);

    List<ConsumeProgress> listByTopic(String topic);
}
