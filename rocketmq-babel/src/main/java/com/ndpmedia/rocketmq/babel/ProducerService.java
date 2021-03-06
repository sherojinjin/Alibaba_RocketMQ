package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.producer.concurrent.MultiThreadMQProducer;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

public class ProducerService implements Producer.Iface {

    private MultiThreadMQProducer producer;

    public ProducerService() {
        producer = MultiThreadMQProducer.configure()
                .configureProducerGroup(Helper.getConfig().getProperty("producer_group")).build();

    }

    private com.alibaba.rocketmq.common.message.Message wrap(Message message) {
        com.alibaba.rocketmq.common.message.Message msg = new com.alibaba.rocketmq.common.message.Message();
        msg.setTopic(message.getTopic());
        msg.setBody(message.getData());
        msg.setFlag(message.getFlag());

        if (null != message.getProperties()) {
            for (Map.Entry<String, String> entry : message.getProperties().entrySet()) {
                msg.putUserProperty(entry.getKey(), entry.getValue());
            }
        }

        return msg;
    }

    @Override
    public void send(Message message) throws TException {
        producer.send(wrap(message));
    }

    @Override
    public void batchSend(List<Message> messageList) throws TException {
        com.alibaba.rocketmq.common.message.Message[] messages =
                new com.alibaba.rocketmq.common.message.Message[messageList.size()];
        int count = 0;
        for (Message msg : messageList) {
            messages[count++] = wrap(msg);
        }

        producer.send(messages);
    }

    @Override
    public void stop() throws TException {
        try {
            producer.shutdown();
        } catch (InterruptedException e) {
            throw new TException("Failed to stop", e);
        }
    }
}
