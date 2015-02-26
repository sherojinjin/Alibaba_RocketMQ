package com.ndpmedia.rocketmq.topic.impl;

import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.command.CommandUtil;
import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.cockpit.util.CollectionUtil;
import com.ndpmedia.rocketmq.cockpit.util.Constant;
import com.ndpmedia.rocketmq.cockpit.util.SqlParamsUtil;
import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("topicManager")
public class TopicManagerImpl implements TopicManager, Constant
{
    private final Logger logger = LoggerFactory.getLogger(TopicManagerImpl.class);

    private CockpitDao cockpitDao;

    /**
     * get the topics on mq admin
     * @return
     */
    @Override
    public Set<String> list()
    {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try
        {
            defaultMQAdminExt.start();

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();

            return topicList.getTopicList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.warn("[QUERY][TOPIC][MQADMIN] try to get topic failed." + e);
        }
        finally
        {
            defaultMQAdminExt.shutdown();
        }
        return null;
    }

    /**
     * get the topics on database
     * @return
     */
    @Override
    public List<Topic> dList()
    {
        List<Topic> topics = new ArrayList<Topic>();
        try
        {
            String sql = SqlParamsUtil.getSQL("topic.all", null);
            TopicRowMapper<Topic> topicRowMapper = new TopicRowMapper<Topic>();
            topics = cockpitDao.getBeanList(sql, topicRowMapper);
        }
        catch (Exception e)
        {
            logger.warn("[QUERY][TOPIC][DATABASE]get database topics failed." + e);
        }

        return topics;
    }

    /**
     * get topic
     * @param fieldMap
     * @return
     */
    @Override
    public Topic lookUp(Map<String, Object> fieldMap)
    {
        Topic topic = new Topic();
        try
        {
            String sql = SqlParamsUtil.getSQL(fieldMap.get("sqlName").toString(), fieldMap);
            TopicRowMapper<Topic> topicRowMapper = new TopicRowMapper<Topic>();
            List<Topic> topics = cockpitDao.getBeanList(sql, topicRowMapper);
            if (null != topics && !topics.isEmpty())
                topic = topics.get(ZERO);
        }
        catch (Exception e)
        {
            logger.warn("[QUERY][TOPIC][DATABASE] try to get topic failed." + e);
        }
        return topic;
    }

    @Override
    public boolean add(Topic topic)
    {
        return addTopic(topic);
    }

