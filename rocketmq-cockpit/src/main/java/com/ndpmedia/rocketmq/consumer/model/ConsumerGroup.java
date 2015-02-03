package com.ndpmedia.rocketmq.consumer.model;

/**
 * Created by Administrator on 2015/1/29.
 */
public class ConsumerGroup
{
    private String group_name;
    private String broker_address;

    public String getGroup_name()
    {
        return group_name;
    }

    public void setGroup_name(String group_name)
    {
        this.group_name = group_name;
    }

    public String getBroker_address()
    {
        return broker_address;
    }

    public void setBroker_address(String broker_address)
    {
        this.broker_address = broker_address;
    }
}

