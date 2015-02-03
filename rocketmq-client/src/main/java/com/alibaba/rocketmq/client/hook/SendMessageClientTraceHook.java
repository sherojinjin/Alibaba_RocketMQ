package com.alibaba.rocketmq.client.hook;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.constant.LoggerName;
import org.slf4j.Logger;

public class SendMessageClientTraceHook implements SendMessageHook {

    private Logger logger = ClientLogger.getLog(LoggerName.RocketmqTracerLoggerName);

    private String name;

    public SendMessageClientTraceHook(String name) {
        this.name = name;
    }

    @Override
    public String hookName() {
        return name;
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        if (!context.getMessage().isTraceable()) {
            return;
        }

        long timeStamp = System.currentTimeMillis();
        logger.info("TracerId: {}, TimeStamp: {}, ProducerGroup: {}, BornHost: {}, Topic: {}, Tags: {}, MsgId: {} --> " +
                        "Broker: {}, MessageQueue: {}, OffSet: {}, Status: {}, Source: {}",
                context.getMessage().getTracerId(),
                timeStamp,
                context.getProducerGroup(),
                context.getBornHost(),
                context.getMessage().getTopic(),
                context.getMessage().getTags(),
                null,
                context.getBrokerAddr(),
                context.getMq().getQueueId(),
                null,
                "BEFORE_SEND",
                "PRODUCER");
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        if (!context.getMessage().isTraceable()) {
            return;
        }

        long timeStamp = System.currentTimeMillis();
        logger.info("TracerId: {}, TimeStamp: {}, ProducerGroup: {}, BornHost: {}, Topic: {}, Tags: {}, MsgId: {} --> " +
                        "Broker: {}, MessageQueue: {}, QueueOffset: {}, Status: {}, Source: {}",
                context.getMessage().getTracerId(),
                timeStamp,
                context.getProducerGroup(),
                context.getBornHost(),
                context.getMessage().getTopic(),
                context.getMessage().getTags(),
                null == context.getSendResult() ? null : context.getSendResult().getMsgId(),
                context.getBrokerAddr(),
                null == context.getMq() ? null : context.getMq().getQueueId(),
                null == context.getSendResult() ? "UNKNOWN" : context.getSendResult().getQueueOffset(),
                null == context.getSendResult() ? "AFTER_SEND" : context.getSendResult().getSendStatus().toString(),
                "PRODUCER");

    }
}
