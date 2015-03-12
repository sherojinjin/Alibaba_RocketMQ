package com.ndpmedia.rocketmq.cockpit.service.impl;

import com.alibaba.rocketmq.common.admin.ConsumeStats;
import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.cockpit.model.ConsumerProgress;
import com.ndpmedia.rocketmq.cockpit.service.ConsumeProgressService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service("consumeProgressService")
public class ConsumeProgressServiceImpl implements ConsumeProgressService {

    @Override
    public List<ConsumerProgress> queryConsumerProgress(String groupName, String topic, String broker) {
        List<ConsumerProgress> progressList = new ArrayList<ConsumerProgress>();
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();
            // 查询特定consumer
            ConsumeStats consumeStats = defaultMQAdminExt.examineConsumeStats(groupName);

            List<MessageQueue> mqList = new LinkedList<MessageQueue>();
            mqList.addAll(consumeStats.getOffsetTable().keySet());
            Collections.sort(mqList);

            long diffTotal = 0L;

            for (MessageQueue mq : mqList) {
                OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);

                if (null != topic && !topic.equals(mq.getTopic())) {
                    continue;
                }

                if (null != broker && !broker.equals(mq.getBrokerName())) {
                    continue;
                }

                long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();
                diffTotal += diff;

                progressList.add(new ConsumerProgress(mq, offsetWrapper, diff));
            }

            progressList.add(new ConsumerProgress(null, null, diffTotal));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return progressList;
    }
}
