package com.alibaba.rocketmq.client.producer;

public enum TraceLevel {
    /**
     * Trace every message.
     */
    DEBUG,

    /**
     * Trace every 512 messages.
     */
    MEDIUM,

    /**
     * Trace every 1024 messages.
     */
    PRODUCTION
}
