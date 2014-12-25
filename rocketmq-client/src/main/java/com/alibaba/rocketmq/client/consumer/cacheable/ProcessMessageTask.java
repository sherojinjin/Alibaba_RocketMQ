package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.StashableMessage;
import org.slf4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

public class ProcessMessageTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final String NEXT_TIME_KEY = "next_time";

    private MessageExt message;

    private MessageHandler messageHandler;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

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
            long start = System.currentTimeMillis();
            LOGGER.info("Request business client to process message[msgId=" + message.getMsgId() + "]. CurrentTimeInMilliseconds:" + start);
            int result = messageHandler.handle(message);
            long end = System.currentTimeMillis();
            LOGGER.info("Business processing completes. CurrentTimeInMilliseconds:" + System.currentTimeMillis() + ". Cost: " + (end - start) + " milliseconds");

            //Remove the message from in-progress queue.
            if (null != messageQueue && messageQueue.contains(message)) {
                messageQueue.remove(message);
                LOGGER.info("Now " + messageQueue.size() + " messages in queue.");
            }

            if (result > 0) {
                StashableMessage stashableMessage = new StashableMessage(message);
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
