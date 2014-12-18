package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.concurrent.LinkedBlockingQueue;

public class ProcessMessageTask implements Runnable {

    private static final String NEXT_TIME_KEY = "next_time";

    private MessageExt message;

    private MessageHandler messageHandler;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    public ProcessMessageTask(MessageExt message, //Message to process.
                              MessageHandler messageHandler, //Message handler.
                              DefaultLocalMessageStore localMessageStore, //Local message store.
                              LinkedBlockingQueue<MessageExt> messageQueue //Buffered message queue.
    ) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.localMessageStore = localMessageStore;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        int result = messageHandler.handle(message);

        //Remove the message from in-progress queue.
        if (null != messageQueue && messageQueue.contains(message)) {
            messageQueue.remove(message);
        }

        if (result > 0) {
            Message me = TranslateMsg.getMessageFromMessageExt(message);
            me.putUserProperty(NEXT_TIME_KEY, String.valueOf(System.currentTimeMillis() + result));
            localMessageStore.stash(me);
        }
    }

}
