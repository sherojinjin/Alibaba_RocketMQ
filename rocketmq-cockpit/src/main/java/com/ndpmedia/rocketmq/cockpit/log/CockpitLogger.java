package com.ndpmedia.rocketmq.cockpit.log;

import com.alibaba.rocketmq.common.constant.LoggerName;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * cockpit log
 */
public class CockpitLogger
{
    private static Logger logger;

    static
    {
        logger = createLogger(LoggerName.RocketMqCockpitLoggerName);
    }

    private static Logger createLogger(String logName)
    {
        String logConfigFilePath = System
                .getProperty("rocketmq.client.log.configFile", System.getenv("ROCKETMQ_CLIENT_LOG_CONFIGFILE"));
        Boolean isloadconfig = Boolean.parseBoolean(System.getProperty("rocketmq.client.log.loadconfig", "true"));

        final String log4j_resource_file = System
                .getProperty("rocketmq.client.log4j.resource.fileName", "log4j_rocketmq_cockpit.xml");

        final String logback_resource_file = System
                .getProperty("rocketmq.client.logback.resource.fileName", "logback_rocketmq_cockpit.xml");

        if (isloadconfig)
        {
            try
            {
                ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
                Class classType = iLoggerFactory.getClass();
                if (classType.getName().equals("org.slf4j.impl.Log4jLoggerFactory"))
                {
                    Class<?> DOMConfigurator = null;
                    Object DOMConfiguratorObj = null;
                    DOMConfigurator = Class.forName("org.apache.log4j.xml.DOMConfigurator");
                    DOMConfiguratorObj = DOMConfigurator.newInstance();
                    if (null == logConfigFilePath)
                    {
                        // 如果应用没有配置，则使用jar包内置配置
                        Method configure = DOMConfiguratorObj.getClass().getMethod("configure", URL.class);
                        URL url = CockpitLogger.class.getClassLoader().getResource(log4j_resource_file);
                        configure.invoke(DOMConfiguratorObj, url);
                    }
                    else
                    {
                        Method configure = DOMConfiguratorObj.getClass().getMethod("configure", String.class);
                        configure.invoke(DOMConfiguratorObj, logConfigFilePath);
                    }

                }
                else if (classType.getName().equals("ch.qos.logback.classic.LoggerContext"))
                {
                    Class<?> joranConfigurator = null;
                    Class<?> context = Class.forName("ch.qos.logback.core.Context");
                    Object joranConfiguratoroObj = null;
                    joranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator");
                    joranConfiguratoroObj = joranConfigurator.newInstance();
                    Method setContext = joranConfiguratoroObj.getClass().getMethod("setContext", context);
                    setContext.invoke(joranConfiguratoroObj, iLoggerFactory);
                    if (null == logConfigFilePath)
                    {
                        // 如果应用没有配置，则使用jar包内置配置
                        URL url = CockpitLogger.class.getClassLoader().getResource(logback_resource_file);
                        Method doConfigure = joranConfiguratoroObj.getClass().getMethod("doConfigure", URL.class);
                        doConfigure.invoke(joranConfiguratoroObj, url);
                    }
                    else
                    {
                        Method doConfigure = joranConfiguratoroObj.getClass().getMethod("doConfigure", String.class);
                        doConfigure.invoke(joranConfiguratoroObj, logConfigFilePath);
                    }

                }
            }
            catch (Exception e)
            {
                System.err.println(e);
            }
        }
        return LoggerFactory.getLogger(logName);
    }

    public static Logger getLogger()
    {
        return logger;
    }

    public static void setLogger(Logger logger)
    {
        CockpitLogger.logger = logger;
    }
}
