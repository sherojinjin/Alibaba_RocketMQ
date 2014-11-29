package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class DefaultLocalMessageStore implements LocalMessageStore {

    private static final Logger LOGGER = ClientLogger.getLog();

    private final AtomicLong numberOfMessageStashed = new AtomicLong(0L);

    @Override
    public void stash(Message message) {
        LOGGER.error("Stashing message: {}", JSON.toJSONString(message));
        numberOfMessageStashed.incrementAndGet();
    }

    @Override
    public Message[] pop() {
        return new Message[0];
    }

    public long getNumberOfMessageStashed() {
        return numberOfMessageStashed.longValue();
    }
}
