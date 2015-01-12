package com.ndpmedia.rocketmq.cockpit.connection.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
import com.ndpmedia.rocketmq.cockpit.log.CockpitLogger;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * cockpit dao implement.
 */
public class CockpitDaoImpl<T> implements CockpitDao
{
    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private DataSource dataSource;

    private final Logger logger = CockpitLogger.getLogger();

    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;

    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate()
    {
        return namedParameterJdbcTemplate;
    }

    public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate)
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Map<String, Object> getFirstRow(String sql)
    {
        List<Map<String, Object>> list = getList(sql);
        if (list != null)
        {
            return list.get(0);
        }

        return null;
    }

    @Override
    public List<Map<String, Object>> getList(String sql)
    {
        logger.debug(" try to query : sql = [" + sql + " ]");
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public int add(String sql)
    {
        logger.debug(" try to query : sql = [" + sql + " ]");
        return jdbcTemplate.update(sql);
    }

    @Override
    public int add(String sql, Map<String, ?> params)
    {
        logger.debug(" try to query : sql = [" + sql + " ]");
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public int add(String sql, Object object)
    {
        logger.debug(" try to query : sql = [" + sql + " ]");
        SqlParameterSource source = new BeanPropertySqlParameterSource(object);
        return namedParameterJdbcTemplate.update(sql, source);
    }

    @Override
    public int del(String sql)
    {
        logger.debug(" try to query : sql = [" + sql + " ]");
        return jdbcTemplate.update(sql);
    }

    @Override
    public <T> List<T> getBeanList(String sql, RowMapper<T> rowMapper)
    {
        List<T> list = new ArrayList<T>();
        logger.debug(" try to query : sql = [" + sql + " ]");
        list.addAll(namedParameterJdbcTemplate.query(sql, rowMapper));
        return list;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }
}

