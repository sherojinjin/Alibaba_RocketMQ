package com.ndpmedia.rocketmq.cockpit.mybatis.mapper;

import com.ndpmedia.rocketmq.cockpit.model.Topic;

import java.util.List;

public interface TopicMapper {

    long insert(Topic topic);

    void delete(long id);

    void update(Topic topic);

    Topic get(long id);

    List<Topic> list();

    List<Topic> listByTopic(String topic);
}
