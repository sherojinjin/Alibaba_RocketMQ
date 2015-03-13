package com.ndpmedia.rocketmq.ip.impl;

import com.ndpmedia.rocketmq.cockpit.mybatis.mapper.IPPairMapper;
import com.ndpmedia.rocketmq.ip.IPMappingService;
import org.springframework.beans.factory.annotation.Autowired;

public class IPMappingServiceImpl implements IPMappingService {

    @Autowired
    private IPPairMapper ipPairMapper;

    @Override
    public String lookUp(String innerIP) {
        return ipPairMapper.lookUp(innerIP);
    }
}
