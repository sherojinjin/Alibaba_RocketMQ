package com.ndpmedia.rocketmq.authentication.impl;

import com.ndpmedia.rocketmq.authentication.RocketMQUserLoginManager;
import com.ndpmedia.rocketmq.authentication.model.LoginType;
import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("rocketMQUserLoginManager")
public class RocketMQUserLoginManagerImpl implements RocketMQUserLoginManager
{
    private CockpitDao cockpitDao;

    @Override
    public List<LoginType> list()
    {
        return null;
    }

    @Override
    public boolean lock(String username)
    {
        return false;
    }

    @Override
    public boolean unlock(String username)
    {
        return false;
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
