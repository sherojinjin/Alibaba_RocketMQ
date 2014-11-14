/**
 * $Id: MappedFileTest.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


public class MappedFileTest {

    private static final String StoreMessage = "Once, there was a chance for me!";


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    @Test
    public void test_write_read() {
        try {
            MappedFile mappedFile = new MappedFile("./unit_test_store/MappedFileTest/000", 1024 * 64);
            boolean result = mappedFile.appendMessage(StoreMessage.getBytes());
            assertTrue(result);
            System.out.println("write OK");

            SelectMappedBufferResult selectMappedBufferResult = mappedFile.selectMappedBuffer(0);
            byte[] data = new byte[StoreMessage.length()];
            selectMappedBufferResult.getByteBuffer().get(data);
            String readString = new String(data);

            System.out.println("Read: " + readString);
            assertTrue(readString.equals(StoreMessage));

            // 禁止Buffer读写
            mappedFile.shutdown(1000);

            // mappedFile对象不可用
            assertTrue(!mappedFile.isAvailable());

            // 释放读到的Buffer
            selectMappedBufferResult.release();

            // 内存真正释放掉
            assertTrue(mappedFile.isCleanupOver());

            // 文件删除成功
            assertTrue(mappedFile.destroy(1000));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 当前测试用例由于对mmap操作错误，会导致JVM CRASHED
     */
    @Ignore
    public void test_jvm_crashed() {
        try {
            MappedFile mappedFile = new MappedFile("./unit_test_store/MappedFileTest/10086", 1024 * 64);
            boolean result = mappedFile.appendMessage(StoreMessage.getBytes());
            assertTrue(result);
            System.out.println("write OK");

            SelectMappedBufferResult selectMappedBufferResult = mappedFile.selectMappedBuffer(0);
            selectMappedBufferResult.release();
            mappedFile.shutdown(1000);

            byte[] data = new byte[StoreMessage.length()];
            selectMappedBufferResult.getByteBuffer().get(data);
            String readString = new String(data);
            System.out.println(readString);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
