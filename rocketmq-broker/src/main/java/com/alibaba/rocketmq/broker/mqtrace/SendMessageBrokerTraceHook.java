package com.alibaba.rocketmq.broker.mqtrace;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.alibaba.rocketmq.common.BrokerConfig;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.message.MessageConst;
import com.alibaba.rocketmq.common.message.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SendMessageBrokerTraceHook implements SendMessageHook {

    private String name;

    private Logger logger;

    public SendMessageBrokerTraceHook(String name, BrokerConfig brokerConfig) throws JoranException {
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
    public void sendMessageBefore(SendMessageContext context) {
        Map<String, String> properties = MessageDecoder.string2messageProperties(context.getMsgProps());
        if (!properties.containsKey(MessageConst.PROPERTY_MESSAGE_TRACE_ID)) {
            return;
        }

        long timeStamp = System.currentTimeMillis();
        logger.info("UUID: {}, TimeStamp: {}, ProducerGroup: {}, BornHost: {}, Topic: {}, Tags: {}, MsgId: {} --> " +
                        "Broker: {}, MessageQueue: {}, OffSet: {}, Status: {}, Source: {}",
                properties.get(MessageConst.PROPERTY_MESSAGE_TRACE_ID),
                timeStamp,
                context.getProducerGroup(),
                context.getBornHost(),
                context.getTopic(),
                properties.get(MessageConst.PROPERTY_TAGS),
                null,
                context.getBrokerAddr(),
                context.getQueueId(),
                context.getQueueOffset(),
                "RECEIVED",
                "BROKER");

    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        Map<String, String> properties = MessageDecoder.string2messageProperties(context.getMsgProps());
        if (!properties.containsKey(MessageConst.PROPERTY_MESSAGE_TRACE_ID)) {
            return;
        }
        
        long timeStamp = System.currentTimeMillis();
        logger.info("UUID: {}, TimeStamp: {}, ProducerGroup: {}, BornHost: {}, Topic: {}, Tags: {}, MsgId: {} --> " +
                        "Broker: {}, MessageQueue: {}, OffSet: {}, Status: {}, Source: {}",
                properties.get(MessageConst.PROPERTY_MESSAGE_TRACE_ID),
                timeStamp,
                context.getProducerGroup(),
                context.getBornHost(),
                context.getTopic(),
                properties.get(MessageConst.PROPERTY_TAGS),
                context.getMsgId(),
                context.getBrokerAddr(),
                context.getQueueId(),
                context.getQueueOffset(),
                "STORED",
                "BROKER");

    }
}
