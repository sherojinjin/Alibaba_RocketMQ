package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayTask implements Runnable {

    private final MessageHandler messageHandler;

    private final ScheduledExecutorService executorService;

    private DefaultLocalMessageStore localMessageStore;

    public DelayTask(ScheduledExecutorService executorService, MessageHandler messageHandler,
                     DefaultLocalMessageStore localMessageStore) {
        this.localMessageStore = localMessageStore;
        this.messageHandler = messageHandler;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        Message[] messages = localMessageStore.pop();
        TreeMap<Message, Message> message = getMessageTree(messages);

        Set<Map.Entry<Message, Message>> set = message.entrySet();

        for (Map.Entry<Message, Message> entry : set) {
            Message me = entry.getKey();
            if (Long.parseLong(me.getProperty("next_time")) - System.currentTimeMillis() < 1000) {
                int result = messageHandler.handle(me);
                if (result > 0) {
                    this.executorService.schedule(this, result, TimeUnit.MILLISECONDS);
                }
            } else {
                localMessageStore.stash(me);
            }
        }
    }

    private TreeMap<Message, Message> getMessageTree(Message[] messages) {
        TreeMap<Message, Message> messageTreeMap = new TreeMap<Message, Message>(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if (Long.parseLong(o1.getProperty("next_time")) < Long.parseLong(o2.getProperty("next_time")))
                    return -1;
                return 1;
            }
        });
        for (final Message me : messages) {
            messageTreeMap.put(me, me);
        }
        return messageTreeMap;
    }
}
