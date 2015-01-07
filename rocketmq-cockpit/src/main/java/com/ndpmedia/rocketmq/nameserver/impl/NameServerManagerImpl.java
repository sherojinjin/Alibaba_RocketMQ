package com.ndpmedia.rocketmq.nameserver.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.nameserver.NameServerManager;
import com.ndpmedia.rocketmq.nameserver.model.NameServer;
import com.ndpmedia.rocketmq.nameserver.model.NameServerRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service("nameServerManager")
public class NameServerManagerImpl implements NameServerManager {

    private static final ConcurrentHashMap<String, Long> NAME_SERVERS = new ConcurrentHashMap<String, Long>(16);

    private CockpitDao cockpitDao;

    @Override
    public Set<String> list() {
        try {
            if (NAME_SERVERS.isEmpty()) {


                String sql = "select * from name_server ";

                List<NameServer> list = cockpitDao.getBeanList(sql, new NameServerRowMapper());

                for (NameServer nameServer : list)
                {
                    NAME_SERVERS.putIfAbsent(nameServer.getUrl(), 0 < nameServer.getUpdate_time() ?
                            nameServer.getUpdate_time() : nameServer.getCreate_time());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NAME_SERVERS.keySet();
    }

    @Override
    public Set<String> list(boolean fromDisk) {
        try {
            NAME_SERVERS.clear();

            String sql = "select * from name_server ";

            List<NameServer> list = cockpitDao.getBeanList(sql, new NameServerRowMapper());

            for (NameServer nameServer : list)
            {
                NAME_SERVERS.putIfAbsent(nameServer.getUrl(), 0 < nameServer.getUpdate_time() ?
                        nameServer.getUpdate_time() : nameServer.getCreate_time());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return NAME_SERVERS.keySet();
    }


    @Override
    public ConcurrentHashMap<String, Long> listAll(boolean fromDisk) {
        try {
            NAME_SERVERS.clear();

            String sql = "select * from name_server ";

            List<NameServer> list = cockpitDao.getBeanList(sql, new NameServerRowMapper());

            for (NameServer nameServer : list)
            {
                NAME_SERVERS.putIfAbsent(nameServer.getUrl(), 0 < nameServer.getUpdate_time() ?
                        nameServer.getUpdate_time() : nameServer.getCreate_time());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return NAME_SERVERS;
    }

    @Override
    public Set<String> add(String nameServer) {
        if (null == nameServer || nameServer.trim().isEmpty()) {
            return NAME_SERVERS.keySet();
        }
        try {
            NameServer ns = new NameServer(nameServer, System.currentTimeMillis());
            String sql = " insert into name_server(ip, port , create_time) values(:ip, :port , :create_time) ";
            cockpitDao.add(sql, ns);
            return list(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return NAME_SERVERS.keySet();
    }

    @Override
    public Set<String> remove(String nameServer) {

        if (null == nameServer || nameServer.trim().isEmpty()) {
            return NAME_SERVERS.keySet();
        }

        try {
            list(true);
            NAME_SERVERS.remove(nameServer);
            String sql = " DELETE FROM name_server WHERE ip = " + nameServer.split(":")[0];

            cockpitDao.del(sql);
        } catch (Exception e) {
           e.printStackTrace();
        }
        return NAME_SERVERS.keySet();
    }

    public CockpitDao getCockpitDao() {
        return cockpitDao;
    }

    public void setCockpitDao(CockpitDao cockpitDao) {
        this.cockpitDao = cockpitDao;
    }
}
