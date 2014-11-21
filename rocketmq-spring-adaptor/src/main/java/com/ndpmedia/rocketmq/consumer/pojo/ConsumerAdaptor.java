package com.ndpmedia.rocketmq.consumer.pojo;

import com.alibaba.rocketmq.client.consumer.*;
import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;

import javax.annotation.PostConstruct;

public class ConsumerAdaptor {

    /**
     * Optional name server. If it's null, you need to configure name-server end-point.
     */
    private String nameServer;

    /**
     * Tags separated by "||". Default value is "*" which accepts all messages, without any filtering.
     */
    private String tags = "*";

    /**
     * Topic name to consume. It's required.
     */
    private String topic;

    /**
     * Consumer group name. It's required.
     */
    private String consumerGroup;

    /**
     * Starting point of message consuming when the app starts at the first time.
     */
    private ConsumeFromWhere consumeFrom = ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET;

    /**
     * Instance of {@link MessageListener}, which contains main logic of processing message.
     * It's required when using push style client.
     */
    private MessageListener messageListener;

    /**
     * Message consuming model, cluster or broadcasting. Default value: cluster.
     */
    private MessageModel messageModel = MessageModel.CLUSTERING;

    /**
     * Indicate if this consumer has started.
     */
    private volatile boolean started;

    /**
     * Push consumer client. Considering push has obvious advantage over pull, we only supports push here.
     */
    private DefaultMQPushConsumer consumer = null;

    /**
     * Post construct checking method.
     */
    @PostConstruct
    public void init() {
        checkNotNull("consumerGroup", consumerGroup);
        checkNotNull("topic", topic);
        checkNotNull("messageListener", messageListener);
    }

    /**
     * Start to process messages.
     * @throws MQClientException
     */
    public void start() throws MQClientException {
        if (!started) {
            consumer = new DefaultMQPushConsumer(consumerGroup);
            //Message consuming model, cluster or broadcasting.
            consumer.setMessageModel(messageModel);
            //set optional name server.
            if (null != nameServer && !nameServer.isEmpty()) {
                consumer.setNamesrvAddr(nameServer);
            }

            consumer.subscribe(topic, tags);
            consumer.registerMessageListener(messageListener);
            consumer.start();

            started = true;
        }
    }

    /**
     * Stop the client.
     */
    public void stop() {
        if (started) {
            consumer.shutdown();
            started = false;
        }
    }

    private static void checkNotNull(String fieldName, Object variable) {
        if (null == variable) {
            throw new ConfigurationException("Variable " + fieldName + " cannot be null");
        }
    }

    public String getNameServer() {
        return nameServer;
    }

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public ConsumeFromWhere getConsumeFrom() {
        return consumeFrom;
    }

    public void setConsumeFrom(ConsumeFromWhere consumeFrom) {
        this.consumeFrom = consumeFrom;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }
}
