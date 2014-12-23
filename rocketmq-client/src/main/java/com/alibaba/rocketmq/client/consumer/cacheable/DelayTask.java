package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DelayTask implements Runnable {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int BATCH_SIZE = 1000;

    private static final String NEXT_TIME_KEY = "next_time";

    private static final String MESSAGE_ID_KEY = "msgId";

    private static final int TOL = 1000;

    private ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    public DelayTask(ConcurrentHashMap<String, MessageHandler> topicHandlerMap,
                     DefaultLocalMessageStore localMessageStore,
                     LinkedBlockingQueue<MessageExt> messageQueue
    ) {
        this.localMessageStore = localMessageStore;
        this.topicHandlerMap = topicHandlerMap;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Start re-consume messages");

            if (messageQueue.remainingCapacity() == 0) {
                LOGGER.info("Message queue is full. Won't fetch message from local message store.");
                return;
            }

            boolean isMessageQueueFull = false;
            Message[] messages = localMessageStore.pop(BATCH_SIZE);
            while (messages != null && messages.length > 0) {
                //TODO:Sorting here does not make sense, remove it next time.
                TreeMap<String, MessageExt> messageExtMap = getMessageTree(messages);
                long current = System.currentTimeMillis();
                for (Message message : messages) {
                    if (null == message) {
                        continue;
                    }
                    MessageExt messageExt = messageExtMap.get(message.getProperty(MESSAGE_ID_KEY));
                    if (Long.parseLong(message.getProperty(NEXT_TIME_KEY)) - current < TOL) { //It's time to process.
                        MessageHandler messageHandler = topicHandlerMap.get(messageExt.getTopic());
                        if (null == messageHandler) {
                            // On restart, fewer message handler may be registered so some messages stored locally
                            // may not get processed. We should warn this.
                            localMessageStore.stash(message);
                            continue;
                        }

                        if (messageQueue.remainingCapacity() > 0) {
                            //JobSubmitter will wrap the message into ProcessMessageTask and executorWorkerService will
                            //deliver this message to business client.
                            messageQueue.put(messageExt);
                        } else {
                            //Mark the messageQueue full and stash the message.
                            isMessageQueueFull = true;
                            localMessageStore.stash(message);
                        }

                    } else {
                        //It's too early to process, stash the message again and wait for next round.
                        localMessageStore.stash(message);
                    }
                }

                if (!isMessageQueueFull) {
                    //Pop more messages from local message store.
                    messages = localMessageStore.pop(BATCH_SIZE);
                } else {
                    //As messageQueue is full, we need to break the loop of popping message from local message store.
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("DelayTask error", e);
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
