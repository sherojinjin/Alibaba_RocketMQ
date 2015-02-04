package com.ndpmedia.rocketmq.monitor;

import com.ndpmedia.rocketmq.cockpit.util.Constant;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * monitor controller.
 * control monitor thread run one time per minute;
 */
public class MonitorThreadController implements Constant
{
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    static
    {
        long nextMinutes = SECONDS_OF_ONE_MINUTE - (System.currentTimeMillis() / THOUSAND % SECONDS_OF_ONE_MINUTE);
        scheduledExecutorService
                .scheduleWithFixedDelay(new MonitorTask(), nextMinutes, TEN * SECONDS_OF_ONE_MINUTE, TimeUnit.SECONDS);
    }
}
