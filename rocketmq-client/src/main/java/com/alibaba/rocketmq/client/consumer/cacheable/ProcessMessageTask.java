package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessMessageTask implements Runnable {
    private Message message;

    private MessageHandler messageHandler;

    private ScheduledExecutorService scheduledExecutorDelayService;

    private DefaultLocalMessageStore localMessageStore;

    public ProcessMessageTask(Message message, MessageHandler messageHandler,
                              ScheduledExecutorService scheduledExecutorDelayService, DefaultLocalMessageStore localMessageStore) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.scheduledExecutorDelayService = scheduledExecutorDelayService;
        this.localMessageStore = localMessageStore;
    }

    @Override
    public void run() {
        int result = messageHandler.handle(message);
        if (result > 0) {
            Message tmp = new Message(message.getTopic(), message.getTags(), message.getKeys(), message.getBody());
            tmp.putUserProperty("next_time", String.valueOf(System.currentTimeMillis() + result));
            localMessageStore.stash(tmp);

            scheduledExecutorDelayService.schedule(
                    new DelayTask(scheduledExecutorDelayService, messageHandler, localMessageStore),
                    result, TimeUnit.MILLISECONDS);
        }
    }

}
