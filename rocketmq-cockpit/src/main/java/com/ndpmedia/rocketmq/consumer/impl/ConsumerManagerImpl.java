package com.ndpmedia.rocketmq.consumer.impl;

import com.alibaba.rocketmq.common.admin.ConsumeStats;
import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.body.Connection;
import com.alibaba.rocketmq.common.protocol.body.ConsumerConnection;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.consumer.ConsumerManager;
import com.ndpmedia.rocketmq.consumer.model.Consumer;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service("consumerManager")
public class ConsumerManagerImpl implements ConsumerManager {
    private CockpitDao cockpitDao;

    private final Logger logger = LoggerFactory.getLogger(ConsumerManagerImpl.class);

    @Override
    public List<Consumer> findConsumersByGroupName(String groupName) {
        List<Consumer> consumers = new ArrayList<Consumer>();

        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            defaultMQAdminExt.start();
            ConsumerConnection cc = defaultMQAdminExt.examineConsumerConnectionInfo(groupName);

            // 打印连接
            //            int i = 1;
            for (Connection conn : cc.getConnectionSet()) {
                Consumer consumer = new Consumer();
                consumer.setClientAddr(conn.getClientAddr());
                consumer.setClientId(conn.getClientId());
                consumer.setLanguage(conn.getLanguage());
                consumer.setVersion(conn.getVersion());
                consumers.add(consumer);
//                logger.debug("%03d  %-32s %-22s %-8s %s\n",//
//                        i++,//
//                        conn.getClientId(),//
//                        conn.getClientAddr(),//
//                        conn.getLanguage(),//
//                        MQVersion.getVersionDesc(conn.getVersion())//
//                );
            }

            // 打印订阅关系
//            logger.debug("\nBelow is subscription:");
//            Iterator<Map.Entry<String, SubscriptionData>> it = cc.getSubscriptionTable().entrySet().iterator();
//            i = 1;
//            while (it.hasNext()) {
//                Map.Entry<String, SubscriptionData> entry = it.next();
//                SubscriptionData sd = entry.getValue();
//                logger.debug("%03d  Topic: %-40s SubExpression: %s\n",//
//                        i++,//
//                        sd.getTopic(),//
//                        sd.getSubString()//
//                );
//            }

            // 打印其他订阅参数
//            logger.debug("");
//            logger.debug("ConsumeType: %s\n", cc.getConsumeType());
//            logger.debug("MessageModel: %s\n", cc.getMessageModel());
//            logger.debug("ConsumeFromWhere: %s\n", cc.getConsumeFromWhere());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            defaultMQAdminExt.shutdown();
        }
        return consumers;
    }

    @Override
    public List<ConsumerProgress> findProgress(String groupName, String topic, String broker) {
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

//                logger.debug("%-32s  %-32s  %-4d  %-20d  %-20d  %d\n", UtilAll.frontStringAtLeast(mq.getTopic(), 32),//
//                        UtilAll.frontStringAtLeast(mq.getBrokerName(), 32), mq.getQueueId(),
//                        offsetWrapper.getBrokerOffset(), offsetWrapper.getConsumerOffset(), diff);
            }

            progressList.add(new ConsumerProgress(null, null, diffTotal));
//            logger.debug("");
//            logger.debug("Consume TPS: %d\n", consumeStats.getConsumeTps());
//            logger.debug("Diff Total: %d\n", diffTotal);

        } catch (Exception e) {

        } finally {
            defaultMQAdminExt.shutdown();
        }
        return progressList;
    }

    @Override
    public List<String> findConsumerGroupNames() {
        List<String> list = null;
        try {
            String sql = " SELECT group_name FROM consumer_group GROUP BY group_name ";
            List<Map<String, Object>> re = cockpitDao.getList(sql);
            for (Map<String, Object> map : re) {
                list.add(map.get("group_name").toString());
            }
        } catch (Exception e) {

        }
        return list;
    }

    public CockpitDao getCockpitDao() {
        return cockpitDao;
    }

    public void setCockpitDao(CockpitDao cockpitDao) {
        this.cockpitDao = cockpitDao;
    }
}
