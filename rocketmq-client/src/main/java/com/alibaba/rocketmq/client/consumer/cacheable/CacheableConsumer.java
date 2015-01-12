package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.rebalance.AllocateMessageQueueByDataCenter;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class CacheableConsumer {
    private static final Logger LOGGER = ClientLogger.getLog();

    private String consumerGroupName;

    protected DefaultLocalMessageStore localMessageStore;

    private final ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private static final AtomicLong CONSUMER_NAME_COUNTER = new AtomicLong();

    private static final String BASE_INSTANCE_NAME = "CacheableConsumer@";

    private DefaultMQPushConsumer defaultMQPushConsumer;

    private boolean started;

    private MessageModel messageModel = MessageModel.BROADCASTING;

    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    private static final int CORE_POOL_SIZE_FOR_WORK_TASKS = 10;

    private static final int MAXIMUM_POOL_SIZE_FOR_WORK_TASKS = 50;

    private int corePoolSizeForWorkTasks = CORE_POOL_SIZE_FOR_WORK_TASKS;

    private int maximumPoolSizeForWorkTasks = MAXIMUM_POOL_SIZE_FOR_WORK_TASKS;

    private ScheduledExecutorService scheduledExecutorDelayService = Executors.newSingleThreadScheduledExecutor();

    private ThreadPoolExecutor executorWorkerService;

    private FrontController frontController;

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
        defaultMQPushConsumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueByDataCenter(defaultMQPushConsumer));
        defaultMQPushConsumer.setInstanceName(getInstanceName());
        localMessageStore = new DefaultLocalMessageStore(consumerGroupName);
        defaultMQPushConsumer.setMessageModel(messageModel);
        defaultMQPushConsumer.setConsumeFromWhere(consumeFromWhere);
        executorWorkerService = new ThreadPoolExecutor(
                corePoolSizeForWorkTasks,
                maximumPoolSizeForWorkTasks,
                0,
                TimeUnit.NANOSECONDS,
                new LinkedBlockingQueue<Runnable>(maximumPoolSizeForWorkTasks),
                new ThreadFactoryImpl("ConsumerWorkerThread"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        frontController = new FrontController(topicHandlerMap, executorWorkerService, localMessageStore);
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

        defaultMQPushConsumer.registerMessageListener(frontController);
        defaultMQPushConsumer.start();

        startPopThread();

        started = true;

        addShutdownHook();

        LOGGER.debug("DefaultMQPushConsumer starts.");
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    LOGGER.info("Begin to shutdown CacheableConsumer");
                    shutdown();
                    LOGGER.info("CacheableConsumer shuts down successfully.");
                } catch (InterruptedException e) {
                    LOGGER.error("Exception thrown while invoking ShutdownHook", e);
                }
            }
        });
    }

    private void startPopThread() {
        DelayTask delayTask = new DelayTask(topicHandlerMap, localMessageStore, frontController.getMessageQueue());
        scheduledExecutorDelayService.scheduleWithFixedDelay(delayTask, 2, 2, TimeUnit.SECONDS);
    }

    public boolean isStarted() {
        return started;
    }

    public void setCorePoolSizeForWorkTasks(int corePoolSizeForWorkTasks) {
        this.corePoolSizeForWorkTasks = corePoolSizeForWorkTasks;
    }

    public void setMaximumPoolSizeForWorkTasks(int maximumPoolSizeForWorkTasks) {
        this.maximumPoolSizeForWorkTasks = maximumPoolSizeForWorkTasks;
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
        try {
            stopReceiving();
        } catch (InterruptedException e) {
            LOGGER.error("Failed to stop", e);
        }

        try {
            //Shut down local message store.
            if (null != localMessageStore && localMessageStore.isReady()) {
                localMessageStore.close();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Failed to stop", e);
        }
    }

    protected void stopReceiving() throws InterruptedException {
        if (started) {
            //Stop pulling messages from broker server.
            defaultMQPushConsumer.shutdown();

            //Stop popping messages from local message store.
            scheduledExecutorDelayService.shutdown();
            scheduledExecutorDelayService.awaitTermination(30000, TimeUnit.MILLISECONDS);

            //Stop consuming messages.
            executorWorkerService.shutdown();
            executorWorkerService.awaitTermination(30000, TimeUnit.MILLISECONDS);

            frontController.stopSubmittingJob();

            //Stash back all those that is not properly handled.
            LinkedBlockingQueue<MessageExt> messageQueue = frontController.getMessageQueue();
            LOGGER.info(messageQueue.size() + " messages to save into local message store due to system shutdown.");
            if (messageQueue.size() > 0) {
                MessageExt messageExt = messageQueue.poll();
                while (null != messageExt) {
                    localMessageStore.stash(messageExt);
                    messageExt = messageQueue.poll();
                }
            }
            LOGGER.info("Local message saving completes.");
            started = false;
        }
    }
}
