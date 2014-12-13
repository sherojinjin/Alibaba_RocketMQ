package com.alibaba.rocketmq.client.producer.concurrent;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.alibaba.rocketmq.common.message.Message;

public class DefaultLocalMessageStoreTest {

  private DefaultLocalMessageStore defaultLocalMessageStore = new DefaultLocalMessageStore(
      "PG_Test");

  @Test
  public void testStash() throws InterruptedException {
    int number = 100;
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

  @Test
  public void testPop() {
    Message[] messages = defaultLocalMessageStore.pop();
  }

}
