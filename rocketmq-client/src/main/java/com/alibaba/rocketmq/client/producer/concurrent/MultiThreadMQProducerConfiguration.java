package com.alibaba.rocketmq.client.producer.concurrent;

public class MultiThreadMQProducerConfiguration {

    private String producerGroup;

    private int corePoolSize = 10;

    private int defaultTopicQueueNumber = 4;

    private int retryTimesBeforeSendingFailureClaimed = 3;

    private int sendMessageTimeOutInMilliSeconds = 3000;

    private int concurrentSendBatchSize = 10;

    private int resendFailureMessageDelay = 5000;

    private LocalMessageStore localMessageStore;

    private int numberOfMessageInitiallyHeldImMemory = 5000;

    public static final int MAXIMUM_NUMBER_OF_MESSAGE_IN_MEMORY = 20000;

    public static final int MINIMUM_NUMBER_OF_MESSAGE_IN_MEMORY = 1000;

    public MultiThreadMQProducerConfiguration configureProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureDefaultTopicQueueNumber(int defaultTopicQueueNumber) {
        this.defaultTopicQueueNumber = defaultTopicQueueNumber;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureRetryTimesBeforeSendingFailureClaimed(int retryTimesBeforeSendingFailureClaimed) {
        this.retryTimesBeforeSendingFailureClaimed = retryTimesBeforeSendingFailureClaimed;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureSendMessageTimeOutInMilliSeconds(int sendMessageTimeOutInMilliSeconds) {
        this.sendMessageTimeOutInMilliSeconds = sendMessageTimeOutInMilliSeconds;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureConcurrentSendBatchSize(int concurrentSendBatchSize) {
        this.concurrentSendBatchSize = concurrentSendBatchSize;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureResendFailureMessageDelay(int resendFailureMessageDelay) {
        this.resendFailureMessageDelay = resendFailureMessageDelay;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureLocalMessageStore(LocalMessageStore localMessageStore) {
        this.localMessageStore = localMessageStore;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureNumberOfMessageInitiallyHeldImMemory(int numberOfMessageInitiallyHeldImMemory) {
        this.numberOfMessageInitiallyHeldImMemory = numberOfMessageInitiallyHeldImMemory;
        return this;
    }

    public MultiThreadMQProducer build() {
        return new MultiThreadMQProducer(this);
    }

    public boolean isReadyToBuild() {
        return null != producerGroup;
    }

    public String reportMissingConfiguration() {
        StringBuilder stringBuilder = null;

        if (null == producerGroup) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Producer Group required");
        }

        return stringBuilder != null ? stringBuilder.toString() : null;
    }


    public String getProducerGroup() {
        return producerGroup;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getDefaultTopicQueueNumber() {
        return defaultTopicQueueNumber;
    }

    public int getRetryTimesBeforeSendingFailureClaimed() {
        return retryTimesBeforeSendingFailureClaimed;
    }

    public int getSendMessageTimeOutInMilliSeconds() {
        return sendMessageTimeOutInMilliSeconds;
    }

    public int getConcurrentSendBatchSize() {
        return concurrentSendBatchSize;
    }

    public int getResendFailureMessageDelay() {
        return resendFailureMessageDelay;
    }

    public LocalMessageStore getLocalMessageStore() {
        return localMessageStore;
    }

    public int getNumberOfMessageInitiallyHeldImMemory() {
        return numberOfMessageInitiallyHeldImMemory;
    }
}
