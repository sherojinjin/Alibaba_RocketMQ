package com.ndpmedia.rocketmq.topic.impl;

import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.TopicService;
import com.ndpmedia.rocketmq.topic.model.Topic;

import java.io.IOException;
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
    public String lookUp(String topic) throws IOException
    {
        return null;
    }

    @Override
    public void add(Topic topic) throws IOException
    {
        System.out.println(topic);
        topicManager.add(topic);
    }

    @Override
    public void delete(String topic, String clusterName) throws
            IOException
    {
        System.out.println(topic);
        System.out.println(clusterName);
        topicManager.delete(topic, clusterName);
    }

    public TopicManager getTopicManager() {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
    }
}
