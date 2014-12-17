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

    private static DefaultLocalMessageStore defaultLocalMessageStore = new DefaultLocalMessageStore("PG_Test");

    @Test
    public void testStash() throws InterruptedException {
        for (int i = 0; i < 1; i++) {
            defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
        }

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
        }

    }


    @Test
    public void testStashBulk() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 5001; j++) {
                defaultLocalMessageStore.stash(new Message("Topic", "Data".getBytes()));
            }
            Thread.sleep(1000);
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
    public void testStressStash() throws InterruptedException {
        int number = 8;
        ExecutorService service = Executors.newFixedThreadPool(number);
        final CountDownLatch l = new CountDownLatch(number);
        final Random r = new Random();
        for (int i = 0; i < number - 1; i++) {
            service.submit(new Runnable() {
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
                while (true) {
                    Message[] messages = defaultLocalMessageStore.pop(200);
                    if (messages == null || messages.length == 0) {
                        if(++badLoop > 100) {
                            System.out.println("Too many bad loops, going to exit.");
                            break;
                        }
                        try {
                            System.out.println("Empty loop. Going to sleep 100ms.");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        while (messages != null && messages.length > 0) {
                            System.out.println("Popped " + messages.length);
                            messages = defaultLocalMessageStore.pop(200);
                        }
                    }
                }


            }
        });

        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
    }


  @Test
  public void testStash2() throws InterruptedException {
    int number = 200;
    ExecutorService service = Executors.newFixedThreadPool(number);
    final CountDownLatch l = new CountDownLatch(number);
    final Random r = new Random();
    for(int i = 0;i<number;i++){
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
                Arrays.fill(bs, (byte)'a');
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
  }
}