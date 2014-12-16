/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.alibaba.rocketmq.client.producer.selector;

import com.alibaba.rocketmq.client.impl.MQClientAPIImpl;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.Pair;
import com.alibaba.rocketmq.common.ServiceState;
import com.alibaba.rocketmq.common.constant.NSConfigKey;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.body.KVTable;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 发送消息，根据数据中心选择队列
 */
public class SelectMessageQueueByDataCenter implements MessageQueueSelector {

    private static final Logger LOGGER = ClientLogger.getLog();

    private final Random random = new Random();

    private float locationRatio = 0.8f;

    private String dispatchStrategy = "BY_LOCATION";

    private final AtomicInteger roundRobin = new AtomicInteger(0);

    private static final String LOCAL_DATA_CENTER_ID = RemotingUtil.getLocalAddress(false).split("\\.")[1];

    private List<Pair<String, Float>> dispatcherList = new ArrayList<Pair<String, Float>>();

    private DefaultMQProducer defaultMQProducer;

    public SelectMessageQueueByDataCenter(DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
        startConfigUpdater();
    }

    private void startConfigUpdater() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (defaultMQProducer.getDefaultMQProducerImpl().getServiceState() != ServiceState.SHUTDOWN_ALREADY) {
                        try {
                            KVTable kvTable = getMQClientAPIImpl().getKVListByNamespace("DC_SELECTOR", 3000);
                            Map<String, String> configMap = kvTable.getTable();
                            String strategy = configMap.get(NSConfigKey.DC_DISPATCH_STRTEGY.getKey());


                            if ("BY_LOCATION".equals(strategy)) {
                                String location_ratio =
                                        configMap.get(NSConfigKey.DC_DISPATCH_STRTEGY_LOCATION_RATIO.getKey());
                                try {
                                    locationRatio = Float.parseFloat(location_ratio);
                                    dispatchStrategy = strategy;
                                } catch (Exception e) {
                                    LOGGER.warn("DC_DISPATCH_STRATEGY_LOCATION_RATIO parse error: {}", locationRatio);
                                }
                            } else if ("BY_RATIO".equals(strategy)) {

                                String dispatch_ratio = configMap.get(NSConfigKey.DC_DISPATCH_RATIO.getKey());
                                if (dispatch_ratio != null) {
                                    String[] values = dispatch_ratio.split(",");
                                    List<Pair<String, Float>> newList = new ArrayList<Pair<String, Float>>();
                                    for (String value : values) {
                                        String keyValue[] = value.split(":");
                                        if (keyValue.length != 2) {
                                            LOGGER.warn("DC_DISPATCH_RATIO parse error: {}", dispatch_ratio);
                                            continue;
                                        }
                                        Float floatValue = null;
                                        try {
                                            floatValue = Float.parseFloat(keyValue[1]);
                                        } catch (NumberFormatException e) {
                                            LOGGER.warn("DC_DISPATCH_RATIO parse error: {}", dispatch_ratio);
                                            continue;
                                        }

                                        newList.add(new Pair<String, Float>(keyValue[0], floatValue));
                                    }

                                    Collections.sort(newList, new Comparator<Pair<String, Float>>() {

                                        @Override
                                        public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                                            return o2.getObject2().compareTo(o1.getObject2());
                                        }
                                    });

                                    //Convert percent to percentile.
                                    if (newList.size() > 0) {
                                        for (int i = 1; i < newList.size() - 1; i++) {
                                            Pair<String, Float> pair = newList.get(i);
                                            pair.setObject2(pair.getObject2() + newList.get(i - 1).getObject2());
                                        }


                                        List<Pair<String, Float>> tmpList = getDispatcherList();
                                        setDispatcherList(newList);
                                        tmpList.clear();
                                        dispatchStrategy = strategy;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("get DC_SELECTOR params error", e);
                        }
                        //Sleep 60 seconds per loop.
                        Thread.sleep(60 * 1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "UpdateDCDispatchRatioThread-").start();

    }

    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        float r = random.nextFloat();
        List<MessageQueue> dateCenterQueues = new ArrayList<MessageQueue>();
        if ("BY_LOCATION".equals(dispatchStrategy)) {
            for (MessageQueue messageQueue : mqs) {
                String[] brokerNameSegments = messageQueue.getBrokerName().split("_");
                if (r > locationRatio && !brokerNameSegments[1].equals(LOCAL_DATA_CENTER_ID)) {
                    dateCenterQueues.add(messageQueue);
                } else if (r <= locationRatio && brokerNameSegments[1].equals(LOCAL_DATA_CENTER_ID)) {
                    dateCenterQueues.add(messageQueue);
                }
            }
        } else {
            List<Pair<String, Float>> list = getDispatcherList();
            String dc = list.get(0).getObject1();
            for (int i = 0; i < list.size() - 2; i++) {
                if (r > list.get(i).getObject2()) {
                    dc = list.get(i + 1).getObject1();
                    break;
                }
            }

            for (MessageQueue messageQueue : mqs) {
                String[] brokerNameSegments = messageQueue.getBrokerName().split("_");
                if (brokerNameSegments[1].equals(dc)) {
                    dateCenterQueues.add(messageQueue);
                }
            }
        }


        // Round robin.
        MessageQueue messageQueue = null;
        if (!dateCenterQueues.isEmpty()) {
            messageQueue = dateCenterQueues.get(roundRobin.incrementAndGet() % dateCenterQueues.size());
        } else {
            messageQueue = mqs.get(roundRobin.incrementAndGet() % mqs.size());
        }

        if ((roundRobin.longValue()) % 1000 == 0) {
            LOGGER.info("Choosing broker: " + messageQueue.getBrokerName());
        }

        return messageQueue;
    }

    public List<Pair<String, Float>> getDispatcherList() {
        return dispatcherList;
    }

    public void setDispatcherList(List<Pair<String, Float>> dispatcherList) {
        this.dispatcherList = dispatcherList;
    }

    private MQClientAPIImpl getMQClientAPIImpl() {
        return this.defaultMQProducer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl();
    }

}