    /**
     * add topic on broker
     * @param topic
     * @return
     */
    private boolean addTopicConfig(Topic topic)
    {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try
        {
            defaultMQAdminExt.start();
            TopicConfig topicConfig = TopicTypeUtil.changeTopicToTopicConfig(topic);

            if (null != topic.getBroker_address() && !topic.getBroker_address().isEmpty())
            {
                defaultMQAdminExt.createAndUpdateTopicConfig(topic.getBroker_address(), topicConfig);
                if (topic.isOrder())
                {
                    // 注册顺序消息到 nameserver
                    String brokerName = CommandUtil.fetchBrokerNameByAddr(defaultMQAdminExt, topic.getBroker_address());
                    String orderConf = brokerName + COLON + topicConfig.getWriteQueueNums();
                    defaultMQAdminExt.createOrUpdateOrderConf(topicConfig.getTopicName(), orderConf, false);
                }
            }
            else
            {
                Set<String> masterSet = CommandUtil
                        .fetchMasterAddrByClusterName(defaultMQAdminExt, topic.getCluster_name());
                for (String address : masterSet)
                {
                    defaultMQAdminExt.createAndUpdateTopicConfig(address, topicConfig);
                }

                if (topic.isOrder())
                {
                    // 注册顺序消息到 nameserver
                    Set<String> brokerNameSet = CommandUtil
                            .fetchBrokerNameByClusterName(defaultMQAdminExt, topic.getCluster_name());
                    StringBuilder orderConf = new StringBuilder();
                    String splitor = EMPTY_STRING;
                    for (String s : brokerNameSet)
                    {
                        orderConf.append(splitor).append(s).append(COLON).append(topicConfig.getWriteQueueNums());
                        splitor = SEMICOLON;
                    }
                    defaultMQAdminExt.createOrUpdateOrderConf(topicConfig.getTopicName(), orderConf.toString(), true);
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("[ADD][TOPIC][MQADMIN]try to add topic failed." + e);
            return false;
        }
        finally
        {
            defaultMQAdminExt.shutdown();
        }

        return true;
    }

    /**
     * add topic on data base
     *
     * @param topic
     * @return
     */
    private boolean addTopic(Topic topic)
    {
        try
        {
            String sql = SqlParamsUtil.getSQL("topic.add", null);
            cockpitDao.add(sql, topic);
        }
        catch (Exception e)
        {
            logger.warn("[ADD][TOPIC][DATABASE] try to add topic failed." + e);
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(Map<String, Object> fieldMap)
    {
        fieldMap.put("sqlName", "topic.getByID");
        Topic topic = lookUp(fieldMap);
        fieldMap.remove("sqlName");

        fieldMap.put("topic", topic.getTopic());
        fieldMap.put("cluster_name", topic.getCluster_name());

        return deleteTopicConfig(fieldMap) && deleteTopic(fieldMap);
    }

    /**
     * delete topic on mq admin
     * @param fieldMap
     * @return
     */
    private boolean deleteTopicConfig(Map<String, Object> fieldMap)
    {
        String topic = fieldMap.get("topic").toString();
        String clusterName = fieldMap.get("cluster_name").toString();

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try
        {
            defaultMQAdminExt.start();
            Set<String> nameServerAddress = CollectionUtil
                    .changeListToSet(defaultMQAdminExt.getNameServerAddressList());
            defaultMQAdminExt.deleteTopicInNameServer(nameServerAddress, topic);

            // 删除 broker 上的 topic 信息
            Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
            defaultMQAdminExt.deleteTopicInBroker(masterSet, topic);
        }
        catch (Exception e)
        {
            logger.warn("[DELETE][TOPIC][MQADMIN] try to delete topic failed." + e);
            return false;
        }
        finally
        {
            defaultMQAdminExt.shutdown();
        }
        return true;
    }

    /**
     * delete topic on database
     * @param fieldMap
     * @return
     */
    private boolean deleteTopic(Map<String, Object> fieldMap)
    {
        try
        {
            String sql = SqlParamsUtil.getSQL("topic.delete", fieldMap);
            cockpitDao.del(sql);
        }
        catch (Exception e)
        {
            logger.warn("[DELETE][TOPIC][DATABASE] try to delete topic failed." + e);
            return false;
        }
        return true;
    }

    @Override
    public boolean register(Map<String, Object> fieldMap)
    {
        boolean result = true;

        try
        {
            Set<String> topics = list();
            fieldMap.put("sqlName", "topic.getByID");
            Topic topic = lookUp(fieldMap);

            //if the topic is already exist on mq admin
            if (!topics.contains(topic.getTopic()))
            {
                result = addTopicConfig(topic);
            }
            //when the mq admin have the topic, update the topic status on database
            if (result)
            {
                fieldMap.put("sqlName", "topic.allow");
                fieldMap.put("topic", topic.getTopic());
                fieldMap.put("cluster_name", topic.getCluster_name());

                result = update(fieldMap);
            }
        }
        catch (Exception e)
        {
            logger.warn("[REGISTER][TOPIC][DATABASE] try to register topic failed." + e);
            result = false;
        }
        //if add topic into mq admin or update the topic status failed, remove topic from mq admin
        if (!result)
        {
            fieldMap.remove("sqlName");

            deleteTopicConfig(fieldMap);
        }

        return result;
    }

    @Override
    public boolean update(Map<String, Object> fieldMap)
    {
        try
        {
            String sql = SqlParamsUtil.getSQL(fieldMap.get("sqlName").toString(), fieldMap);
            cockpitDao.add(sql, fieldMap);
        }
        catch (Exception e)
        {
            logger.warn("[UPDATE][TOPIC][DATABASE] try to update topic failed." + e);
            return false;
        }
        return true;
    }

    public CockpitDao getCockpitDao()
    {
        return cockpitDao;
    }

    public void setCockpitDao(CockpitDao cockpitDao)
    {
        this.cockpitDao = cockpitDao;
    }
}
