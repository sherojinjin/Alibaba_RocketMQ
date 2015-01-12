package com.ndpmedia.rocketmq.babel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Helper {

    private static Properties properties = new Properties();

    static {
        InputStream inputStream = null;
        try {
            ClassLoader classLoader = ConsumerService.class.getClassLoader();
            inputStream = classLoader.getResourceAsStream("rocketmq_client_setting.properties");

            if (null != inputStream) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
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


    public static Properties getConfig() {
        return properties;
    }
}
