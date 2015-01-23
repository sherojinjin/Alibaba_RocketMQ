package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.StashableMessage;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.slf4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class ProcessMessageTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final String NEXT_TIME_KEY = "next_time";

    private MessageExt message;

    private MessageHandler messageHandler;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    private LinkedBlockingQueue<MessageExt> inProgressMessageQueue;

    private SynchronizedDescriptiveStatistics statistics;

    public ProcessMessageTask(MessageExt message, //Message to process.
                              MessageHandler messageHandler, //Message handler.
                              DefaultLocalMessageStore localMessageStore, //Local message store.
                              LinkedBlockingQueue<MessageExt> messageQueue, //Buffered message queue.
                              LinkedBlockingQueue<MessageExt> inProgressMessageQueue,
                              SynchronizedDescriptiveStatistics statistics
    ) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.localMessageStore = localMessageStore;
        this.messageQueue = messageQueue;
        this.inProgressMessageQueue = inProgressMessageQueue;
        this.statistics = statistics;
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            int result = messageHandler.handle(message);
            if (0 == result) {
                statistics.addValue(System.currentTimeMillis() - start);
            } else if (result > 0) {
                StashableMessage stashableMessage = message.buildStashableMessage();
                stashableMessage.putUserProperty(NEXT_TIME_KEY, String.valueOf(System.currentTimeMillis() + result));
                LOGGER.info("Stashing message[msgId=" + message.getMsgId() + "] for later retry in " + result + " ms.");
                localMessageStore.stash(stashableMessage);
                LOGGER.info("Message stashed.");
            } else {
                LOGGER.error("Unable to process returning result: " + result);
            }
        } catch (Exception e) {
            LOGGER.error("ProcessMessageTask failed! Automatic retry scheduled.", e);
            messageQueue.offer(message);
        } finally {
            inProgressMessageQueue.remove(message);
        }
    }

}
