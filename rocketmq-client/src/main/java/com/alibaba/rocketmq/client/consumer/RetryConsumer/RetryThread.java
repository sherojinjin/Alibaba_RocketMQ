package com.alibaba.rocketmq.client.consumer.RetryConsumer;

import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.Message;

/**
 * 定时轮询异常消息，将第一次失败的消息再次消费，若失败则继续保存
 *
 * @author robert
 * @since 2014-12-03
 */
public class RetryThread extends Thread {
//    private final Logger log = ClientLogger.getLog();

    private static final long SLEEP_TIME = 30000;

    private DefaultLocalMessageStore dms;

    private MessageHandler messageHandler;

    public RetryThread(DefaultLocalMessageStore dms, MessageHandler messageHandler) {
        this.dms = dms;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        System.out.println(" retry thread started ");

        while (true) {
            tryToSleep();
            System.err.println(" ******************* ");
            Message[] messages = dms.pop();
            if (null == messages || messages.length < 1)
                continue;

            for (Message me : messages) {
                int result = messageHandler.handle(me);
                if (result != 0) {
                    System.out.println(Thread.currentThread().getName() + " ****** Receive New Messages: " + me);
                    dms.stash(new Message(me.getTopic(), me.getTags(), me.getKeys(), me.getBody()));
                }
            }
        }
    }

    private void tryToSleep() {
        try {
            sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            System.err.println(" retry thread try to sleep failed.");
        }
    }
}
