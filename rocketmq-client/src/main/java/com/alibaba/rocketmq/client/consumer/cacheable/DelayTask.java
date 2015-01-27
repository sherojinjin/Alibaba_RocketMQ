package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.StashableMessage;
import org.slf4j.Logger;

public class DelayTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int BATCH_SIZE = 1000;

    private static final String NEXT_TIME_KEY = "next_time";

    private static final int TOL = 1000;

    private CacheableConsumer cacheableConsumer;

    public DelayTask(CacheableConsumer cacheableConsumer) {
        this.cacheableConsumer = cacheableConsumer;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Start re-consume messages");
            if (cacheableConsumer.getMessageQueue().remainingCapacity() == 0) {
                LOGGER.warn("Message queue is full. Won't fetch message from local message store.");
                return;
            }

            boolean isMessageQueueFull = false;
            StashableMessage[] messages = cacheableConsumer.getLocalMessageStore().pop(BATCH_SIZE);
            while (messages != null && messages.length > 0) {
                long current = System.currentTimeMillis();
                for (StashableMessage message : messages) {
                    if (null == message) {
                        continue;
                    }

                    if (null == message.getProperty(NEXT_TIME_KEY) || Long.parseLong(message.getProperty(NEXT_TIME_KEY)) - current < TOL) {
                        MessageHandler messageHandler = cacheableConsumer.getTopicHandlerMap().get(message.getTopic());
                        if (null == messageHandler) {
                            cacheableConsumer.getLocalMessageStore().stash(message);
                            continue;
                        }

                        if (cacheableConsumer.getMessageQueue().remainingCapacity() > 0) {
                            cacheableConsumer.getMessageQueue().put(message.buildMessageExt());
                        } else {
                            isMessageQueueFull = true;
                            cacheableConsumer.getLocalMessageStore().stash(message);
                        }
                    } else {
                        cacheableConsumer.getLocalMessageStore().stash(message);
                    }
                }

                if (!isMessageQueueFull) {
                    messages = cacheableConsumer.getLocalMessageStore().pop(BATCH_SIZE);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("DelayTask error", e);
        }
    }
}
