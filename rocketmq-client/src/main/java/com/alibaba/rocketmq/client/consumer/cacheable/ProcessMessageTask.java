package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.StashableMessage;
import org.slf4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ProcessMessageTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final String NEXT_TIME_KEY = "next_time";

    private MessageExt message;

    private MessageHandler messageHandler;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    private static final AtomicLong LOG_COUNTER = new AtomicLong(0L);

    private static final long LOG_PERFORMANCE_INTERVAL = 1000L;

    public ProcessMessageTask(MessageExt message, //Message to process.
                              MessageHandler messageHandler, //Message handler.
                              DefaultLocalMessageStore localMessageStore, //Local message store.
                              LinkedBlockingQueue<MessageExt> messageQueue //Buffered message queue.
    ) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.localMessageStore = localMessageStore;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {

            boolean logPerformance = LOG_COUNTER.incrementAndGet() % LOG_PERFORMANCE_INTERVAL == 0;

            long start = 0L;
            if (logPerformance) {
                start = System.currentTimeMillis();
            }

            int result = messageHandler.handle(message);

            if (logPerformance) {
                LOGGER.info("Business processing takes " + (System.currentTimeMillis() - start) + " ms");
            }

            //Remove the message from in-progress queue.
            if (null != messageQueue && messageQueue.contains(message)) {
                messageQueue.remove(message);
                LOGGER.info("Now " + messageQueue.size() + " messages in queue.");
            }

            if (result > 0) {
                StashableMessage stashableMessage = message.buildStashableMessage();
                stashableMessage.putUserProperty(NEXT_TIME_KEY, String.valueOf(System.currentTimeMillis() + result));
                LOGGER.info("Stashing message[msgId=" + message.getMsgId() + "] for later retry in " + result
                        + " milliseconds.");
                localMessageStore.stash(stashableMessage);
                LOGGER.info("Message stashed.");
            }
        } catch (Exception e) {
            LOGGER.error("ProcessMessageTask error", e);
        }
    }

}
