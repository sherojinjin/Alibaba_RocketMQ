package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.rocketmq.common.message.Message;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DefaultLocalMessageStoreTest {

    private boolean stop = false;

    private static DefaultLocalMessageStore defaultLocalMessageStore = new DefaultLocalMessageStore("PG_Test");

    @Test
    public void testStash() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
        }

        defaultLocalMessageStore.close();
    }

    @Test
    public void testStashBulk() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5001; j++) {
                defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
            }
        }
        defaultLocalMessageStore.close();
    }

    @Test
    public void testPop() throws InterruptedException {
        Message[] messages = defaultLocalMessageStore.pop(2);
        while (messages.length > 0) {
            for (Message msg : messages) {
                System.out.println(msg.getTopic());
            }
            messages = defaultLocalMessageStore.pop(2);
        }

        defaultLocalMessageStore.close();
    }

    @Test
    public void testStressStash() throws InterruptedException {
        int number = 8;
        ExecutorService service = Executors.newFixedThreadPool(number);
        final CountDownLatch l = new CountDownLatch(number);
        final Random random = new Random();

        for (int i = 0; i < number - 2; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    l.countDown();
                    try {
                        l.await();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        while (!stop) {
                            if (random.nextBoolean()) {
                                Thread.sleep(100);
                            } else {
                                for (int i = 0; i < random.nextInt(50); i++) {
                                    byte[] bs = new byte[1024];
                                    Arrays.fill(bs, (byte) 'a');
                                    defaultLocalMessageStore.stash(new Message("Topic", bs));
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            });
        }

        for (int i = 0; i < 2; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    l.countDown();

                    try {
                        l.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    int badLoop = 0;
                    while (!stop) {
                        Message[] messages = defaultLocalMessageStore.pop(200);
                        if (messages == null || messages.length == 0) {
                            if (++badLoop > 100) {
                                System.out.println("Too many bad loops, going to exit.");
                                stop = true;
                                break;
                            }
                            try {
                                System.out.println("Empty loop. Going to sleep 1000ms.");
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            while (messages != null && messages.length > 0) {
                                System.out.println(Thread.currentThread().getName() + ": Popped " + messages.length);
                                messages = defaultLocalMessageStore.pop(200);
                            }
                        }
                    }
                }
            });
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        defaultLocalMessageStore.close();
    }


    @Test
    public void testStash2() throws InterruptedException {
        int number = 200;
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
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    while (true) {
                        try {
                            for (int i = 0; i < 3000; i++) {
                                byte[] bs = new byte[1024];
                                Arrays.fill(bs, (byte) 'a');
                                defaultLocalMessageStore.stash(new Message("Topic", bs));
                            }

                            Thread.sleep((long) (10 * r.nextFloat()));
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }


                }

            });
        }
        Thread.sleep(99999999);
        defaultLocalMessageStore.close();
    }
}