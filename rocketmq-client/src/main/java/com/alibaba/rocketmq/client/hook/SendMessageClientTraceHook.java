package com.alibaba.rocketmq.client.hook;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;

public class SendMessageClientTraceHook implements SendMessageHook {

    private Logger logger = ClientLogger.createLogger(LoggerName.RocketmqTracerLoggerName);

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
        long timeStamp = System.currentTimeMillis();
        logger.info("UUID: {}, TimeStamp: {}, ProducerGroup: {}, BornHost: {}, Topic: {}, Tags: {}, MsgId: {} --> " +
                        "Broker: {}, MessageQueue: {}, OffSet: {}, Status: {}, Source: {}",
                context.getMessage().getProperty(MessageConst.PROPERTY_MESSAGE_TRACE_ID),
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
                "CLIENT");
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        long timeStamp = System.currentTimeMillis();
        logger.info("UUID: {}, TimeStamp: {}, ProducerGroup: {}, BornHost: {}, Topic: {}, Tags: {}, MsgId: {} --> " +
                        "Broker: {}, MessageQueue: {}, Status: {}, Source: {}",
                context.getMessage().getProperty(MessageConst.PROPERTY_MESSAGE_TRACE_ID),
                timeStamp,
                context.getProducerGroup(),
                context.getBornHost(),
                context.getMessage().getTopic(),
                context.getMessage().getTags(),
                context.getSendResult().getMsgId(),
                context.getBrokerAddr(),
                context.getMq().getQueueId(),
                context.getSendResult().getQueueOffset(),
                context.getSendResult().getSendStatus().toString(),
                "CLIENT");

    }
}
