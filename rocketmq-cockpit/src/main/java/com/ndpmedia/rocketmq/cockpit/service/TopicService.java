package com.ndpmedia.rocketmq.cockpit.service;

import com.ndpmedia.rocketmq.cockpit.model.Topic;

import java.util.Set;

public interface TopicService {

    Set<String> fetchTopics();

    boolean createOrUpdateTopic(Topic topic);

    boolean deleteTopic(Topic topic);

}
