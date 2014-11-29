package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;

public interface LocalMessageStore {

    void stash(Message message);

    public int getNumberOfMessageStashed();

    Message[] pop();
}
