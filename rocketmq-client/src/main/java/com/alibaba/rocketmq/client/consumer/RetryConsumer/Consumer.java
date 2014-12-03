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
package com.alibaba.rocketmq.client.consumer.RetryConsumer;


import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.Message;


/**
 * Consumer，订阅消息
 */
public class Consumer {
    public static void main(String[] args) throws InterruptedException, MQClientException {
        RetryConsumer rc = new RetryConsumer(new MessageHandler() {
            @Override
            public int handle(Message message) {
                if (message.getBody().length % 3 == 0)
                    return 0;
                return 1;
            }
        });
        rc.setGroupName("retryConsumer");
        rc.setTopic("TopicTest");
        rc.setTags("*");

        rc.start();
    }
}


