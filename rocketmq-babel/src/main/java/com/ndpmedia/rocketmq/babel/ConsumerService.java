package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.consumer.cacheable.MessageHandler;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsumerService implements Consumer.AsyncIface {

    private CustomCacheableConsumer consumer;

    private String consumerGroup;

    private LinkedBlockingQueue<MessageExt> messageQueue = new LinkedBlockingQueue<MessageExt>(500);

    @Override
    public void setConsumerGroup(String consumerGroup, AsyncMethodCallback resultHandler) throws TException {
        this.consumerGroup = consumerGroup;
        resultHandler.onComplete("success");
    }

    private void checkInit() {
        if (null == consumer) {
            synchronized (ConsumerService.class) {
                if (null == consumer) {
                    consumer = new CustomCacheableConsumer(consumerGroup);
                }
            }
        }
    }

    @Override
    public void setMessageModel(MessageModel messageModel, AsyncMethodCallback resultHandler) throws TException {
        checkInit();

        switch (messageModel) {
            case CLUSTERING:
                consumer.setMessageModel(com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel.CLUSTERING);
                break;
            case BROADCASTING:
                consumer.setMessageModel(com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel.BROADCASTING);
                break;
        }
    }

    @Override
    public void registerTopic(String topic, String tag, AsyncMethodCallback resultHandler) throws TException {
        checkInit();

        MessageHandler messageHandler = new ThriftMessageHandler(this);
        messageHandler.setTopic(topic);
        messageHandler.setTag(tag); //TODO add tag.
        try {
            consumer.registerMessageHandler(messageHandler);
        } catch (MQClientException e) {
            resultHandler.onError(e);
        }
    }

    @Override
    public void start(AsyncMethodCallback resultHandler) throws TException {
        try {
            consumer.start();
        } catch (Exception e) {
            resultHandler.onError(e);
        }

    }

    @Override
    public void pull(AsyncMethodCallback resultHandler) throws TException {

        List<com.ndpmedia.rocketmq.babel.MessageExt> messageList = new ArrayList<com.ndpmedia.rocketmq.babel.MessageExt>();
        MessageExt msg = messageQueue.poll();
        while (null != msg) {
            messageList.add(wrap(msg));
            msg = messageQueue.poll();
        }

        resultHandler.onComplete(messageList);
    }

    private com.ndpmedia.rocketmq.babel.MessageExt wrap(MessageExt msg) {
        com.ndpmedia.rocketmq.babel.MessageExt message = new com.ndpmedia.rocketmq.babel.MessageExt();
        message.setTopic(msg.getTopic());
        message.setSysFlag(msg.getSysFlag());
        message.setFlag(msg.getFlag());
        message.setProperties(msg.getProperties());
        message.setQueueId(msg.getQueueId());
        message.setQueueOffset(msg.getQueueOffset());
        message.setStoreSize(msg.getStoreSize());
        message.setStoreTimestamp(msg.getStoreTimestamp());
        message.setBodyCRC(msg.getBodyCRC());
        message.setData(msg.getBody());
        message.setReconsumeTimes(msg.getReconsumeTimes());
        return message;
    }

    @Override
    public void stop(AsyncMethodCallback resultHandler) throws TException {
        try {
            consumer.stopReceiving();

            MessageExt msg = messageQueue.poll();
            while (null != msg) {
                consumer.getLocalMessageStore().stash(msg);
                msg = messageQueue.poll();
            }
            consumer.shutdown();
        } catch (InterruptedException e) {
            resultHandler.onError(e);
        }
    }
}

