package com.alibaba.rocketmq.example.verify;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.Arrays;
import java.util.List;

/**
 * Tool to verify working of Topic and consumer group configurations on line.
 */
public class VerificationTool {

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
        }

        try {
            if ("send".equals(args[0])) {
                if (args.length > 2) {
                    printUsage();
                } else {
                    verifySend(args[1]);
                }
            } else if ("receive".equals(args[0])) {
                if (args.length < 3) {
                    printUsage();
                } else {
                    String[] topics = new String[args.length - 2];
                    System.arraycopy(args, 2, topics, 0, args.length - 2);
                    verifyReceive(args[1], topics);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: \n");
        System.out.println("1) Send: java VerificationTool send TOPIC_NAME");
        System.out.println("2) Receive: java VerificationTool receive CONSUMER_GROUP TOPIC_NAME_1 TOPIC_NAME_2 ... TOPIC_NAME_N");
    }

    private static void verifySend(String topic) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("PG_Verify");
        byte[] messageData = new byte[1024];
        Arrays.fill(messageData, (byte) 'T');
        Message message = new Message(topic, messageData);
        SendResult sendResult = producer.send(message);
        System.out.println(sendResult);
        producer.shutdown();
    }

    private static void verifyReceive(String consumerGroup, String[] topics) throws Exception {
        final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);

        for (String topic : topics) {
            consumer.subscribe(topic, "*");
        }

        consumer.setConsumeMessageBatchMaxSize(1);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.println(msg);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("Please wait 1 minute.");
        Thread.sleep(60 * 1000);
        consumer.shutdown();
    }
}
