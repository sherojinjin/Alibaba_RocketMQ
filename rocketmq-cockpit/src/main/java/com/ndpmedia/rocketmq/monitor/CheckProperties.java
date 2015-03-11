package com.ndpmedia.rocketmq.monitor;

import com.ndpmedia.rocketmq.cockpit.util.FileConstant;
import com.ndpmedia.rocketmq.io.FileManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * the properties of check box.
 * default:all check.
 */
public class CheckProperties implements FileConstant {
    private static Properties config = null;

    private static Map<String, Integer> checkSwitch = new HashMap<String, Integer>();

    static {
        config = FileManager.getConfig();
        initCheckMap();
    }

    private static void initCheckMap() {
        try {
            checkSwitch.put("diff", Integer.parseInt(config.getProperty("diff_check")));
            checkSwitch.put("topic", Integer.parseInt(config.getProperty("topic_check")));
            checkSwitch.put("consumer", Integer.parseInt(config.getProperty("consumer_check")));
            checkSwitch.put("producer", Integer.parseInt(config.getProperty("producer_check")));
        } catch (NumberFormatException e) {
            checkSwitch.put("diff", 1);
            checkSwitch.put("topic", 2);
            checkSwitch.put("consumer", 4);
            checkSwitch.put("producer", 8);
        }
    }

    public boolean check(String name) {
        boolean result = false;

        int checkKey = 15;
        try {
            checkKey = Integer.parseInt(config.getProperty(CHECK_PROPERTIES_KEY));
        } catch (NumberFormatException e) {

        }

        if ((checkKey & checkSwitch.get(name)) != 0)
            return true;
        return result;
    }
}
