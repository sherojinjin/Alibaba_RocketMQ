package com.ndpmedia.rocketmq.nameserver.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KVRowMapper implements RowMapper<KV> {

    @Override
    public KV mapRow(ResultSet resultSet, int i) throws SQLException {
        KV kv = new KV();
        kv.setNameSpace(resultSet.getString("name_space"));
        kv.setKey(resultSet.getString("key"));
        kv.setValue(resultSet.getString("value"));
        return kv;
    }
}
