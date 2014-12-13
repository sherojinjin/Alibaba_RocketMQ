package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.selector.SelectMessageQueueByDataCenter;
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

    private float officialTps = 0.0F;

    private float accumulativeTPSDelta = 0.0F;

    private int count;

    private AtomicLong numberOfMessageStashedDueToLackOfSemaphoreToken = new AtomicLong(0L);

    private MessageQueueSelector messageQueueSelector = new SelectMessageQueueByDataCenter();

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

    private void startMonitorTPS() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                float tps = (successSendingCounter.longValue() - lastSuccessfulSendingCount) * 1000.0F
                        / (System.currentTimeMillis() - lastStatsTimeStamp);
                count++;

                if (tps > officialTps + TPS_TOL || tps < officialTps - TPS_TOL) {
                    adjustThrottle(tps);
                } else {
                    accumulativeTPSDelta += tps - officialTps;
                    if (Math.abs(accumulativeTPSDelta) > TPS_TOL) {
                        adjustThrottle(tps);
                    }
                }

                lastStatsTimeStamp = System.currentTimeMillis();
                lastSuccessfulSendingCount = successSendingCounter.longValue();
            }

            private void adjustThrottle(float tps) {
                int updatedSemaphoreCapacity = 0;
                if (tps > officialTps) {
                    if (accumulativeTPSDelta > TPS_TOL) { //Update due to accumulative TPS delta surpass TPS_TOL
                        updatedSemaphoreCapacity = Math.min(semaphoreCapacity + (int)accumulativeTPSDelta / count,
                                MultiThreadMQProducerConfiguration.MAXIMUM_NUMBER_OF_MESSAGE_IN_MEMORY);
                    } else { //Update due to a specific second-average TPS > officialTPS + TPS_TOL
                        updatedSemaphoreCapacity = Math.min(semaphoreCapacity + (int)(tps - officialTps) + 1,
                                MultiThreadMQProducerConfiguration.MAXIMUM_NUMBER_OF_MESSAGE_IN_MEMORY);
                    }

                    if (updatedSemaphoreCapacity > semaphoreCapacity) {
                        semaphore.release(updatedSemaphoreCapacity - semaphoreCapacity);
                        semaphoreCapacity = updatedSemaphoreCapacity;
                    }
                } else {
                    if (-1 * accumulativeTPSDelta > TPS_TOL) { //Update due to accumulative TPS delta surpass TPS_TOL
                        updatedSemaphoreCapacity = Math.max(semaphoreCapacity + (int)accumulativeTPSDelta / count,
                                MultiThreadMQProducerConfiguration.MINIMUM_NUMBER_OF_MESSAGE_IN_MEMORY);
                    } else { //Update due to a specific second-average TPS < officialTPS - TPS_TOL
                        updatedSemaphoreCapacity = Math.max(semaphoreCapacity + (int)(tps - officialTps) - 1,
                                MultiThreadMQProducerConfiguration.MINIMUM_NUMBER_OF_MESSAGE_IN_MEMORY);
                    }

                    if (updatedSemaphoreCapacity < semaphoreCapacity) {
                        int delta = semaphoreCapacity - updatedSemaphoreCapacity;
                        semaphore.reducePermits(delta);
                        semaphoreCapacity = updatedSemaphoreCapacity;
                    }
                }

                //Update official TPS.
                officialTps = tps;

                //reset accumulative TPS delta.
                accumulativeTPSDelta = 0.0F;

                //reset count.
                count = 0;

                LOGGER.info("Semaphore capacity adjusted to:" + semaphoreCapacity);
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

        //Release assigned token.
        semaphore.release();

        localMessageStore.stash(msg);
    }

    public void send(final Message msg) {
        if (semaphore.tryAcquire()) { //Acquire a token from semaphore.
            sendMessagePoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        defaultMQProducer.send(msg, messageQueueSelector, null, new SendMessageCallback(MultiThreadMQProducer.this, sendCallback, msg));
                    } catch (Exception e) {
                        handleSendMessageFailure(msg, e);
                    }
                }
            });
            LOGGER.info("One message submitted to send.");
        } else {
            localMessageStore.stash(msg);
            LOGGER.warn("One message stashed");
        }
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
            if (!hasTokens) { //No tokens pre-assigned.
                if (semaphore.tryAcquire(messages.length)) { //Try to acquire tokens.
                    sendMessagePoolExecutor.submit(new BatchSendMessageTask(messages, sendCallback, this));
                    LOGGER.info(messages.length + " messages submitted to send.");
                } else { //Stash all these messages if no sufficient tokens are available.
                    for (Message message : messages) {
                        localMessageStore.stash(message);
                    }
                    LOGGER.warn(messages.length + " messages stashed.");
                }
            } else {
                //As these messages already got tokens, send them directly.
                sendMessagePoolExecutor.submit(new BatchSendMessageTask(messages, sendCallback, this));
                LOGGER.info(messages.length + " messages submitted to send.");
            }
        } else {
            Message[] sendBatchArray = null;
            int remain = 0;
            for (int i = 0; i < messages.length; i += remain) {
                remain = Math.min(concurrentSendBatchSize, messages.length - i);
                sendBatchArray = new Message[remain];
                System.arraycopy(messages, i, sendBatchArray, 0, remain);
                if (hasTokens) { //If messages have pre-assigned tokens, send them directly.
                    sendMessagePoolExecutor.submit(new BatchSendMessageTask(sendBatchArray, sendCallback, this));
                    LOGGER.info(sendBatchArray.length + " messages submitted to send.");
                } else if (semaphore.tryAcquire(sendBatchArray.length)) { //Try to acquire tokens and send them.
                    sendMessagePoolExecutor.submit(new BatchSendMessageTask(sendBatchArray, sendCallback, this));
                    LOGGER.info(sendBatchArray.length + " messages submitted to send.");
                } else { // Stash messages if no sufficient tokens available.
                    for (Message message : sendBatchArray) {
                        localMessageStore.stash(message);
                    }
                    LOGGER.warn(sendBatchArray.length + " messages stashed");
                }
            }
        }
    }

    public static MultiThreadMQProducerConfiguration configure() {
        return new MultiThreadMQProducerConfiguration();
    }

    public DefaultMQProducer getDefaultMQProducer() {
        return defaultMQProducer;
    }

    /**
     * This method properly shutdown this producer client.
     * @throws InterruptedException if unable to shutdown within 1 minute.
     */
    public void shutdown() throws InterruptedException {
        //No more messages from client or local message store.
        semaphore.drainPermits();

        //Stop thread which pops messages from local message store.
        resendFailureMessagePoolExecutor.shutdown();
        resendFailureMessagePoolExecutor.awaitTermination(30000, TimeUnit.MILLISECONDS);

        //Stop threads which send message to broker.
        sendMessagePoolExecutor.shutdown();
        sendMessagePoolExecutor.awaitTermination(30000, TimeUnit.MILLISECONDS);

        //Stop defaultMQProducer.
        getDefaultMQProducer().shutdown();

        //Refresh local message store configuration file.
        localMessageStore.close();
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

    public MessageQueueSelector getMessageQueueSelector() {
        return messageQueueSelector;
    }

    /**
     * This class is to expose reducePermits(int reduction) method publicly.
     */
    public static class CustomizableSemaphore extends Semaphore {
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