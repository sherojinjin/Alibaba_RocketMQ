package com.ndpmedia.rocketmq.io;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class SqlManager
{

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static final Properties SQLS = new Properties();

    private static long changeTime = 0;

    static {
        load();
    }

    private static void load(){
        ClassLoader classLoader = SqlManager.class.getClassLoader();
        InputStream inputStream = null;
        try {
            inputStream = classLoader.getResourceAsStream("sql.properties");
            SQLS.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //Ignore.
                }
            }
        }
    }

    public static void reload(){
        load();
        changeTime++;
    }

    public static long getChangeTime()
    {
        return changeTime;
    }

    public static Properties getSqls()
    {
        return SQLS;
    }
}
