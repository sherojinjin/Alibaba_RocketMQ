package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;

import java.util.concurrent.*;

public class MultiThreadMQProducer {

    private static final Logger LOGGER = ClientLogger.getLog();

    private int concurrentSendBatchSize = 10;

    private final ThreadPoolExecutor threadPoolExecutor;

    private final ScheduledExecutorService resendFailureMessageService;

    private final DefaultMQProducer defaultMQProducer;

    private final SendCallback sendCallback;

    private final LocalMessageStore localMessageStore;

    public MultiThreadMQProducer(MultiThreadMQProducerConfiguration configuration) {

        if (null == configuration) {
            throw new IllegalArgumentException("MultiThreadMQProducerConfiguration cannot be null");
        }

        if (!configuration.isReadyToBuild()) {
            throw new IllegalArgumentException(configuration.reportMissingConfiguration());
        }

        this.concurrentSendBatchSize = configuration.getConcurrentSendBatchSize();

        threadPoolExecutor = new ScheduledThreadPoolExecutor(configuration.getCorePoolSize(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        resendFailureMessageService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl("ResendFailureMessageService"));

        defaultMQProducer = new DefaultMQProducer(configuration.getProducerGroup());

        //Configure default producer.
        defaultMQProducer.setDefaultTopicQueueNums(configuration.getDefaultTopicQueueNumber());
        defaultMQProducer.setRetryTimesWhenSendFailed(configuration.getRetryTimesBeforeSendingFailureClaimed());
        defaultMQProducer.setSendMsgTimeout(configuration.getSendMessageTimeOutInMilliSeconds());

        try {
            defaultMQProducer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("Unable to create producer instance", e);
        }

        sendCallback = configuration.getSendCallback();

        if (null == configuration.getLocalMessageStore()) {
            localMessageStore = new DefaultLocalMessageStore(configuration.getProducerGroup());
        } else {
            localMessageStore = configuration.getLocalMessageStore();
        }

        resendFailureMessageService.scheduleWithFixedDelay(new ResendMessageTask(localMessageStore, this), 0,
                configuration.getResendFailureMessageDelay(), TimeUnit.MILLISECONDS);
    }

    public void handleSendMessageFailure(Message msg, Throwable e) {
        LOGGER.error("Send message failed, enter resend later logic. Exception message: {}, caused by: {}",
                e.getMessage(), e.getCause().getMessage());
        localMessageStore.stash(msg);
    }


    public void send(final Message msg) {
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    defaultMQProducer.send(msg, new SendMessageCallback(MultiThreadMQProducer.this, sendCallback, msg));
                } catch (MQClientException e) {
                    handleSendMessageFailure(msg, e);
                } catch (RemotingException e) {
                    handleSendMessageFailure(msg, e);
                } catch (InterruptedException e) {
                    handleSendMessageFailure(msg, e);
                }

            }
        });
    }


    public void send(final Message[] messages) {

        if (null == messages || messages.length == 0) {
            return;
        }

        if (messages.length <= concurrentSendBatchSize) {
            threadPoolExecutor.submit(new BatchSendMessageTask(messages, sendCallback, this));
        } else {

            Message[] sendBatchArray = null;
            int remain = 0;
            for (int i = 0; i < messages.length; i += concurrentSendBatchSize) {
                sendBatchArray = new Message[concurrentSendBatchSize];
                remain = Math.min(concurrentSendBatchSize, messages.length - i);
                System.arraycopy(messages, i, sendBatchArray, 0, remain);
                threadPoolExecutor.submit(new BatchSendMessageTask(sendBatchArray, sendCallback, this));
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
        localMessageStore.close();

        getDefaultMQProducer().shutdown();
    }
}