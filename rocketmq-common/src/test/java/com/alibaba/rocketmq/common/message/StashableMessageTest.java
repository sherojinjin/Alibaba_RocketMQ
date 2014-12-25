package com.alibaba.rocketmq.common.message;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

public class StashableMessageTest {

    @Test
    public void testStashableToJSON() {

        StashableMessage message = new StashableMessage();
        message.setMsgId("abc");
        message.setQueueId(1);

        message.setSysFlag(0);
        message.setBody("abc".getBytes());

        String json = JSON.toJSONString(message);
        System.out.println(json);

        JSON.parseObject(json.getBytes(), StashableMessage.class);


    }

}