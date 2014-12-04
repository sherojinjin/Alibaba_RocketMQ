package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessMessageTask implements Runnable {
    private MessageExt message;

    private MessageHandler messageHandler;

    private ScheduledExecutorService scheduledExecutorDelayService;

    public ProcessMessageTask(MessageExt message, MessageHandler messageHandler,
                              ScheduledExecutorService scheduledExecutorDelayService) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.scheduledExecutorDelayService = scheduledExecutorDelayService;
    }

    @Override
    public void run() {
        int result = messageHandler.handle(message);
        if (result > 0) {
            scheduledExecutorDelayService.schedule(
                    new DelayTask(scheduledExecutorDelayService, messageHandler, message),
                    result, TimeUnit.MILLISECONDS);



            //TODO Implement a message store with the following features.
            // 1) index for quick access;
            // 2) able to persist timestamp to execute;
            // 3) able to mark and sweep deprecated data without fraction.
            //localMessageStore.stash(message);
        }
    }

}
