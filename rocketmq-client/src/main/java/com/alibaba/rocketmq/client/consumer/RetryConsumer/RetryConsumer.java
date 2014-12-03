package com.alibaba.rocketmq.client.consumer.RetryConsumer;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.concurrent.DefaultLocalMessageStore;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 消费，异常消息会记录费，
 *
 * @author robert
 * @since 2014-12-03
 */
public class RetryConsumer {
    private String groupName = "robert";

    private String topic = "TopicTest";

    private String tags = "*";

    private DefaultLocalMessageStore dms = new DefaultLocalMessageStore(groupName);

    private MessageHandler messageHandler;

    public RetryConsumer(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void start() throws InterruptedException, MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setInstanceName(groupName);
        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST);

        consumer.subscribe(topic, tags);

        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                boolean flag = false;

                for (Message me : msgs) {
                    int result = messageHandler.handle(me);
                    if (result != 0) {
//                        System.out.println(Thread.currentThread().getName() + " +++++ Receive New Messages: " + me);
                        dms.stash(new Message(me.getTopic(), me.getTags(), me.getKeys(), me.getBody()));
                        if (!flag)
                            flag = true;
                    }
                }
                if (flag)
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        RetryThread rt = new RetryThread(dms, messageHandler);
        rt.start();

        System.out.println("Consumer Started.");

    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
        dms = new DefaultLocalMessageStore(groupName);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }
}
