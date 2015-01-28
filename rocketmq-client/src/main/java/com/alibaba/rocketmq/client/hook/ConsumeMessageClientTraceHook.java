package com.alibaba.rocketmq.client.hook;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;

public class ConsumeMessageClientTraceHook implements ConsumeMessageHook {

    private String name;

    private Logger logger;

    public ConsumeMessageClientTraceHook(String name) {
        this.name = name;
        logger = ClientLogger.createLogger(LoggerName.RocketmqTracerLoggerName);
    }

    @Override
    public String hookName() {
        return name;
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        long timeStamp = System.currentTimeMillis();
        for (MessageExt messageExt : context.getMsgList()) {
            logger.info("MsgId: {}, TimeStamp: {}, Broker: {}, MessageQueue: {} --> " +
                            "ConsumerGroup: {}, Client: {}, Topic: {}, Tags: {}, Status: {}, Source: {}",
                    messageExt.getMsgId(),
                    timeStamp,
                    context.getMq().getBrokerName(),
                    context.getMq().getQueueId(),
                    context.getConsumerGroup(),
                    messageExt.getTopic(),
                    messageExt.getTags(),
                    context.getStatus(),
                    "CLIENT");
        }
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        long timeStamp = System.currentTimeMillis();
        for (MessageExt messageExt : context.getMsgList()) {
            logger.info("MsgId: {}, TimeStamp: {}, Broker: {}, MessageQueue: {} --> " +
                            "ConsumerGroup: {}, Client: {}, Topic: {}, Tags: {}, Status: {}, Source: {}",
                    messageExt.getMsgId(),
                    timeStamp,
                    context.getMq().getBrokerName(),
                    context.getMq().getQueueId(),
                    context.getConsumerGroup(),
                    messageExt.getTopic(),
                    messageExt.getTags(),
                    context.getStatus(),
                    "CLIENT");
        }

    }
}
