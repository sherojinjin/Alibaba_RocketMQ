package com.ndpmedia.rocketmq.authentication.impl;

import com.ndpmedia.rocketmq.authentication.RocketMQUserLoginManager;
import com.ndpmedia.rocketmq.authentication.RocketMQUserLoginService;
import com.ndpmedia.rocketmq.authentication.model.LoginType;
import com.ndpmedia.rocketmq.cockpit.log.CockpitLogger;
import com.ndpmedia.rocketmq.io.FileManager;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * the impl for RocketMQUserService.
 */
public class RocketMQUserLoginServiceImpl implements RocketMQUserLoginService
{
    private RocketMQUserLoginManager rocketMQUserLoginManager;

    private ConcurrentMap<String, LoginType> users = new ConcurrentHashMap<String, LoginType>();

    private static Properties config;

    private final Logger logger = CockpitLogger.getLogger();

    static
    {
        config = FileManager.getConfig();
    }

    @Override
    public boolean findUserStatus(String username)
    {
        LoginType loginType = null == users.get(username) ? new LoginType(username) : users.get(username);
        if (null != loginType)
        {
            long lockTime = loginType.getLockTime();
            int status = loginType.getStatus();
            System.out.println(" get the user status " + loginType.toString());
            if (lockTime > System.currentTimeMillis() && 1 == status)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean logUserStatus(String username)
    {
        LoginType loginType = new LoginType(username);
        users.put(username, loginType);
        return true;
    }

    @Override
    public boolean lockUser(String username)
    {
        return false;
    }

    @Override
    public boolean unlockUser(String username)
    {
        return false;
    }

    @Override
    public boolean userRetryTimeAdd(String username)
    {
        LoginType loginType = new LoginType(username);
        try
        {
            if (null != users.get(username))
            {
                loginType = users.get(username);
            }

            if (loginType.getStatus() != 1)
            {
                int index = loginType.getRetryTimes() + 1;
                loginType.setRetryTimes(index);
            }

            if (loginType.getRetryTimes() >= Integer.parseInt(config.getProperty("login_retry_time")))
            {
                loginType.setStatus(1);
                loginType.setLockTime(System.currentTimeMillis() + Integer.parseInt(config.getProperty("login_lock_time")));
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }

        System.out.println(" login failed , retry time ++ " + loginType.toString());
        System.out.println(" login failed , retry time ++ " + users);
        return true;
    }

    public RocketMQUserLoginManager getRocketMQUserLoginManager()
    {
        return rocketMQUserLoginManager;
    }

    public void setRocketMQUserLoginManager(RocketMQUserLoginManager rocketMQUserLoginManager)
    {
        this.rocketMQUserLoginManager = rocketMQUserLoginManager;
    }
}
