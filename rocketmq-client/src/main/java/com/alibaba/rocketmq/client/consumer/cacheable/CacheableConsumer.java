package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private MessageModel messageModel = MessageModel.BROADCASTING;

    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    private static final int CORE_POOL_SIZE_FOR_DELAY_TASKS = 2;

    private static final int CORE_POOL_SIZE_FOR_WORK_TASKS = 10;

    private int corePoolSizeForDelayTasks = CORE_POOL_SIZE_FOR_DELAY_TASKS;

    private int corePoolSizeForWorkTasks = CORE_POOL_SIZE_FOR_WORK_TASKS;

    private ScheduledExecutorService scheduledExecutorDelayService = Executors
            .newScheduledThreadPool(corePoolSizeForDelayTasks);

    private ScheduledExecutorService scheduledExecutorWorkerService = Executors
            .newScheduledThreadPool(corePoolSizeForWorkTasks);

    private static String getInstanceName() {
        return BASE_INSTANCE_NAME + RemotingUtil.getLocalAddress(false) + "_" +
                CONSUMER_NAME_COUNTER.incrementAndGet();
    }

    /**
     * Constructor with consumer group name.
     * @param consumerGroupName consumer group name.
     */
    public CacheableConsumer(String consumerGroupName) {

        if (null == consumerGroupName || consumerGroupName.trim().isEmpty()) {
            throw new RuntimeException("ConsumerGroupName cannot be null or empty.");
        }

        this.consumerGroupName = consumerGroupName;
        this.topicHandlerMap = new ConcurrentHashMap<String, MessageHandler>();
        defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroupName);
        defaultMQPushConsumer.setInstanceName(getInstanceName());
        localMessageStore = new DefaultLocalMessageStore(consumerGroupName);
        defaultMQPushConsumer.setMessageModel(messageModel);
        defaultMQPushConsumer.setConsumeFromWhere(consumeFromWhere);
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
                scheduledExecutorWorkerService, localMessageStore);
        defaultMQPushConsumer.registerMessageListener(frontController);
        defaultMQPushConsumer.start();

        startPopThread();

        started = true;

        LOGGER.debug("DefaultMQPushConsumer starts.");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startPopThread() {
        scheduledExecutorDelayService.scheduleWithFixedDelay(new DelayTask(topicHandlerMap, localMessageStore), 2, 2,
                TimeUnit.SECONDS);
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

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;

        if (started) {
            throw new RuntimeException("Please set message model before start");
        }

        if (null != defaultMQPushConsumer) {
            defaultMQPushConsumer.setMessageModel(messageModel);
        }
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;

        if (started) {
            throw new RuntimeException("Please set consume-from-where before start");
        }

        if (null != defaultMQPushConsumer) {
            defaultMQPushConsumer.setConsumeFromWhere(consumeFromWhere);
        }
    }

    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    /**
     * This method shuts down this client properly.
     * @throws InterruptedException If unable to shut down within 1 minute.
     */
    public void shutdown() throws InterruptedException {
        //Stop pulling messages from server.
        defaultMQPushConsumer.shutdown();

        //Stop popping messages from local message store.
        scheduledExecutorDelayService.shutdown();
        scheduledExecutorDelayService.awaitTermination(30000, TimeUnit.MILLISECONDS);

        //Stop consuming messages.
        scheduledExecutorWorkerService.shutdown();
        scheduledExecutorWorkerService.awaitTermination(30000, TimeUnit.MILLISECONDS);

        //Refresh local message store configuration file.
        localMessageStore.close();
    }
}
