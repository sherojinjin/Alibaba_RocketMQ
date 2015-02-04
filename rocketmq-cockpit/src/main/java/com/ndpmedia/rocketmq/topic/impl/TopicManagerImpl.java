package com.ndpmedia.rocketmq.topic.impl;

import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.topic.TopicManager;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service("topicManager")
public class TopicManagerImpl implements TopicManager
{
    private CockpitDao cockpitDao;

    @Override
    public Set<String> list()
    {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            TopicList topicList = defaultMQAdminExt.fetchAllTopicList();

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

    @Override
    public boolean add(String topic, String key, int nums)
    {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try{
            defaultMQAdminExt.start();
            defaultMQAdminExt.createTopic(key, topic, nums);


        }catch (Exception e){
            return false;
        }finally
        {
            defaultMQAdminExt.shutdown();
        }

        return true;
    }

    @Override
    public boolean delete()
    {

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
