package com.ndpmedia.rocketmq.cockpit.util;

import com.ndpmedia.rocketmq.io.SqlManager;

import java.util.Map;
import java.util.Set;

/**
 * sql util.
 * auto get sql by sql key.
 * then change the $$begin part.
 * get the runnable sql.
 */
public class SqlParamsUtil implements Constant
{
    public static String getSQL(String sqlKey, Map<String, Object> params)
    {
        String sql = SqlManager.getSqls().getProperty(sqlKey);

        if (null != params && !params.isEmpty())
        {
            Set<String> paramKeys = params.keySet();
            for (String param : paramKeys)
            {
                sql = sql.replace(DOLLER + DOLLER + param, params.get(param).toString());
            }
        }
        return sql;
    }
}
