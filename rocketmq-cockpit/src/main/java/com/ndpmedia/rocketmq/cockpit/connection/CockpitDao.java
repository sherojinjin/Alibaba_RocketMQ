package com.ndpmedia.rocketmq.cockpit.connection;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

/**
 * cockpit dao .
 */
public interface CockpitDao {
    public Map<String, Object> getFirstRow(String sql);

    public List<Map<String, Object>> getList(String sql);

    public <T> List<T> getBeanList(String sql, RowMapper<T> rowMapper);

    public int add(String sql);

    public int add(String sql, Map<String, ?> params);

    public int add(String sql, Object object);

    public int del(String sql);

}
