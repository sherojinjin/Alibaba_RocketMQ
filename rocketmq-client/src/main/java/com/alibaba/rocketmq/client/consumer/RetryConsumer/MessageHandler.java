package com.alibaba.rocketmq.client.consumer.RetryConsumer;

import com.alibaba.rocketmq.common.message.Message;

/**
 * 消费处理接口，
 *
 * @author robert
 * @since 2014-12-03
 */
public interface MessageHandler {

    public int handle(Message message);
}
