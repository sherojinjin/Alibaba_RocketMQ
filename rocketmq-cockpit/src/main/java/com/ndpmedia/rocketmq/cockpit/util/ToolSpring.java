package com.ndpmedia.rocketmq.cockpit.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;

/**
 * get spring beans.
 * when need,can get it.
 */
public class ToolSpring extends ApplicationObjectSupport
{
    private static ApplicationContext applicationContext = null;

    @Override
    protected void initApplicationContext(ApplicationContext context) throws BeansException
    {
        super.initApplicationContext(context);
        if (null == ToolSpring.applicationContext)
            ToolSpring.applicationContext = context;
    }

    public static Object getBean(String beanName)
    {
        return applicationContext.getBean(beanName);
    }
}
