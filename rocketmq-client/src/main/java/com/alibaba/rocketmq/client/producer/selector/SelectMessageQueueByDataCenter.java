/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.client.producer.selector;

import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 发送消息，根据数据中心选择队列
 */
public class SelectMessageQueueByDataCenter implements MessageQueueSelector {

    private static final Random RANDOM = new Random();

    private static final float SAME_DATA_CENTER_LOAD = 0.8F;

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final AtomicInteger ROUND_ROBIN_SAME_DATA_CENTER = new AtomicInteger(0);

    private static final AtomicInteger ROUND_ROBIN = new AtomicInteger(0);

    private static final String LOCAL_IP = RemotingUtil.getLocalAddress(false);

    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        DataCenter dataCenter = DataCenterLocator.locate(LOCAL_IP);

        List<MessageQueue> sameDataCenterQueues = new ArrayList<MessageQueue>();

        if (null != dataCenter) {
            for (MessageQueue messageQueue : mqs) {
                String[] brokerNameSegments = messageQueue.getBrokerName().split("_");
                if (brokerNameSegments[1].equalsIgnoreCase(dataCenter.getName())) {
                    sameDataCenterQueues.add(messageQueue);
                }
            }
        } else {
            for (MessageQueue messageQueue : mqs) {
                String[] brokerNameSegments = messageQueue.getBrokerName().split("_");
                String[] ipSegments = LOCAL_IP.split("\\.");
                if (brokerNameSegments[1].equals(ipSegments[1])) {
                    sameDataCenterQueues.add(messageQueue);
                }
            }

        }

        //Round robin.
        boolean chooseSameDataCenter = RANDOM.nextFloat() <= SAME_DATA_CENTER_LOAD;
        MessageQueue messageQueue = null;
        if (chooseSameDataCenter && !sameDataCenterQueues.isEmpty()) {
             messageQueue = sameDataCenterQueues
                     .get(ROUND_ROBIN_SAME_DATA_CENTER.incrementAndGet() % sameDataCenterQueues.size());
        } else {
             messageQueue = mqs.get(ROUND_ROBIN.incrementAndGet() % mqs.size());
        }

        if ((ROUND_ROBIN_SAME_DATA_CENTER.longValue() + ROUND_ROBIN.longValue()) % 1000 == 0) {
            LOGGER.info("Choosing broker: " + messageQueue.getBrokerName());
        }

        return messageQueue;
    }
}
