package com.ndpmedia.rocketmq.topic.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Administrator on 2015/2/13.
 */
public class TopicRowMapper<T> implements RowMapper {
    @Override
    public Topic mapRow(ResultSet rs, int rowNum) throws SQLException {
        Topic topic = new Topic();

        topic.setId(rs.getInt("id"));
        topic.setTopic(rs.getString("topic"));
        topic.setCluster_name(rs.getString("cluster_name"));
        topic.setBroker_address(rs.getString("broker_address"));
        topic.setWrite_queue_num(rs.getInt("write_queue_num"));
        topic.setRead_queue_num(rs.getInt("read_queue_num"));
        topic.setAllow(rs.getBoolean("allow"));
        topic.setOrder(rs.getBoolean("order_type"));

        return topic;
    }
}
