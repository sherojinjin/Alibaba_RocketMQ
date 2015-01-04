package com.ndpmedia.rocketmq.topic.impl;

import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.topic.TopicManager;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("topicManager")
public class TopicManagerImpl implements TopicManager
{
    @Override
    public Set<String> list()
    {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
            for (String topic : topicList.getTopicList()) {
                System.out.println(topic);
            }

            return topicList.getTopicList();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            defaultMQAdminExt.shutdown();
        }
        return null;
    }
}
