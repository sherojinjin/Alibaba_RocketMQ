package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class CacheableConsumer
{
    private static final Logger LOGGER = ClientLogger.getLog();

    private String consumerGroupName;

    private DefaultLocalMessageStore localMessageStore;

    private final ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private static final AtomicLong CONSUMER_NAME_COUNTER = new AtomicLong();

    private static final String BASE_INSTANCE_NAME = "CacheableConsumer@";

    private DefaultMQPushConsumer defaultMQPushConsumer;

    private boolean started;

    private static final int CORE_POOL_SIZE_FOR_DELAY_TASKS = 2;

    private static final int CORE_POOL_SIZE_FOR_WORK_TASKS = 10;

    private int corePoolSizeForDelayTasks = CORE_POOL_SIZE_FOR_DELAY_TASKS;

    private int corePoolSizeForWorkTasks = CORE_POOL_SIZE_FOR_WORK_TASKS;

    private ScheduledExecutorService scheduledExecutorDelayService = Executors
            .newScheduledThreadPool(corePoolSizeForDelayTasks);

    private ScheduledExecutorService scheduledExecutorWorkerService = Executors
            .newScheduledThreadPool(corePoolSizeForWorkTasks);

    private static String getInstanceName() {
        try {
            return BASE_INSTANCE_NAME + InetAddress.getLocalHost().getHostAddress() + "_" + CONSUMER_NAME_COUNTER.incrementAndGet();
        } catch (UnknownHostException e) {
            return BASE_INSTANCE_NAME + "127.0.0.1_" + CONSUMER_NAME_COUNTER.incrementAndGet();
        }
    }

    public CacheableConsumer(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        this.topicHandlerMap = new ConcurrentHashMap<String, MessageHandler>();
        defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroupName);
        localMessageStore = new DefaultLocalMessageStore(consumerGroupName);
        defaultMQPushConsumer.setInstanceName(getInstanceName());
        defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
    }

    public CacheableConsumer registerMessageHandler(MessageHandler messageHandler) throws MQClientException {
        if (started) {
            throw new IllegalStateException("Please register before start");
        }

        if (null == messageHandler.getTopic() || messageHandler.getTopic().trim().isEmpty()) {
            throw new RuntimeException("Topic cannot be null or empty");
        }

        topicHandlerMap.putIfAbsent(messageHandler.getTopic(), messageHandler);
        defaultMQPushConsumer.subscribe(messageHandler.getTopic(),
                null != messageHandler.getTag() ? messageHandler.getTag() : "*");
        return this;
    }

    public CacheableConsumer registerMessageHandler(Collection<MessageHandler> messageHandlers)
            throws MQClientException {
        for (MessageHandler messageHandler : messageHandlers) {
            registerMessageHandler(messageHandler);
        }
        return this;
    }

    public void start() throws InterruptedException, MQClientException {
        if (topicHandlerMap.isEmpty()) {
            throw new RuntimeException("Please at least configure one message handler to subscribe one topic");
        }

        MessageListenerConcurrently frontController = new FrontController(topicHandlerMap,
                scheduledExecutorWorkerService, scheduledExecutorDelayService);
        defaultMQPushConsumer.registerMessageListener(frontController);
        defaultMQPushConsumer.start();
        started = true;
        LOGGER.debug("DefaultMQPushConsumer starts.");
    }

    public void setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
    }

    public boolean isStarted() {
        return started;
    }

    public void setCorePoolSizeForDelayTasks(int corePoolSizeForDelayTasks) {
        this.corePoolSizeForDelayTasks = corePoolSizeForDelayTasks;
    }

    public void setCorePoolSizeForWorkTasks(int corePoolSizeForWorkTasks) {
        this.corePoolSizeForWorkTasks = corePoolSizeForWorkTasks;
    }
}
