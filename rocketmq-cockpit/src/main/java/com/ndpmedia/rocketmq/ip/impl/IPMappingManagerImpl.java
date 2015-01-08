package com.ndpmedia.rocketmq.ip.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.ip.IPMappingManager;
import com.ndpmedia.rocketmq.ip.model.IPPair;
import com.ndpmedia.rocketmq.ip.model.IPRowMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service("ipMappingManager")
public class IPMappingManagerImpl implements IPMappingManager
{

    private CockpitDao cockpitDao;

    @Override
    public void remove(String innerIP) throws IOException
    {
        try
        {
            String sql = " DELETE FROM ip_mapping WHERE inner_ip = '" + innerIP + "'";
            cockpitDao.del(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void add(String innerIP, String publicIP) throws IOException
    {
        try
        {
            IPPair ipPair = new IPPair(innerIP, publicIP);
            String sql = " INSERT INTO ip_mapping(inner_ip, public_ip, create_time) values(:innerIP, :publicIP, :create_time)";
            cockpitDao.add(sql, ipPair);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String lookup(String privateIP) throws IOException
    {
        List<IPPair> list;
        try
        {
            String sql = " SELECT * FROM ip_mapping WHERE inner_ip = '" + privateIP + "'";
            IPRowMapper<IPPair> ipRowMapper = new IPRowMapper<IPPair>();
            list = cockpitDao.getBeanList(sql, ipRowMapper);
            if (list != null)
            {
                list.get(0).getPublicIP();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<IPPair> list() throws IOException
    {
        List<IPPair> list = null;
        try
        {
            String sql = " SELECT * FROM ip_mapping ";
            IPRowMapper<IPPair> ipRowMapper = new IPRowMapper<IPPair>();
            list = cockpitDao.getBeanList(sql, ipRowMapper);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return list;
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
