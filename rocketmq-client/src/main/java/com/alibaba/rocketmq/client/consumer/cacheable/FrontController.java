package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class FrontController implements MessageListenerConcurrently {

    private static final Logger LOGGER = ClientLogger.getLog();

    private final ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private final ThreadPoolExecutor executorWorkerService;

    private DefaultLocalMessageStore localMessageStore;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    private static final int MAXIMUM_NUMBER_OF_MESSAGE_BUFFERED = 20000;

    private JobSubmitter jobSubmitter = null;

    public FrontController(final ConcurrentHashMap<String, MessageHandler> topicHandlerMap,
                           final ThreadPoolExecutor executorWorkerService,
                           final DefaultLocalMessageStore localMessageStore) {
        this.topicHandlerMap = topicHandlerMap;
        this.executorWorkerService = executorWorkerService;
        this.localMessageStore = localMessageStore;
        messageQueue = new LinkedBlockingQueue<MessageExt>(MAXIMUM_NUMBER_OF_MESSAGE_BUFFERED);

        jobSubmitter = new JobSubmitter();

        Thread jobSubmitterThread = new Thread(jobSubmitter);
        jobSubmitterThread.setName("JobSubmitter");
        jobSubmitterThread.start();
    }


    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                    ConsumeConcurrentlyContext context) {
        for (MessageExt message : messages) {
            try {
                if (messageQueue.remainingCapacity() > 0) {
                    messageQueue.put(message);
                } else {
                    localMessageStore.stash(message);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to put message into message queue", e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    public LinkedBlockingQueue<MessageExt> getMessageQueue() {
        return messageQueue;
    }

    public void stopSubmittingJob() {
        jobSubmitter.stop();
    }

    class JobSubmitter implements Runnable {
        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                try {
                    MessageExt message = messageQueue.take();
                    final MessageHandler messageHandler = topicHandlerMap.get(message.getTopic());
                    ProcessMessageTask task =
                            new ProcessMessageTask(message, messageHandler, localMessageStore, messageQueue);
                    executorWorkerService.submit(task);
                } catch (Exception e) {
                    LOGGER.error("Error while submitting ProcessMessageTask", e);
                }
            }
        }

        public void stop() {
            running = false;
        }

    }
}

