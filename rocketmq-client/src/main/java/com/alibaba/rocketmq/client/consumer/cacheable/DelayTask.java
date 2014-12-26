package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.StashableMessage;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DelayTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int BATCH_SIZE = 1000;

    private static final String NEXT_TIME_KEY = "next_time";

    private static final int TOL = 1000;

    private ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    public DelayTask(ConcurrentHashMap<String, MessageHandler> topicHandlerMap,
                     DefaultLocalMessageStore localMessageStore,
                     LinkedBlockingQueue<MessageExt> messageQueue
    ) {
        this.localMessageStore = localMessageStore;
        this.topicHandlerMap = topicHandlerMap;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Start re-consume messages");

            if (messageQueue.remainingCapacity() == 0) {
                LOGGER.info("Message queue is full. Won't fetch message from local message store.");
                return;
            }

            boolean isMessageQueueFull = false;
            StashableMessage[] messages = localMessageStore.pop(BATCH_SIZE);
            while (messages != null && messages.length > 0) {
                long current = System.currentTimeMillis();
                for (StashableMessage message : messages) {
                    if (null == message) {
                        continue;
                    }

                    /**
                     * It's time to process.
                     *
                     * Stashed messages generally have two sources:
                     * 1) The message was stashed because there was no space in message queue then.
                     * 2) Stashed because business client prefers to process later on.
                     */
                    if (null == message.getProperty(NEXT_TIME_KEY)
                            || Long.parseLong(message.getProperty(NEXT_TIME_KEY)) - current < TOL) {
                        MessageHandler messageHandler = topicHandlerMap.get(message.getTopic());
                        if (null == messageHandler) {
                            // On restart, fewer message handler may be registered so some messages stored locally
                            // may not get processed. We should warn this.
                            localMessageStore.stash(message);
                            continue;
                        }

                        if (messageQueue.remainingCapacity() > 0) {
                            //JobSubmitter will wrap the message into ProcessMessageTask and executorWorkerService will
                            //deliver this message to business client.
                            messageQueue.put(message.buildMessageExt());
                        } else {
                            //Mark the messageQueue full and stash the message.
                            isMessageQueueFull = true;
                            localMessageStore.stash(message);
                        }

                    } else {
                        //It's too early to process, stash the message again and wait for next round.
                        localMessageStore.stash(message);
                    }
                }

                if (!isMessageQueueFull) {
                    //Pop more messages from local message store.
                    messages = localMessageStore.pop(BATCH_SIZE);
                } else {
                    //As messageQueue is full, we need to break the loop of popping message from local message store.
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("DelayTask error", e);
        }
    }
}
