package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultLocalMessageStoreTest {

    private static DefaultLocalMessageStore defaultLocalMessageStore = new DefaultLocalMessageStore("PG_Test");

    @Test
    public void testStash() {
        for (int i = 0; i < 1; i++) {
            defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
        }
    }


    @Test
    public void testStashBulk() {
        for (int i = 0; i < 5001; i++) {
            defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
        }
    }

    @Test
    public void testPop() {
        Message[] messages = defaultLocalMessageStore.pop(1);
    }

    @Test
    public void testPopBulk() {

        Message[] messages = defaultLocalMessageStore.pop(200);
        while (messages.length > 0) {
            System.out.println(messages.length);
            messages = defaultLocalMessageStore.pop(200);
        }
    }

    @Test
    public void testStash2() throws InterruptedException {
        int number = 100;
        ExecutorService service = Executors.newFixedThreadPool(number);
        final CountDownLatch l = new CountDownLatch(number);
        final Random r = new Random();
        for (int i = 0; i < number; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    l.countDown();
                    try {
                        l.await();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    for (int i = 0; i < 3000; i++) {
                        byte[] bs = new byte[1024];
                        Arrays.fill(bs, (byte) 'a');
                        defaultLocalMessageStore.stash(new Message("Topic", bs));
                    }
                }

            });
        }

        Thread.sleep(99999999);

    }
}