package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;
import org.junit.Assert;
import org.junit.Test;

public class DefaultLocalMessageStoreTest {

    private DefaultLocalMessageStore defaultLocalMessageStore = new DefaultLocalMessageStore("PG_Test");

    @Test
    public void testStash() {
        defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
    }

    @Test
    public void testPop() {
        Message[] messages = defaultLocalMessageStore.pop();
        Assert.assertNotNull(messages);
    }

}