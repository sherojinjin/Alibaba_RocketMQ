package com.ndpmedia.rocketmq.nameserver.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * the row mapper for name server.
 */
public class NameServerRowMapper implements RowMapper
{
    @Override
    public NameServer mapRow(ResultSet rs, int rowNum) throws SQLException {
        NameServer nameServer = new NameServer();

        nameServer.setId(rs.getInt("id"));
        nameServer.setIp(rs.getString("ip"));
        nameServer.setPort(rs.getString("port"));
        nameServer.setCreate_time(rs.getLong("create_time"));
        nameServer.setUpdate_time(rs.getLong("update_time"));
        nameServer.makeUrl();

        return nameServer;
    }
}
