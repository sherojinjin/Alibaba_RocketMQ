package com.ndpmedia.rocketmq.cockpit.mybatis.mapper;

import com.ndpmedia.rocketmq.cockpit.model.ConsumerGroup;

import java.util.List;

public interface ConsumerGroupMapper {

    List<ConsumerGroup> list();

    ConsumerGroup get(long id);

    List<ConsumerGroup> getByGroupName(String topic);

    List<ConsumerGroup> getByClusterName(String clusterName);

    long insert(ConsumerGroup consumerGroup);

    void update(ConsumerGroup consumerGroup);

    void delete(long id);



}
