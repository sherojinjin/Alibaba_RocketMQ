package com.ndpmedia.rocketmq.topic.impl;

import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.TopicService;
import com.ndpmedia.rocketmq.topic.model.Topic;

import javax.ws.rs.FormParam;
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
    public String lookUp(@FormParam("topic") String topic) throws IOException
    {
        return null;
    }

    @Override
    public boolean add(Topic topic) throws IOException
    {
        return topicManager.add(topic);
    }

    @Override
    public boolean delete(@FormParam("topic") String topic, @FormParam("clusterName") String clusterName) throws
            IOException
    {
        return topicManager.delete(topic, clusterName);
    }

    public TopicManager getTopicManager() {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
    }
}
