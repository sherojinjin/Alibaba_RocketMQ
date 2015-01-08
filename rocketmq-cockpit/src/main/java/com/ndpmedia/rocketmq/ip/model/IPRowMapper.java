package com.ndpmedia.rocketmq.ip.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * row mapper for ip pair.
 */
public class IPRowMapper<T> implements RowMapper
{
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        IPPair ipPair = new IPPair();

        ipPair.setId(rs.getInt("id"));
        ipPair.setInnerIP(rs.getString("inner_ip"));
        ipPair.setPublicIP(rs.getString("public_ip"));
        ipPair.setCreate_time(rs.getLong("create_time"));
        ipPair.setUpdate_time(rs.getLong("update_time"));

        return ipPair;
    }
}
