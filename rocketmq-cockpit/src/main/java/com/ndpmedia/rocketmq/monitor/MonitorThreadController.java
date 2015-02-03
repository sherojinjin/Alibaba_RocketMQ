package com.ndpmedia.rocketmq.monitor;

import com.ndpmedia.rocketmq.cockpit.log.CockpitLogger;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * monitor controller.
 * control monitor thread run one time per minute;
 */
public class MonitorThreadController
{
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private static Logger logger = CockpitLogger.getLogger();

    static
    {
        logger.debug(" start monitor ");
        long nextMinutes = 60 - (System.currentTimeMillis()/1000%60);
        scheduledExecutorService.scheduleWithFixedDelay(new MonitorTask(), nextMinutes, 600, TimeUnit.SECONDS);
    }
}
