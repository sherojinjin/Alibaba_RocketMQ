package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.consumer.cacheable.MessageHandler;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsumerService implements Consumer.AsyncIface {

    private CustomCacheableConsumer consumer;

    private String consumerGroup;

    private LinkedBlockingQueue<MessageExt> messageQueue = new LinkedBlockingQueue<MessageExt>(500);

    public ConsumerService() throws MQClientException, InterruptedException {
        Properties properties = Helper.getConfig();
        String consumerGroup = properties.getProperty("consumer_group");
        String topicInfo = properties.getProperty("topic_info");
        consumer = new CustomCacheableConsumer(consumerGroup);

        if(!"cluster".equals(properties.getProperty("message_model"))) {
            consumer.setMessageModel(com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel.BROADCASTING);
        }

        String[] topicList = topicInfo.split(";");
        for (String topicItem : topicList) {
            String[] topicAndTag = topicItem.split(",");
            if (topicAndTag.length != 2) {
                throw new RuntimeException("Configuration file format illegal. Please refer to sample_rocketmq_client_setting.properties file");
            }

            MessageHandler messageHandler = new ThriftMessageHandler(this);
            messageHandler.setTopic(topicAndTag[0]);
            messageHandler.setTag(topicAndTag[1]);
            consumer.registerMessageHandler(messageHandler);
        }

        consumer.start();
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
        message.setStoreHost(null == msg.getBornHost() ? null : msg.getBornHost().toString());
        message.setBornTimestamp(msg.getBornTimestamp());
        message.setBornHost(null == msg.getBornHost() ? null : msg.getBornHost().toString());
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

    public LinkedBlockingQueue<MessageExt> getMessageQueue() {
        return messageQueue;
    }
}

