package com.alibaba.rocketmq.broker.mqtrace;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.alibaba.rocketmq.common.BrokerConfig;
import com.alibaba.rocketmq.common.constant.LoggerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumeMessageBrokerTraceHook implements ConsumeMessageHook {

    private String name;

    private Logger logger;

    public ConsumeMessageBrokerTraceHook(String name, BrokerConfig brokerConfig) throws JoranException {
        this.name = name;
        // 初始化Logback
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(brokerConfig.getRocketmqHome() + "/conf/logback_broker.xml");
        logger = LoggerFactory.getLogger(LoggerName.RocketmqTracerLoggerName);
    }

    @Override
    public String hookName() {
        return name;
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        long timeStamp = System.currentTimeMillis();
        for (String msgId : context.getMessageIds().keySet()) {
            logger.info("MsgId: {}, TimeStamp: {}, Broker: {}, MessageQueue: {} --> " +
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

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {

        long timeStamp = System.currentTimeMillis();
        for (String msgId : context.getMessageIds().keySet()) {
            logger.info("MsgId: {}, TimeStamp: {}, ConsumerGroup: {}, Client: {} --> " +
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
