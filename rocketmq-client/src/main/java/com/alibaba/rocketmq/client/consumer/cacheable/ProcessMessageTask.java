package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

public class ProcessMessageTask implements Runnable {
    private MessageExt message;

    private MessageHandler messageHandler;

    private DefaultLocalMessageStore localMessageStore;

    public ProcessMessageTask(MessageExt message, MessageHandler messageHandler,
                              DefaultLocalMessageStore localMessageStore) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.localMessageStore = localMessageStore;
    }

    @Override
    public void run() {
        int result = messageHandler.handle(message);
        if (result > 0) {
            Message me = TranslateMsg.getMessageFromMessageExt(message);
            me.putUserProperty("next_time", String.valueOf(System.currentTimeMillis() + result));

            localMessageStore.stash(me);
        }
    }

}
