package com.ndpmedia.rocketmq.topic.impl;

import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.TopicService;
import com.ndpmedia.rocketmq.topic.model.Topic;

import java.util.List;
import java.util.Map;

public class TopicServiceImpl implements TopicService {
    private TopicManager topicManager;

    @Override
    public List<Topic> list() {
        List<Topic> lists = topicManager.dList();
        return lists;
    }

    @Override
    public String lookUp(String topic) {
        return null;
    }

    @Override
    public void add(Topic topic) {
        topicManager.add(topic);
    }

    @Override
    public void delete(Map<String, Object> fieldMap) {
        topicManager.delete(fieldMap);
    }

    @Override
    public void update(Map<String, Object> fieldMap) {
        topicManager.register(fieldMap);
    }

    @Override
    public List<Object> messageDiff(Map<String, Object> fieldMap) {
        return null;
    }

    public TopicManager getTopicManager() {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
    }
}
