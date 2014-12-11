package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class MultiThreadMQProducer {

    private static final Logger LOGGER = ClientLogger.getLog();

    private int concurrentSendBatchSize = 100;

    private static final int TPS_TOL = 100;

    private final ThreadPoolExecutor sendMessagePoolExecutor;

    private final ScheduledExecutorService resendFailureMessagePoolExecutor;

    private final DefaultMQProducer defaultMQProducer;

    private SendCallback sendCallback;

    private final LocalMessageStore localMessageStore;

    private volatile boolean started;

    private final CustomizableSemaphore semaphore;

    private int semaphoreCapacity;

    private final AtomicLong successSendingCounter = new AtomicLong(0L);

    private long lastSuccessfulSendingCount = 0L;

    private long lastStatsTimeStamp = System.currentTimeMillis();

    private float successTps = 0.0F;

    private AtomicLong numberOfMessageStashedDueToLackOfSemaphoreToken = new AtomicLong(0L);

    public MultiThreadMQProducer(MultiThreadMQProducerConfiguration configuration) {
        if (null == configuration) {
            throw new IllegalArgumentException("MultiThreadMQProducerConfiguration cannot be null");
        }

        if (!configuration.isReadyToBuild()) {
            throw new IllegalArgumentException(configuration.reportMissingConfiguration());
        }

        this.concurrentSendBatchSize = configuration.getConcurrentSendBatchSize();

        sendMessagePoolExecutor = new ScheduledThreadPoolExecutor(configuration.getCorePoolSize(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        resendFailureMessagePoolExecutor = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("ResendFailureMessageService"));

        semaphoreCapacity = configuration.getNumberOfMessageInitiallyHeldImMemory();

        semaphore = new CustomizableSemaphore(semaphoreCapacity, true);

        defaultMQProducer = new DefaultMQProducer(configuration.getProducerGroup());

        //Configure default producer.
        defaultMQProducer.setDefaultTopicQueueNums(configuration.getDefaultTopicQueueNumber());
        defaultMQProducer.setRetryTimesWhenSendFailed(configuration.getRetryTimesBeforeSendingFailureClaimed());
        defaultMQProducer.setSendMsgTimeout(configuration.getSendMessageTimeOutInMilliSeconds());

        try {
            defaultMQProducer.start();
            started = true;
        } catch (MQClientException e) {
            throw new RuntimeException("Unable to create producer instance", e);
        } finally {
            if (started) {
                LOGGER.debug("Client starts successfully");
            } else {
                LOGGER.error("Client starts with error.");
            }
        }

        if (null == configuration.getLocalMessageStore()) {
            localMessageStore = new DefaultLocalMessageStore(configuration.getProducerGroup());
        } else {
            localMessageStore = configuration.getLocalMessageStore();
        }

        startResendFailureMessageService(configuration.getResendFailureMessageDelay());

        startMonitorTPS();
    }

    private void startMonitorTPS() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                float tps = (successSendingCounter.longValue() - lastSuccessfulSendingCount) * 1000.0F
                        / (System.currentTimeMillis() - lastStatsTimeStamp);

                if (tps > successTps + TPS_TOL) {
                    int updatedSemaphoreCapacity = Math.min((int)(semaphoreCapacity*1.2),
                            MultiThreadMQProducerConfiguration.MAXIMUM_NUMBER_OF_MESSAGE_IN_MEMORY);
                    if (updatedSemaphoreCapacity > semaphoreCapacity) {
                        semaphore.release(updatedSemaphoreCapacity - semaphoreCapacity);
                        semaphoreCapacity = updatedSemaphoreCapacity;
                    }
                    successTps = tps;
                } else if (tps < successTps - TPS_TOL) {
                    int updatedSemaphoreCapacity = Math.max((int) (semaphoreCapacity * 0.8), MultiThreadMQProducerConfiguration.MINIMUM_NUMBER_OF_MESSAGE_IN_MEMORY);

                    if (updatedSemaphoreCapacity < semaphoreCapacity) {
                        int delta = semaphoreCapacity - updatedSemaphoreCapacity;
                        semaphore.reducePermits(delta);
                        semaphoreCapacity = updatedSemaphoreCapacity;
                    }
                    successTps = tps;
                }
                lastStatsTimeStamp = System.currentTimeMillis();
                lastSuccessfulSendingCount = successSendingCounter.longValue();
            }
        }, 3000, 1000, TimeUnit.MILLISECONDS);
    }

    public void startResendFailureMessageService(long interval) {
            resendFailureMessagePoolExecutor.scheduleWithFixedDelay(
                    new ResendMessageTask(localMessageStore, this), 3100, interval, TimeUnit.MILLISECONDS);
    }

    public void registerCallback(SendCallback sendCallback) {
        this.sendCallback = sendCallback;
    }

    public void handleSendMessageFailure(Message msg, Throwable e) {
        LOGGER.error("Send message failed. Enter re-send logic. Exception:", e);
        localMessageStore.stash(msg);
    }

    public void send(final Message msg) {
        sendMessagePoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    //Acquire a token from semaphore.
                    if (semaphore.tryAcquire()) {
                        defaultMQProducer.send(msg, new SendMessageCallback(MultiThreadMQProducer.this, sendCallback, msg));
                    } else {
                        //In case all tokens are taken.
                        localMessageStore.stash(msg);
                    }
                } catch (Exception e) {
                    handleSendMessageFailure(msg, e);
                }
            }
        });
    }


    /**
     * This method is assumed to be called by client user. Tokens will be assigned later on.
     * @param messages Messages to send.
     */
    public void send(final Message[] messages) {
        send(messages, false);
    }

    /**
     * This method would send message with or without token from semaphore. Ultimate client user is not supposed to use
     * this method unless you know what you are doing.
     *
     * @param messages Messages to send.
     * @param hasTokens If these messages have already been assigned with tokens: true for yes; false for no.
     */
    protected void send(final Message[] messages, boolean hasTokens) {
        if (null == messages || messages.length == 0) {
            return;
        }

        if (messages.length <= concurrentSendBatchSize) {
            sendMessagePoolExecutor.submit(new BatchSendMessageTask(messages, sendCallback, this, hasTokens));
        } else {
            Message[] sendBatchArray = null;
            int remain = 0;
            for (int i = 0; i < messages.length; i += concurrentSendBatchSize) {
                sendBatchArray = new Message[concurrentSendBatchSize];
                remain = Math.min(concurrentSendBatchSize, messages.length - i);
                System.arraycopy(messages, i, sendBatchArray, 0, remain);
                sendMessagePoolExecutor.submit(new BatchSendMessageTask(sendBatchArray, sendCallback, this, hasTokens));
            }
        }
    }

    public static MultiThreadMQProducerConfiguration configure() {
        return new MultiThreadMQProducerConfiguration();
    }

    public DefaultMQProducer getDefaultMQProducer() {
        return defaultMQProducer;
    }

    public void shutdown() {
        resendFailureMessagePoolExecutor.shutdown();
        sendMessagePoolExecutor.shutdown();
        localMessageStore.close();
        getDefaultMQProducer().shutdown();
    }

    public CustomizableSemaphore getSemaphore() {
        return semaphore;
    }

    public LocalMessageStore getLocalMessageStore() {
        return localMessageStore;
    }

    public AtomicLong getSuccessSendingCounter() {
        return successSendingCounter;
    }

    public AtomicLong getNumberOfMessageStashedDueToLackOfSemaphoreToken() {
        return numberOfMessageStashedDueToLackOfSemaphoreToken;
    }

    /**
     * This class is to expose reducePermits(int reduction) method publicly.
     */
    static class CustomizableSemaphore extends Semaphore {
        public CustomizableSemaphore(int permits) {
            super(permits);
        }

        public CustomizableSemaphore(int permits, boolean fair) {
            super(permits, fair);
        }

        /**
         * Override to expose this method publicly.
         * @param reduction amount of permits to reduce.
         */
        @Override
        public void reducePermits(int reduction) {
            super.reducePermits(reduction);
        }
    }
}