package com.ndpmedia.rocketmq.authentication.model;

import org.springframework.security.core.AuthenticationException;

/**
 * Created by Administrator on 2015/1/13.
 */
public class CaptchaException extends AuthenticationException
{
    public CaptchaException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public CaptchaException(String msg)
    {
        super(msg);
    }
}
