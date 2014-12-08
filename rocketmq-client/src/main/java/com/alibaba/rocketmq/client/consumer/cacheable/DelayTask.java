package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.*;

public class DelayTask implements Runnable {
    private final MessageHandler messageHandler;

    private DefaultLocalMessageStore localMessageStore;

    public DelayTask(MessageHandler messageHandler,
                     DefaultLocalMessageStore localMessageStore) {
        this.localMessageStore = localMessageStore;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        Message[] messages = localMessageStore.pop();
        TreeMap<String, MessageExt> messageExtMap = getMessageTree(messages);

        for (Message message : messages) {
            if (null == message)
                continue;
            MessageExt messageExt = messageExtMap.get(message.getProperty("msgId"));

            if (Long.parseLong(message.getProperty("next_time")) - System.currentTimeMillis() < 1000) {
                int result = messageHandler.handle(messageExt);
                if (result > 0) {
                    message.putUserProperty("next_time", String.valueOf(System.currentTimeMillis() + result));
                    localMessageStore.stash(message);
                }
            } else {
                localMessageStore.stash(message);
            }
        }
    }

    private TreeMap<String, MessageExt> getMessageTree(Message[] messages) {
        TreeMap<String, MessageExt> messageTreeMap = new TreeMap<String, MessageExt>();

        for (final Message message : messages) {
            if (null == message)
                continue;
            MessageExt me = TranslateMsg.getMessageExtFromMessage(message);
            me.putUserProperty("next_time", message.getProperty("next_time"));
            messageTreeMap.put(me.getMsgId(), me);
        }

        return messageTreeMap;
    }
}
