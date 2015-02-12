package com.ndpmedia.rocketmq.topic;

import com.ndpmedia.rocketmq.topic.model.Topic;

import java.util.Map;
import java.util.Set;

/**
 * the interface of topic manager.
 */
public interface TopicManager
{
    /**
     * get the topic list from name server.
     * @return topic list
     */
    public Set<String> list();

    /**
     * add a new topic to local database and name server.
     * @return  topic list
     */
    public boolean add(Topic topic);

    /**
     * delete topic from name server and local database.
     * @return  topic list
     */
    public boolean delete(Map<String, Object> fieldMap);

    /**
     * get the topic list from local database.
     * @return  topic list
     */
    public Set<String> dList();
}
