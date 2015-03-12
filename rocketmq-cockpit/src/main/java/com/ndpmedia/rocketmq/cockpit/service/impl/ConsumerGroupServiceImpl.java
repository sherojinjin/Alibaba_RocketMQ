package com.ndpmedia.rocketmq.cockpit.service.impl;

import com.alibaba.rocketmq.common.subscription.SubscriptionGroupConfig;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.command.CommandUtil;
import com.ndpmedia.rocketmq.cockpit.model.ConsumerGroup;
import com.ndpmedia.rocketmq.cockpit.service.ConsumerGroupService;
import com.ndpmedia.rocketmq.cockpit.util.Helper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("consumerGroupService")
public class ConsumerGroupServiceImpl implements ConsumerGroupService {
    @Override
    public boolean update(ConsumerGroup consumerGroup) {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Helper.getInstanceName());
        try {
            defaultMQAdminExt.start();
            SubscriptionGroupConfig subscriptionGroupConfig = wrap(consumerGroup);
            if (null != consumerGroup.getBrokerAddress()) {
                defaultMQAdminExt.createAndUpdateSubscriptionGroupConfig(consumerGroup.getBrokerAddress(), subscriptionGroupConfig);
            } else {
                Set<String> masterSet = CommandUtil
                        .fetchMasterAddrByClusterName(defaultMQAdminExt, consumerGroup.getClusterName());

                if (null != masterSet && !masterSet.isEmpty()) {
                    for (String brokerAddress : masterSet) {
                        defaultMQAdminExt.createAndUpdateSubscriptionGroupConfig(brokerAddress, subscriptionGroupConfig);
                    }
                }
            }
        } catch (Exception e) {
            return false;
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return true;
    }


    private SubscriptionGroupConfig wrap(ConsumerGroup consumerGroup) {
        SubscriptionGroupConfig subscriptionGroupConfig = new SubscriptionGroupConfig();
        subscriptionGroupConfig.setBrokerId(consumerGroup.getBrokerId());
        subscriptionGroupConfig.setConsumeBroadcastEnable(consumerGroup.isConsumeBroadcastEnable());
        subscriptionGroupConfig.setConsumeEnable(consumerGroup.isConsumeEnable());
        subscriptionGroupConfig.setConsumeFromMinEnable(consumerGroup.isConsumeFromMinEnable());
        subscriptionGroupConfig.setGroupName(consumerGroup.getGroupName());
        subscriptionGroupConfig.setRetryMaxTimes(consumerGroup.getRetryMaxTimes());
        subscriptionGroupConfig.setRetryQueueNums(consumerGroup.getRetryQueueNum());
        subscriptionGroupConfig.setWhichBrokerWhenConsumeSlowly(consumerGroup.getWhichBrokerWhenConsumeSlowly());
        return subscriptionGroupConfig;
    }
}
