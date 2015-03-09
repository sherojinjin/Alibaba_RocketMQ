package com.ndpmedia.rocketmq.cockpit.service.impl;

import com.ndpmedia.rocketmq.cockpit.model.KV;
import com.ndpmedia.rocketmq.cockpit.model.KVStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KVRowMapper implements RowMapper<KV> {

    @Override
    public KV mapRow(ResultSet resultSet, int i) throws SQLException {
        KV kv = new KV();
        kv.setId(resultSet.getLong("id"));
        kv.setNameSpace(resultSet.getString("name_space"));
        kv.setKey(resultSet.getString("key"));
        kv.setValue(resultSet.getString("value"));
        kv.setStatus(KVStatus.valueOf(resultSet.getString("status")));
        return kv;
    }
}
