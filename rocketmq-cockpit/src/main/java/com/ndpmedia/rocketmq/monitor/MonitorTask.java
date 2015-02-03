package com.ndpmedia.rocketmq.monitor;

import com.alibaba.rocketmq.common.MixAll;
import com.ndpmedia.rocketmq.cockpit.log.CockpitLogger;
import com.ndpmedia.rocketmq.consumer.ConsumerManager;
import com.ndpmedia.rocketmq.consumer.impl.ConsumerManagerImpl;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;
import com.ndpmedia.rocketmq.topic.TopicManager;
import com.ndpmedia.rocketmq.topic.impl.TopicManagerImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * main thread.
 * contains:check topic;check consumer group name;find all topic diff.
 */
public class MonitorTask implements Runnable
{
    @Autowired
    private ConsumerManager consumerManager = new ConsumerManagerImpl();

    @Autowired
    private static TopicManager topicManager = new TopicManagerImpl();

    private static Logger logger = CockpitLogger.getLogger();

    @Override
    public void run()
    {
        Set<String> topicList = topicManager.list();

        List<ConsumerProgress> results = new ArrayList<ConsumerProgress>();

        for (String topic : topicList)
        {
            if (!topic.contains(MixAll.RETRY_GROUP_TOPIC_PREFIX))
                continue;
            try
            {
                results.addAll(consumerManager.findProgress(topic.replace(MixAll.RETRY_GROUP_TOPIC_PREFIX, ""), null,
                        null));
            } catch (Exception e)
            {
                logger.warn("");
            }
        }

        for (ConsumerProgress cp : results)
        {
            if (null == cp || null == cp.getMessageQueue() || null == cp.getOffsetWrapper())
                continue;
            logger.info(cp.toString());
        }

        System.gc();
    }

    public ConsumerManager getConsumerManager()
    {
        return consumerManager;
    }

    public void setConsumerManager(ConsumerManager consumerManager)
    {
        this.consumerManager = consumerManager;
    }

    public TopicManager getTopicManager()
    {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager)
    {
        this.topicManager = topicManager;
    }
}
