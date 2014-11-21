package com.ndpmedia.rocketmq.web.listener;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.ndpmedia.rocketmq.consumer.pojo.ConsumerAdaptor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ExampleWebStartUpListener implements ServletContextListener {

    private ConsumerAdaptor consumerAdaptor = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContextEvent.getServletContext());
        consumerAdaptor = applicationContext.getBean("consumerAdaptor", ConsumerAdaptor.class);
        try {
            consumerAdaptor.start();
            System.out.println("Consumer starts");
        } catch (MQClientException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (null != consumerAdaptor) {
            consumerAdaptor.stop();
        }
    }
}
