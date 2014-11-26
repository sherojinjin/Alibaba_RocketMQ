package com.ndpmedia.rocketmq.nameserver.impl;

import com.google.common.base.Joiner;
import com.ndpmedia.rocketmq.nameserver.NameServerAddressService;
import com.ndpmedia.rocketmq.nameserver.NameServerManager;
import com.ndpmedia.rocketmq.nameserver.model.NameServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NameServerAddressServiceImpl implements NameServerAddressService {

    private NameServerManager nameServerManager;

    @Override
    public String listNameServer() {
        StringBuilder stringBuilder = new StringBuilder(256);
        Set<String> nameServers = nameServerManager.list();
        Joiner joiner = Joiner.on(";").skipNulls();
        return joiner.join(nameServers);
    }


    @Override
    public void add(String nameServer) {
        nameServerManager.add(nameServer);
    }

    @Override
    public void delete(String nameServer) {
        nameServerManager.remove(nameServer);
    }

    @Override
    public List<NameServer> list() {

        try {
            ConcurrentHashMap<String, Long> map = nameServerManager.listAll(true);
            List<NameServer> nameServers = new ArrayList<NameServer>();
            for (Map.Entry<String, Long> row : map.entrySet()) {
                nameServers.add(new NameServer(row.getKey(), new Date(row.getValue())));
            }

            return nameServers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NameServerManager getNameServerManager() {
        return nameServerManager;
    }

    public void setNameServerManager(NameServerManager nameServerManager) {
        this.nameServerManager = nameServerManager;
    }
}
