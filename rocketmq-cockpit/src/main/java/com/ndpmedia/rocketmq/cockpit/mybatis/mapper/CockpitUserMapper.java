package com.ndpmedia.rocketmq.cockpit.mybatis.mapper;

import com.ndpmedia.rocketmq.cockpit.model.CockpitUser;

import java.util.List;

public interface CockpitUserMapper {

    CockpitUser get(long id);

    List<CockpitUser> list();

    CockpitUser getByUserName(String userName);

}
