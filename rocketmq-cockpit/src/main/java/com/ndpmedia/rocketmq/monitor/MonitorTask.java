package com.ndpmedia.rocketmq.monitor;

import com.alibaba.rocketmq.common.MixAll;
import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.cockpit.util.SqlParamsUtil;
import com.ndpmedia.rocketmq.cockpit.util.ToolSpring;
import com.ndpmedia.rocketmq.consumer.ConsumerManager;
import com.ndpmedia.rocketmq.consumer.model.ConsumerProgress;
import com.ndpmedia.rocketmq.topic.TopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * main thread.
 * contains:check topic;check consumer group name;find all topic diff.
 */
public class MonitorTask implements Runnable {

    @Autowired
    private ConsumerManager consumerManager;

    @Autowired
    private TopicManager topicManager;

    @Autowired
    private CockpitDao cockpitDao;

    private static Logger logger = LoggerFactory.getLogger(MonitorTask.class);

    public MonitorTask()
    {
        init();
    }

    private void init(){
        topicManager = (TopicManager) ToolSpring.getBean("topicManager");
        consumerManager = (ConsumerManager)ToolSpring.getBean("consumerManager");
        cockpitDao = (CockpitDao)ToolSpring.getBean("cockpitDao");
    }

    @Override
    public void run()
    {
        try
        {
            Set<String> topicList = topicManager.list();

            List<ConsumerProgress> results = new ArrayList<ConsumerProgress>();

            for (String topic : topicList)
            {
                if (!topic.contains(MixAll.RETRY_GROUP_TOPIC_PREFIX))
                    continue;
                results = consumerManager.findProgress(topic.replace(MixAll.RETRY_GROUP_TOPIC_PREFIX, ""), null, null);

                for (ConsumerProgress cp : results)
                {
                    if (null == cp || null == cp.getTopic() || null == cp.getBrokerName())
                        continue;
                    logger.info(cp.toString());
                    String sql = SqlParamsUtil.getSQL("message.diff", null);
                    cockpitDao.add(sql, cp);
                }
            }
        } catch (Exception e)
        {
            logger.warn(" monitor had some problem" + e.getMessage());
        }
    }

    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public void setConsumerManager(ConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    public TopicManager getTopicManager() {
        return topicManager;
    }

    public void setTopicManager(TopicManager topicManager) {
        this.topicManager = topicManager;
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
