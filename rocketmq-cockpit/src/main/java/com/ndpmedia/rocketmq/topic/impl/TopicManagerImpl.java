package com.ndpmedia.rocketmq.topic.impl;

import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.command.CommandUtil;
import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.cockpit.util.CollectionUtil;
import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.model.Topic;
import com.ndpmedia.rocketmq.topic.model.TopicTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("topicManager")
public class TopicManagerImpl implements TopicManager
{
    private final Logger logger = LoggerFactory.getLogger(TopicManagerImpl.class);

    private CockpitDao cockpitDao;

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
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            defaultMQAdminExt.shutdown();
        }
        return null;
    }

    @Override
    public boolean add(Topic topic)
    {
        return addTopicConfig(topic) && addTopic(topic);
    }

    /**
     * add topic on broker
     *
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
                    String orderConf = brokerName + ":" + topicConfig.getWriteQueueNums();
                    defaultMQAdminExt.createOrUpdateOrderConf(topicConfig.getTopicName(), orderConf, false);
                }
            } else
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
                    String splitor = "";
                    for (String s : brokerNameSet)
                    {
                        orderConf.append(splitor).append(s).append(":").append(topicConfig.getWriteQueueNums());
                        splitor = ";";
                    }
                    defaultMQAdminExt.createOrUpdateOrderConf(topicConfig.getTopicName(), orderConf.toString(), true);
                }
            }
        } catch (Exception e)
        {
            logger.warn("[ADD][TOPIC][MQADMIN]try to add topic failed." + e);
            return false;
        } finally
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
            String sql = " insert into topic(topic, cluster_name , write_queue_num, read_queue_num, broker_address, " +
                    "order, create_time) values" +
                    "(:topic, :cluster_name , :write_queue_num , :read_queue_num, :broker_address, :order," +
                    " :create_time) ";
            cockpitDao.add(sql, topic);
        } catch (Exception e)
        {
            logger.warn("[ADD][TOPIC][DATABASE] try to add topic failed." + e);
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(String topic, String clusterName)
    {
        return deleteTopicConfig(topic, clusterName) && deleteTopic(topic, clusterName);
    }

    private boolean deleteTopicConfig(String topic, String clusterName)
    {
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
        } catch (Exception e)
        {
            logger.warn("[DELETE][TOPIC][MQADMIN] try to delete topic failed." + e);
            return false;
        } finally
        {
            defaultMQAdminExt.shutdown();
        }
        return true;
    }

    private boolean deleteTopic(String topic, String clusterName)
    {
        try{
            String sql = " DELETE FROM topic WHERE topic ='%s' AND cluster_name = '%s'";
            cockpitDao.del(String.format(sql, topic, clusterName));
        }catch (Exception e)
        {
            logger.warn("[DELETE][TOPIC][DATABASE] try to delete topic failed." + e);
            return false;
        }
        return true;
    }

    @Override
    public Set<String> dList()
    {
        return null;
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
