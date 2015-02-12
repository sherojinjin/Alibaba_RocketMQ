package com.ndpmedia.rocketmq.ip.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.cockpit.util.SqlParamsUtil;
import com.ndpmedia.rocketmq.ip.IPMappingManager;
import com.ndpmedia.rocketmq.ip.model.IPPair;
import com.ndpmedia.rocketmq.ip.model.IPRowMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service("ipMappingManager")
public class IPMappingManagerImpl implements IPMappingManager
{

    private CockpitDao cockpitDao;

    @Override
    public void remove(Map<String, Object> params) throws IOException
    {
        try
        {
            String sql = SqlParamsUtil.getSQL("ip.delete", params);
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
            String sql = SqlParamsUtil.getSQL("ip.add", null);
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
            String sql = SqlParamsUtil.getSQL("ip.get", null);
            IPRowMapper<IPPair> ipRowMapper = new IPRowMapper<IPPair>();
            list = cockpitDao.getBeanList(sql, ipRowMapper);
            if (list != null && !list.isEmpty())
            {
                return list.get(0).getPublicIP();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public List<IPPair> list() throws IOException
    {
        List<IPPair> list = null;
        try
        {
            String sql = SqlParamsUtil.getSQL("ip.all", null);
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
