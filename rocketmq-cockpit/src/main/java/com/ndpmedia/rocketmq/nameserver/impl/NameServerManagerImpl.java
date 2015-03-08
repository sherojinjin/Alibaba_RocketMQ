package com.ndpmedia.rocketmq.nameserver.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.nameserver.NameServerManager;
import com.ndpmedia.rocketmq.nameserver.model.NameServer;
import com.ndpmedia.rocketmq.nameserver.model.NameServerRowMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service("nameServerManager")
public class NameServerManagerImpl implements NameServerManager {

    private CockpitDao cockpitDao;

    @Override
    public List<NameServer> list() {
        List<NameServer> list = null;
        try {
            String sql = "select * from name_server ";
            NameServerRowMapper nameServerRowMapper = new NameServerRowMapper();
            list = cockpitDao.getBeanList(sql, nameServerRowMapper);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Set<String> listNames() {
        Set<String> names = new HashSet<String>();
        try {
            List<NameServer> lists = list();
            for (NameServer nameServer : lists) {
                names.add(nameServer.getUrl());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return names;
    }

    @Override
    public List<NameServer> add(String nameServer) {
        if (null == nameServer || nameServer.trim().isEmpty()) {
            return list();
        }
        try {
            NameServer ns = new NameServer(nameServer, System.currentTimeMillis());
            String sql = " insert into name_server(ip, port , create_time) values(:ip, :port , :create_time) ";
            cockpitDao.add(sql, ns);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list();
    }

    @Override
    public List<NameServer> remove(String nameServer) {

        if (null == nameServer || nameServer.trim().isEmpty()) {
            return list();
        }

        try {
            String sql = " DELETE FROM name_server WHERE ip = '" + nameServer.split(":")[0] + "'";

            cockpitDao.del(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list();
    }

    public CockpitDao getCockpitDao() {
        return cockpitDao;
    }

    public void setCockpitDao(CockpitDao cockpitDao) {
        this.cockpitDao = cockpitDao;
    }
}
