package com.ndpmedia.rocketmq.ip.impl;

import com.ndpmedia.rocketmq.ip.IPMappingManager;
import com.ndpmedia.rocketmq.ip.IPMappingService;
import com.ndpmedia.rocketmq.ip.model.IPPair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class IPMappingServiceImpl implements IPMappingService {

    private IPMappingManager ipMappingManager;

    @Override
    public String lookUp(String innerIP) throws IOException {
        return ipMappingManager.lookup(innerIP);
    }

    @Override
    public void add(String innerIP, String publicIP) throws IOException {
        ipMappingManager.add(innerIP, publicIP);
    }

    @Override
    public void delete(Map<String, Object> params) throws IOException {
        ipMappingManager.remove(params);
    }

    @Override
    public List<IPPair> list() throws IOException {
        return ipMappingManager.list();
    }

    public IPMappingManager getIpMappingManager() {
        return ipMappingManager;
    }

    public void setIpMappingManager(IPMappingManager ipMappingManager) {
        this.ipMappingManager = ipMappingManager;
    }
}
