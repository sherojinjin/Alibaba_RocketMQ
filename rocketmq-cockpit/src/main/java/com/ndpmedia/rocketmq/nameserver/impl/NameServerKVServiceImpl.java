package com.ndpmedia.rocketmq.nameserver.impl;

import com.ndpmedia.rocketmq.nameserver.NameServerKVService;
import com.ndpmedia.rocketmq.nameserver.model.KV;
import com.ndpmedia.rocketmq.nameserver.model.KVRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TODO implement this.
 */
@Service("nameServerKVService")
public class NameServerKVServiceImpl implements NameServerKVService {

    private static final String SQL_ADD =
            "INSERT INTO name_server_kv(id, name_space, `key`, `value`) VALUES (NULL, ?, ?, ?)";

    private static final String SQL_DELETE = "DELETE FROM name_server_kv WHERE id = ?";

    private static final String SQL_UPDATE =
            "UPDATE name_server_kv SET name_space = ?, `key` = ?, `value` = ? WHERE id = ?";


    private static final String SQL_QUERY_BY_ID = "SELECT id, name_space, `key`, `value` FROM name_server_kv WHERE id = ?";

    private static final String SQL_QUERY_ALL = "SELECT id, name_space, `key`, `value` FROM name_server_kv";


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void add(KV kv) {
        jdbcTemplate.update(SQL_ADD, kv.getNameSpace(), kv.getKey(), kv.getValue());
    }

    @Override
    public void delete(KV kv) {
        delete(kv.getId());
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }

    @Override
    public void update(KV kv) {
        jdbcTemplate.update(SQL_UPDATE, kv.getNameSpace(), kv.getKey(), kv.getValue(), kv.getId());
    }

    @Override
    public KV get(long id) {
        return jdbcTemplate.queryForObject(SQL_QUERY_BY_ID, new KVRowMapper(), id);
    }

    @Override
    public List<KV> list() {
        return jdbcTemplate.query(SQL_QUERY_ALL, new KVRowMapper());
    }
}
