package com.ndpmedia.rocketmq.cockpit.connection.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * cockpit dao implement.
 */
public class CockpitDaoImpl implements CockpitDao
{
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getList(String sql)
    {
        return jdbcTemplate.queryForList(sql);
    }
}

