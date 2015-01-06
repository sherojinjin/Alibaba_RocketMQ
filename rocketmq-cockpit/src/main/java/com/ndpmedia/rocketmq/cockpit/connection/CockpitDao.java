package com.ndpmedia.rocketmq.cockpit.connection;

import java.util.List;
import java.util.Map;

/**
 * cockpit dao .
 */
public interface CockpitDao
{
    public List<Map<String, Object>> getList(String sql);
}
