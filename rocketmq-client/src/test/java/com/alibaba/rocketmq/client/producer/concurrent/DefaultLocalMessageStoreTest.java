package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;
import org.junit.Test;

import java.util.Arrays;

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
        while (messages.length >0) {
            System.out.println(messages.length);
            messages = defaultLocalMessageStore.pop(200);
        }
    }


    private static Message[] buildMessages(int n, int size) {
        Message[] messages = new Message[n];

        byte[] data = new byte[size];
        Arrays.fill(data, (byte)'T');

        for (int i = 0; i < n; i++) {
            messages[i] = new Message("Topic", "Hello".getBytes());
        }

        return messages;
    }


    public static void main(String[] args) throws InterruptedException {

        for (Message message : buildMessages(10001, 1000)) {
            defaultLocalMessageStore.stash(message);
        }

        Message[] messages = defaultLocalMessageStore.pop(200);
        while (messages.length > 0) {
            System.out.println(messages.length);
            messages = defaultLocalMessageStore.pop(200);
        }

        defaultLocalMessageStore.close();

    }



}