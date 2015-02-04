package com.ndpmedia.rocketmq.topic;

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
    public boolean add(String topic, String key, int nums);

    /**
     * delete topic from name server and local database.
     * @return  topic list
     */
    public boolean delete();

    /**
     * get the topic list from local database.
     * @return  topic list
     */
    public Set<String> dList();
}
