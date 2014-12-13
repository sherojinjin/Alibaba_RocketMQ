package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

public class ResendMessageTask implements Runnable {

    /**
     * Indicate number of messages to retrieve from local message store each time.
     */
    private static final int BATCH_FETCH_MESSAGE_FROM_STORE_SIZE = 100;

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = ClientLogger.getLog();

    private LocalMessageStore localMessageStore;

    private MultiThreadMQProducer multiThreadMQProducer;

    public ResendMessageTask(LocalMessageStore localMessageStore, MultiThreadMQProducer multiThreadMQProducer) {
        this.localMessageStore = localMessageStore;
        this.multiThreadMQProducer = multiThreadMQProducer;
    }

    @Override
    public void run() {
        LOGGER.info("Start to re-send");
        if (localMessageStore.getNumberOfMessageStashed() == 0) {
            LOGGER.info("No stashed messages to re-send");
            return;
        }

        int popSize = Math.min(multiThreadMQProducer.getSemaphore().availablePermits(),
                BATCH_FETCH_MESSAGE_FROM_STORE_SIZE);

        if (popSize == 0) {
            LOGGER.info("No permits available in semaphore. Yield and wait for next round.");
            return;
        }

        Message[] messages = localMessageStore.pop(popSize);
        if (null == messages || messages.length == 0) {
            LOGGER.info("No stashed messages to re-send");
            return;
        }

        while (null != messages && messages.length > 0) {
            //Acquire tokens from semaphore.
            multiThreadMQProducer.getSemaphore().acquireUninterruptibly(messages.length);
            //Send messages with tokens.
            multiThreadMQProducer.send(messages, true);

            LOGGER.info(messages.length + " stashed messages re-sending completes: scheduled job submitted.");

            //Prepare for next loop step.
            popSize = Math.min(multiThreadMQProducer.getSemaphore().availablePermits(),
                    BATCH_FETCH_MESSAGE_FROM_STORE_SIZE);
            if (popSize == 0) {
                LOGGER.info("No permits available in semaphore. Break looping.");
                break;
            }
            messages = localMessageStore.pop(popSize);
        }
    }
}
