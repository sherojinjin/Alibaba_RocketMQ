package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.client.ClientStatus;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CacheableConsumer {
    private static final Logger LOGGER = ClientLogger.getLog();

    private String consumerGroupName;

    protected DefaultLocalMessageStore localMessageStore;

    private final ConcurrentHashMap<String, MessageHandler> topicHandlerMap;

    private static final AtomicLong CONSUMER_NAME_COUNTER = new AtomicLong();

    private static final String BASE_INSTANCE_NAME = "CacheableConsumer@";

    private static final int NUMBER_OF_CONSUMER = 4;

    private List<DefaultMQPushConsumer> defaultMQPushConsumers = new ArrayList<DefaultMQPushConsumer>();

    private ClientStatus status = ClientStatus.CREATED;

    private MessageModel messageModel = MessageModel.CLUSTERING;

    private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

    private static final int CORE_POOL_SIZE_FOR_WORK_TASKS = 10;

    private static final int MAXIMUM_POOL_SIZE_FOR_WORK_TASKS = 50;

    private static final int DEFAULT_PULL_BATCH_SIZE = 256;

    private int pullBatchSize = DEFAULT_PULL_BATCH_SIZE;

    private static final int DEFAULT_CONSUME_MESSAGE_MAX_BATCH_SIZE = 1;

    private int consumeMessageMaxBatchSize = DEFAULT_CONSUME_MESSAGE_MAX_BATCH_SIZE;

    private int corePoolSizeForWorkTasks = CORE_POOL_SIZE_FOR_WORK_TASKS;

    private int maximumPoolSizeForWorkTasks = MAXIMUM_POOL_SIZE_FOR_WORK_TASKS;

    private ScheduledExecutorService scheduledExecutorDelayService = Executors.newSingleThreadScheduledExecutor();

    private ThreadPoolExecutor executorWorkerService;

    private FrontController frontController;

    private static final int MAXIMUM_NUMBER_OF_MESSAGE_BUFFERED = 20000;

    private LinkedBlockingQueue<MessageExt> messageQueue;

    private LinkedBlockingQueue<MessageExt> inProgressMessageQueue;

    private static String getInstanceName() {
        return BASE_INSTANCE_NAME + RemotingUtil.getLocalAddress(false) + "_" + CONSUMER_NAME_COUNTER.incrementAndGet();
    }

    /**
     * Constructor with consumer group name.
     * @param consumerGroupName consumer group name.
     */
    public CacheableConsumer(String consumerGroupName) {
        try {
            if (null == consumerGroupName || consumerGroupName.trim().isEmpty()) {
                throw new RuntimeException("ConsumerGroupName cannot be null or empty.");
            }

            this.consumerGroupName = consumerGroupName;
            this.topicHandlerMap = new ConcurrentHashMap<String, MessageHandler>();

            for (int i = 0; i < NUMBER_OF_CONSUMER; i++) {
                DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroupName);
                defaultMQPushConsumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueByDataCenter(defaultMQPushConsumer));
                defaultMQPushConsumer.setInstanceName(getInstanceName());
                defaultMQPushConsumer.setMessageModel(messageModel);
                defaultMQPushConsumer.setConsumeFromWhere(consumeFromWhere);
                defaultMQPushConsumer.setPullBatchSize(DEFAULT_PULL_BATCH_SIZE);
                defaultMQPushConsumer.setConsumeMessageBatchMaxSize(DEFAULT_CONSUME_MESSAGE_MAX_BATCH_SIZE);
                defaultMQPushConsumers.add(defaultMQPushConsumer);
            }

            executorWorkerService = new ThreadPoolExecutor(
                    corePoolSizeForWorkTasks,
                    maximumPoolSizeForWorkTasks,
                    0,
                    TimeUnit.NANOSECONDS,
                    new LinkedBlockingQueue<Runnable>(maximumPoolSizeForWorkTasks),
                    new ThreadFactoryImpl("ConsumerWorkerThread"),
                    new ThreadPoolExecutor.CallerRunsPolicy());

            messageQueue = new LinkedBlockingQueue<MessageExt>(MAXIMUM_NUMBER_OF_MESSAGE_BUFFERED);
            inProgressMessageQueue = new LinkedBlockingQueue<MessageExt>(MAXIMUM_NUMBER_OF_MESSAGE_BUFFERED);
            frontController = new FrontController(topicHandlerMap, executorWorkerService, localMessageStore,
                    messageQueue, inProgressMessageQueue);
            localMessageStore = new DefaultLocalMessageStore(consumerGroupName);
        } catch (IOException e) {
            LOGGER.error("Fatal error", e);
            throw new RuntimeException("Fatal error while instantiating CacheableConsumer");
        }
    }

    public CacheableConsumer registerMessageHandler(MessageHandler messageHandler) throws MQClientException {
        if (status != ClientStatus.CREATED) {
            throw new IllegalStateException("Please register before start");
        }

        if (null == messageHandler.getTopic() || messageHandler.getTopic().trim().isEmpty()) {
            throw new RuntimeException("Topic cannot be null or empty");
        }

        topicHandlerMap.putIfAbsent(messageHandler.getTopic(), messageHandler);

        for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
            defaultMQPushConsumer.subscribe(messageHandler.getTopic(),
                    null != messageHandler.getTag() ? messageHandler.getTag() : "*");
        }

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

        //We may have only one embedded consumer for broadcasting scenario.
        if (MessageModel.BROADCASTING == messageModel) {
            int i = 0;
            DefaultMQPushConsumer defaultMQPushConsumer = defaultMQPushConsumers.get(i);
            while (null == defaultMQPushConsumer && i < defaultMQPushConsumers.size()) {
                defaultMQPushConsumer = defaultMQPushConsumers.get(i++);
            }
            defaultMQPushConsumers.clear();
            defaultMQPushConsumers.add(defaultMQPushConsumer);
        }

        for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
            defaultMQPushConsumer.registerMessageListener(frontController);
            defaultMQPushConsumer.start();
        }
        startPopThread();
        addShutdownHook();
        status = ClientStatus.ACTIVE;
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
        return status == ClientStatus.ACTIVE;
    }

    public void setCorePoolSizeForWorkTasks(int corePoolSizeForWorkTasks) {
        this.corePoolSizeForWorkTasks = corePoolSizeForWorkTasks;
    }

    public void setMaximumPoolSizeForWorkTasks(int maximumPoolSizeForWorkTasks) {
        this.maximumPoolSizeForWorkTasks = maximumPoolSizeForWorkTasks;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;

        if (status != ClientStatus.CREATED) {
            throw new RuntimeException("Please set message model before start");
        }

        for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
            if (null != defaultMQPushConsumer) {
                defaultMQPushConsumer.setMessageModel(messageModel);
            }
        }
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;

        if (status != ClientStatus.CREATED) {
            throw new RuntimeException("Please set consume-from-where before start");
        }

        for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
            if (null != defaultMQPushConsumer) {
                defaultMQPushConsumer.setConsumeFromWhere(consumeFromWhere);
            }
        }
    }

    public void setConsumeMessageMaxBatchSize(int consumeMessageMaxBatchSize) {
        this.consumeMessageMaxBatchSize = consumeMessageMaxBatchSize;

        if (status != ClientStatus.CREATED) {
            throw new RuntimeException("Please set consumeMessageMaxBatchSize before start");
        }

        for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
            if (null != defaultMQPushConsumer) {
                defaultMQPushConsumer.setConsumeMessageBatchMaxSize(consumeMessageMaxBatchSize);
            }
        }
    }

    public int getConsumeMessageMaxBatchSize() {
        return consumeMessageMaxBatchSize;
    }

    public int getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(int pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
        if (status != ClientStatus.CREATED) {
            throw new RuntimeException("Please set pullBatchSize before start");
        }

        for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
            if (null != defaultMQPushConsumer) {
                defaultMQPushConsumer.setPullBatchSize(pullBatchSize);
            }
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
        LOGGER.info("Start to shutdown");
        status = ClientStatus.CLOSED;
        try {
            stopReceiving();
        } catch (InterruptedException e) {
            LOGGER.error("Failed to stop", e);
        }

        try {
            //Shut down local message store.
            if (null != localMessageStore) {
                localMessageStore.close();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Failed to stop", e);
        }
        LOGGER.info("Shutdown completes");
    }

    protected void stopReceiving() throws InterruptedException {
        if (status == ClientStatus.ACTIVE || status == ClientStatus.SUSPENDED) {
            //Stop pulling messages from broker server.
            for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
                defaultMQPushConsumer.shutdown();
            }

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

            if (inProgressMessageQueue.size() > 0) {
                MessageExt messageExt = inProgressMessageQueue.poll();
                while (null != messageExt) {
                    localMessageStore.stash(messageExt);
                    messageExt = inProgressMessageQueue.poll();
                }
            }

            status = ClientStatus.CLOSED;
            LOGGER.info("Local messages saving completes.");
        }
    }

    public void suspend() {
        if (ClientStatus.SUSPENDED == status) {
            return;
        }

        if (ClientStatus.ACTIVE == status) {
            for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
                defaultMQPushConsumer.suspend();
            }

            localMessageStore.suspend();
            status = ClientStatus.SUSPENDED;
        }
    }

    public void resume() {
        if (ClientStatus.SUSPENDED == status) {
            status = ClientStatus.ACTIVE;
            localMessageStore.resume();
            for (DefaultMQPushConsumer defaultMQPushConsumer : defaultMQPushConsumers) {
                defaultMQPushConsumer.resume();
            }
        }
    }
}
