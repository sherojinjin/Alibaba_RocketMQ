package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayTask implements Runnable {
    private final MessageExt message;

    private final MessageHandler messageHandler;

    private final ScheduledExecutorService executorService;

    public DelayTask(ScheduledExecutorService executorService, MessageHandler messageHandler,
                     MessageExt message) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        int result = messageHandler.handle(message);
        if (result > 0) {
            this.executorService.schedule(this, result, TimeUnit.MILLISECONDS);
        }
    }
}
