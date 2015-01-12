package com.ndpmedia.rocketmq.authentication.model;

/**
 * the user login type.
 * login failed - retry times
 */
public class LoginType
{
    private String name;
    private int retryTimes;
    private long lockTime;
    private int status;

    public LoginType()
    {
    }

    public LoginType(String name)
    {
        this.name = name;
        this.retryTimes = 0;
        this.status = 0;
        this.lockTime = 0L;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getRetryTimes()
    {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes)
    {
        this.retryTimes = retryTimes;
    }

    public long getLockTime()
    {
        return lockTime;
    }

    public void setLockTime(long lockTime)
    {
        this.lockTime = lockTime;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return "LoginType{" +
                "name='" + name + '\'' +
                ", retryTimes='" + retryTimes + '\'' +
                ", lockTime=" + lockTime +
                ", status=" + status +
                '}';
    }
}
