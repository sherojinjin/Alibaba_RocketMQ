package com.alibaba.rocketmq.broker.mqtrace;

import ch.qos.logback.core.joran.spi.JoranException;
import com.alibaba.rocketmq.broker.BrokerLogFactory;
import com.alibaba.rocketmq.common.constant.LoggerName;
import org.slf4j.Logger;

public class ConsumeMessageBrokerTraceHook implements ConsumeMessageHook {

    private String name;

    private Logger logger;

    public ConsumeMessageBrokerTraceHook(String name) throws JoranException {
        this.name = name;
        logger = BrokerLogFactory.getLogger(LoggerName.RocketmqTracerLoggerName);
    }

    @Override
    public String hookName() {
        return name;
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        if (logger.isDebugEnabled()) {
            long timeStamp = System.currentTimeMillis();
            for (String msgId : context.getMessageIds().keySet()) {
                logger.debug("MsgId: {}, TimeStamp: {}, Broker: {}, MessageQueue: {} --> " +
                                "ConsumerGroup: {}, Client: {}, Status: {}, Source: {}",
                        msgId,
                        timeStamp,
                        context.getStoreHost(),
                        context.getQueueId(),
                        context.getConsumerGroup(),
                        context.getClientHost(),
                        context.getStatus(),
                        "BROKER");
            }
        }
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        if (logger.isDebugEnabled()) {
            long timeStamp = System.currentTimeMillis();
            for (String msgId : context.getMessageIds().keySet()) {
                logger.debug("MsgId: {}, TimeStamp: {}, ConsumerGroup: {}, Client: {} --> " +
                                "Broker: {}, MessageQueue: {}, Status: {}, Source: {}",
                        msgId,
                        timeStamp,
                        context.getStoreHost(),
                        context.getQueueId(),
                        context.getConsumerGroup(),
                        context.getClientHost(),
                        context.getStatus(),
                        "BROKER");
            }
        }
    }

}
