package com.ndpmedia.rocketmq.topic.impl;

import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.TopicService;
import com.ndpmedia.rocketmq.topic.model.Topic;

import java.util.Map;
import java.util.Set;

public class TopicServiceImpl implements TopicService
{
    private TopicManager topicManager;

    @Override
    public Set<String> list()
    {
        Set<String> lists = topicManager.list();
        return lists;
    }

    @Override
    public String lookUp(String topic)
    {
        return null;
    }

    @Override
    public void add(Topic topic)
    {
        System.out.println(topic);
        topicManager.add(topic);
    }

    @Override
    public void delete(Map<String, Object> fieldMap)
    {
        topicManager.delete(fieldMap);
    }

    public TopicManager getTopicManager() {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
    }
}
