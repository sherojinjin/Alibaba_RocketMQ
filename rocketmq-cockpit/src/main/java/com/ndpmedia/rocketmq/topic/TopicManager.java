package com.ndpmedia.rocketmq.topic;

import com.ndpmedia.rocketmq.topic.model.Topic;

import java.util.List;
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
     *
     * @param fieldMap
     * @return
     */
    public Topic lookUp(Map<String, Object> fieldMap);
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
     * allow the topic to use.
     * @param fieldMap
     * @return
     */
    public boolean register(Map<String, Object> fieldMap);

    /**
     * change topic params on data base
     * @param fieldMap
     * @return
     */
    public boolean update(Map<String, Object> fieldMap);

    /**
     * get the topic list from local database.
     * @return  topic list
     */
    public List<Topic> dList();
}
