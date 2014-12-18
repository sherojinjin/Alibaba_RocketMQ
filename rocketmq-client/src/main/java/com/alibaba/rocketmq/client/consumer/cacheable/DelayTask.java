package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class DelayTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int BATCH_SIZE = 1000;

    private static final String NEXT_TIME_KEY = "next_time";

    private static final String MESSAGE_ID_KEY = "msgId";

    private static final int TOL = 1000;

    private ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    private final ThreadPoolExecutor executorWorkerService;

    public DelayTask(ConcurrentHashMap<String, MessageHandler> topicHandlerMap,
                     DefaultLocalMessageStore localMessageStore,
                     LinkedBlockingQueue<MessageExt> messageQueue,
                     ThreadPoolExecutor executorWorkerService
    ) {
        this.localMessageStore = localMessageStore;
        this.topicHandlerMap = topicHandlerMap;
        this.executorWorkerService = executorWorkerService;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        LOGGER.info("Start re-consume messages");
        Message[] messages = localMessageStore.pop(BATCH_SIZE);
        while (messages != null && messages.length > 0) {
            //TODO:Sorting here does not make sense, remove it next time.
            TreeMap<String, MessageExt> messageExtMap = getMessageTree(messages);
            for (Message message : messages) {
                if (null == message) {
                    continue;
                }
                MessageExt messageExt = messageExtMap.get(message.getProperty(MESSAGE_ID_KEY));
                if (Long.parseLong(message.getProperty(NEXT_TIME_KEY)) - System.currentTimeMillis() < TOL) {
                    MessageHandler messageHandler = topicHandlerMap.get(messageExt.getTopic());
                    if (null == messageHandler) {
                        //On restart, fewer message handler may be registered so some messages stored locally may not
                        // get processed. We should warn this.
                        localMessageStore.stash(message);
                        continue;
                    }
                    ProcessMessageTask task =
                            new ProcessMessageTask(messageExt, messageHandler, localMessageStore, messageQueue);
                    executorWorkerService.submit(task);
                } else {
                    localMessageStore.stash(message);
                }
            }
            messages = localMessageStore.pop(BATCH_SIZE);
        }
    }

    private TreeMap<String, MessageExt> getMessageTree(Message[] messages) {
        TreeMap<String, MessageExt> messageTreeMap = new TreeMap<String, MessageExt>();
        for (final Message message : messages) {
            if (null == message) {
                continue;
            }
            MessageExt me = TranslateMsg.getMessageExtFromMessage(message);
            me.putUserProperty(NEXT_TIME_KEY, message.getProperty(NEXT_TIME_KEY));
            messageTreeMap.put(me.getMsgId(), me);
        }

        return messageTreeMap;
    }
}
