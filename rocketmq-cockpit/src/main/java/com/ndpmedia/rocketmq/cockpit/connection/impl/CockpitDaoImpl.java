package com.ndpmedia.rocketmq.cockpit.connection.impl;

import com.ndpmedia.rocketmq.cockpit.connection.CockpitDao;
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

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getList(String sql)
    {
        System.out.println(" try to query : sql = [" + sql + " ]" );
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public int add(String sql)
    {
        System.out.println(" try to query : sql = [" + sql + " ]" );
        return jdbcTemplate.update(sql);
    }

    public int add(String sql, Map params)
    {
        System.out.println(" try to query : sql = [" + sql + " ]" );
        return namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public int add(String sql, Object object)
    {
        System.out.println(" try to query : sql = [" + sql + " ]" );
        SqlParameterSource source = new BeanPropertySqlParameterSource(object);
        return namedParameterJdbcTemplate.update(sql, source);
    }

    public int del(String sql)
    {
        System.out.println(" try to query : sql = [" + sql + " ]" );
        return jdbcTemplate.update(sql);
    }

    public <T> List<T> getBeanList(String sql, RowMapper<T> rowMapper)
    {
        List<T> list = new ArrayList<T>();
        list.addAll(namedParameterJdbcTemplate.query(sql, rowMapper));
        return list;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }
}

