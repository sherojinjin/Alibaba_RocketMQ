package com.ndpmedia.rocketmq.nameserver.impl;

import com.ndpmedia.rocketmq.nameserver.NameServerKVService;
import com.ndpmedia.rocketmq.nameserver.model.KV;
import com.ndpmedia.rocketmq.nameserver.model.KVRowMapper;
import com.ndpmedia.rocketmq.nameserver.model.KVStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("nameServerKVService")
public class NameServerKVServiceImpl implements NameServerKVService {

    private static final String SQL_ADD =
            "INSERT INTO name_server_kv(id, name_space, `key`, `value`, status_id) VALUES (NULL, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE name_server_kv SET name_space = ?, `key` = ?, `value` = ?, status_id = ? WHERE id = ?";


    private static final String SQL_QUERY_BY_ID =
            "SELECT ns_kv.id, name_space, `key`, `value`, status_lu.name AS status " +
            "FROM name_server_kv AS ns_kv " +
            "JOIN name_server_kv_status_lu AS status_lu ON ns_kv.status_id = status_lu.id  " +
            "WHERE ns_kv.id = ?";

    private static final String SQL_QUERY_ALL =
            "SELECT ns_kv.id, name_space, `key`, `value`, status_lu.name AS status " +
            "FROM name_server_kv AS ns_kv " +
            "JOIN name_server_kv_status_lu AS status_lu ON ns_kv.status_id = status_lu.id";

    private static final String SQL_QUERY_ALL_BY_STATUS =
            "SELECT ns_kv.id, name_space, `key`, `value`, status_lu.name AS status " +
                    "FROM name_server_kv AS ns_kv " +
                    "JOIN name_server_kv_status_lu AS status_lu ON ns_kv.status_id = status_lu.id " +
                    "WHERE ns_kv.status_id IN (%s)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void add(KV kv) {
        jdbcTemplate.update(SQL_ADD, kv.getNameSpace(), kv.getKey(), kv.getValue(), kv.getStatus().getId());
    }

    @Override
    public void delete(KV kv) {
        kv.setStatus(KVStatus.DELETED);
        update(kv);
    }

    @Override
    public void delete(long id) {
        delete(get(id));
    }

    @Override
    public void update(KV kv) {
        jdbcTemplate.update(SQL_UPDATE, kv.getNameSpace(), kv.getKey(), kv.getValue(), kv.getId(),
                kv.getStatus().getId());
    }

    @Override
    public KV get(long id) {
        return jdbcTemplate.queryForObject(SQL_QUERY_BY_ID, new KVRowMapper(), id);
    }

    @Override
    public List<KV> list() {
        List<KV> kvList = jdbcTemplate.query(SQL_QUERY_ALL, new KVRowMapper());
        Collections.sort(kvList);
        return kvList;
    }

    @Override
    public List<KV> list(KVStatus... statuses) {
        if (null == statuses) {
            return list();
        } else {
            int[] statusArray = new int[statuses.length];
            int i = 0;
            for (KVStatus status : statuses) {
                statusArray[i++] = status.getId();
            }
            List<KV> kvList = jdbcTemplate.query(String.format(SQL_QUERY_ALL_BY_STATUS, arrayToString(statusArray)),
                    new KVRowMapper());
            Collections.sort(kvList);
            return kvList;
        }
    }

    private static String arrayToString(int[] array) {
        if (null == array || 0 == array.length) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (0 != i) {
                stringBuilder.append(", ").append(array[i]);
            } else {
                stringBuilder.append(array[i]);
            }
        }

        return stringBuilder.toString();
    }
}
