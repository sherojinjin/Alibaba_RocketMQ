package com.ndpmedia.rocketmq.authentication;

import com.ndpmedia.rocketmq.authentication.model.LoginType;

import java.util.List;

public interface RocketMQUserLoginManager
{
    List<LoginType> list();

    boolean lock(String username);

    boolean unlock(String username);
}
