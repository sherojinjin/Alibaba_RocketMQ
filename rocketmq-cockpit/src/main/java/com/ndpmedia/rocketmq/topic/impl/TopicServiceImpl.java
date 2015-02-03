package com.ndpmedia.rocketmq.topic.impl;

import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.TopicService;

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
    public void add(@FormParam("topic") String topic, @FormParam("key") String key) throws IOException
    {

    }

    @Override
    public void delete(@FormParam("topic") String topic) throws IOException
    {

    }

    public TopicManager getTopicManager() {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
    }
}
