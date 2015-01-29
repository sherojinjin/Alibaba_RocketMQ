package com.alibaba.rocketmq.broker;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.alibaba.rocketmq.common.BrokerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrokerLogFactory {

    private volatile boolean configured;

    private BrokerLogFactory() {
    }

    public static void initialize(BrokerConfig brokerConfig) throws JoranException  {
        // 初始化Logback
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(brokerConfig.getRocketmqHome() + "/conf/logback_broker.xml");
    }

    public static Logger getLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }
}
