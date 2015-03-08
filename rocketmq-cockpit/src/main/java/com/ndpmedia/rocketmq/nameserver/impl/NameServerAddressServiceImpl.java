package com.ndpmedia.rocketmq.nameserver.impl;

import com.google.common.base.Joiner;
import com.ndpmedia.rocketmq.nameserver.NameServerAddressService;
import com.ndpmedia.rocketmq.nameserver.NameServerManager;
import com.ndpmedia.rocketmq.nameserver.model.NameServer;

import java.util.List;
import java.util.Set;

public class NameServerAddressServiceImpl implements NameServerAddressService {

    private NameServerManager nameServerManager;

    @Override
    public String listNameServer() {
        StringBuilder stringBuilder = new StringBuilder(256);
        Set<String> nameServers = nameServerManager.listNames();
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
            List<NameServer> nameServers = nameServerManager.list();

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
