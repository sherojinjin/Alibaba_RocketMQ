package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

public class ResendMessageTask implements Runnable {

    /**
     * Indicate number of messages to retrieve from local message store each time.
     */
    private static final int BATCH_FETCH_MESSAGE_FROM_STORE_SIZE = 1000;

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
        LOGGER.debug("Start to resend");
        Message[] messages = localMessageStore.pop(BATCH_FETCH_MESSAGE_FROM_STORE_SIZE);
        while (null != messages && messages.length > 0) {
            multiThreadMQProducer.send(messages);
            messages = localMessageStore.pop(BATCH_FETCH_MESSAGE_FROM_STORE_SIZE);
        }
    }
}
