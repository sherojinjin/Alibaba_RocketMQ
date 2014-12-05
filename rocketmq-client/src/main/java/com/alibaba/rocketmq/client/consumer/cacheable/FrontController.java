package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class FrontController implements MessageListenerConcurrently {

    private final ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private final ScheduledExecutorService scheduledExecutorWorkerService;

    private final ScheduledExecutorService scheduledExecutorDelayService;

    private DefaultLocalMessageStore localMessageStore;

    public FrontController(ConcurrentHashMap<String, MessageHandler> topicHandlerMap,
                           ScheduledExecutorService scheduledExecutorWorkerService,
                           ScheduledExecutorService scheduledExecutorDelayService,
                           DefaultLocalMessageStore localMessageStore) {
        this.topicHandlerMap = topicHandlerMap;
        this.scheduledExecutorDelayService = scheduledExecutorDelayService;
        this.scheduledExecutorWorkerService = scheduledExecutorWorkerService;
        this.localMessageStore = localMessageStore;
    }


    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                    ConsumeConcurrentlyContext context) {
        for (MessageExt message : messages) {
            final MessageHandler messageHandler = topicHandlerMap.get(message.getTopic());
            scheduledExecutorWorkerService.submit(new ProcessMessageTask(message, messageHandler,
                    scheduledExecutorDelayService, localMessageStore));
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
