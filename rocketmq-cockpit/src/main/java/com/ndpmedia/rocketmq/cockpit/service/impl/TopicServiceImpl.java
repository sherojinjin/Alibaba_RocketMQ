package com.ndpmedia.rocketmq.cockpit.service.impl;

import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.command.CommandUtil;
import com.ndpmedia.rocketmq.cockpit.model.Topic;
import com.ndpmedia.rocketmq.cockpit.service.TopicService;
import com.ndpmedia.rocketmq.cockpit.util.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service("topicService")
public class TopicServiceImpl implements TopicService {

    private Logger logger = LoggerFactory.getLogger(TopicServiceImpl.class);

    @Override
    public Set<String> fetchTopics() {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Helper.getInstanceName());
        try {
            defaultMQAdminExt.start();

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();

            return topicList.getTopicList();
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("[QUERY][TOPIC][MQADMIN] try to get topic failed." + e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return null;
    }


    @Override
    public boolean createOrUpdateTopic(Topic topic) {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Helper.getInstanceName());
        try {
            defaultMQAdminExt.start();
            TopicConfig topicConfig = wrapTopicToTopicConfig(topic);
            if (null != topic.getBrokerAddress() && !topic.getBrokerAddress().isEmpty()) {
                defaultMQAdminExt.createAndUpdateTopicConfig(topic.getBrokerAddress(), topicConfig);
                if (topic.isOrder()) {
                    // 注册顺序消息到 nameserver
                    String brokerName = CommandUtil.fetchBrokerNameByAddr(defaultMQAdminExt, topic.getBrokerAddress());
                    String orderConf = brokerName + ":" + topicConfig.getWriteQueueNums();
                    defaultMQAdminExt.createOrUpdateOrderConf(topicConfig.getTopicName(), orderConf, false);
                }
            } else {
                Set<String> masterSet = CommandUtil
                        .fetchMasterAddrByClusterName(defaultMQAdminExt, topic.getClusterName());
                for (String address : masterSet) {
                    defaultMQAdminExt.createAndUpdateTopicConfig(address, topicConfig);
                }

                if (topic.isOrder()) {
                    // 注册顺序消息到 nameserver
                    Set<String> brokerNameSet = CommandUtil
                            .fetchBrokerNameByClusterName(defaultMQAdminExt, topic.getClusterName());
                    StringBuilder orderConf = new StringBuilder();
                    String splitter = "";
                    for (String s : brokerNameSet) {
                        orderConf.append(splitter).append(s).append(":").append(topicConfig.getWriteQueueNums());
                        splitter = ";";
                    }
                    defaultMQAdminExt.createOrUpdateOrderConf(topicConfig.getTopicName(), orderConf.toString(), true);
                }
            }
        } catch (Exception e) {
            logger.warn("[ADD][TOPIC][MQADMIN]try to add topic failed." + e);
            return false;
        } finally {
            defaultMQAdminExt.shutdown();
        }

        return true;

    }

    @Override
    public boolean deleteTopic(Topic topic) {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Helper.getInstanceName());
        try {
            defaultMQAdminExt.start();
            Set<String> nameServerAddress = new HashSet<String>(defaultMQAdminExt.getNameServerAddressList());

            // Delete from brokers.
            Set<String> masterBrokerAddressSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, topic.getClusterName());

            defaultMQAdminExt.deleteTopicInBroker(masterBrokerAddressSet, topic.getTopic());

            // Delete from name server.
            defaultMQAdminExt.deleteTopicInNameServer(nameServerAddress, topic.getTopic());
        } catch (Exception e) {
            logger.warn("[DELETE][TOPIC][MQADMIN] try to delete topic failed." + e);
            return false;
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return true;
    }

    private static TopicConfig wrapTopicToTopicConfig(Topic topic) {
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setWriteQueueNums(topic.getWriteQueueNum());
        topicConfig.setReadQueueNums(topic.getReadQueueNum());
        topicConfig.setTopicName(topic.getTopic());
        return topicConfig;
    }
}
