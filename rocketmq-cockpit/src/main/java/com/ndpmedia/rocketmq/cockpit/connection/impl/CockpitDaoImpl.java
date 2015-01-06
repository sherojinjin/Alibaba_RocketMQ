package com.ndpmedia.rocketmq.cockpit.connection.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

/**
 * Created by Administrator on 2015/1/6.
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

}

