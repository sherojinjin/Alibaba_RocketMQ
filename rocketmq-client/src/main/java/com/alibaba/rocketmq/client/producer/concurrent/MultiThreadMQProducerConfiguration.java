package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.producer.SendCallback;

public class MultiThreadMQProducerConfiguration {

    private String producerGroup;

    private int corePoolSize = 20;

    private int maximumPoolSize = 200;

    private int defaultTopicQueueNumber = 4;

    private int retryTimesBeforeSendingFailureClaimed = 3;

    private int sendMessageTimeOutInMilliSeconds = 3000;

    private int concurrentSendBatchSize = 10;

    private SendCallback sendCallback;

    private int resendFailureMessageDelay = 30000;

    private LocalMessageStore localMessageStore;

    public MultiThreadMQProducerConfiguration configureProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public MultiThreadMQProducerConfiguration configureMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
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

    public MultiThreadMQProducerConfiguration configureSendCallback(SendCallback sendCallback) {
        this.sendCallback = sendCallback;
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

    public MultiThreadMQProducer build() {
        return new MultiThreadMQProducer(this);
    }

    public boolean isReadyToBuild() {
        return null != producerGroup && null != sendCallback;
    }

    public String reportMissingConfiguration() {
        StringBuilder stringBuilder = null;

        if (null == producerGroup) {
            stringBuilder = new StringBuilder();

            stringBuilder.append("Producer Group required");
        }

        if (null == sendCallback) {
            if (null == stringBuilder) {
                stringBuilder = new StringBuilder();
            }
            stringBuilder.append("SendCallback is required");
        }

        return stringBuilder.toString();
    }


    public String getProducerGroup() {
        return producerGroup;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
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

    public SendCallback getSendCallback() {
        return sendCallback;
    }

    public int getResendFailureMessageDelay() {
        return resendFailureMessageDelay;
    }

    public LocalMessageStore getLocalMessageStore() {
        return localMessageStore;
    }
}
