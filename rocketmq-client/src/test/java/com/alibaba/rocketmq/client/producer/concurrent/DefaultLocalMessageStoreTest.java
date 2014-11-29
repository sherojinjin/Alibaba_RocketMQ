package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;
import org.junit.Test;

public class DefaultLocalMessageStoreTest {

    private DefaultLocalMessageStore defaultLocalMessageStore = new DefaultLocalMessageStore("PG_Test");


    @Test
    public void testStash() {
        defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
    }

}