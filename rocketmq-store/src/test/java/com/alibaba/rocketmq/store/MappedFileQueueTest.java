/**
 * $Id: MappedFileQueueTest.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package com.alibaba.rocketmq.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class MappedFileQueueTest {

    // private static final String StoreMessage =
    // "Once, there was a chance for me! but I did not treasure it. if";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    @Before
    public void setUp() throws Exception {
    }


    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void test_getLastMapedFile() {
        final String fixedMsg = "0123456789abcdef";
        System.out.println("================================================================");
        AllocateMapedFileService allocateMapedFileService = new AllocateMapedFileService();
        allocateMapedFileService.start();
        MapedFileQueue mapedFileQueue =
                new MapedFileQueue("./unit_test_store/a/", 1024, allocateMapedFileService);

        for (int i = 0; i < 1024; i++) {
            MappedFile mappedFile = mapedFileQueue.getLastMapedFile();
            assertTrue(mappedFile != null);
            boolean result = mappedFile.appendMessage(fixedMsg.getBytes());
            if (!result) {
                System.out.println("appendMessage " + i);
            }
            assertTrue(result);
        }

        mapedFileQueue.shutdown(1000);
        mapedFileQueue.destroy();
        allocateMapedFileService.shutdown();
        System.out.println("MapedFileQueue.getLastMapedFile() OK");
    }


    @Test
    public void test_findMapedFileByOffset() {
        final String fixedMsg = "abcd";
        System.out.println("================================================================");
        AllocateMapedFileService allocateMapedFileService = new AllocateMapedFileService();
        allocateMapedFileService.start();
        MapedFileQueue mapedFileQueue =
                new MapedFileQueue("./unit_test_store/b/", 1024, allocateMapedFileService);

        for (int i = 0; i < 1024; i++) {
            MappedFile mappedFile = mapedFileQueue.getLastMapedFile();
            assertTrue(mappedFile != null);
            boolean result = mappedFile.appendMessage(fixedMsg.getBytes());
            // System.out.println("appendMessage " + bytes);
            assertTrue(result);
        }

        MappedFile mappedFile = mapedFileQueue.findMapedFileByOffset(0);
        assertTrue(mappedFile != null);
        assertEquals(mappedFile.getFileFromOffset(), 0);
        System.out.println(mappedFile.getFileFromOffset());

        mappedFile = mapedFileQueue.findMapedFileByOffset(100);
        assertTrue(mappedFile != null);
        assertEquals(mappedFile.getFileFromOffset(), 0);
        System.out.println(mappedFile.getFileFromOffset());

        mappedFile = mapedFileQueue.findMapedFileByOffset(1024);
        assertTrue(mappedFile != null);
        assertEquals(mappedFile.getFileFromOffset(), 1024);
        System.out.println(mappedFile.getFileFromOffset());

        mappedFile = mapedFileQueue.findMapedFileByOffset(1024 + 100);
        assertTrue(mappedFile != null);
        assertEquals(mappedFile.getFileFromOffset(), 1024);
        System.out.println(mappedFile.getFileFromOffset());

        mappedFile = mapedFileQueue.findMapedFileByOffset(1024 * 2);
        assertTrue(mappedFile != null);
        assertEquals(mappedFile.getFileFromOffset(), 1024 * 2);
        System.out.println(mappedFile.getFileFromOffset());

        mappedFile = mapedFileQueue.findMapedFileByOffset(1024 * 2 + 100);
        assertTrue(mappedFile != null);
        assertEquals(mappedFile.getFileFromOffset(), 1024 * 2);
        System.out.println(mappedFile.getFileFromOffset());

        mappedFile = mapedFileQueue.findMapedFileByOffset(1024 * 4);
        assertTrue(mappedFile == null);

        mappedFile = mapedFileQueue.findMapedFileByOffset(1024 * 4 + 100);
        assertTrue(mappedFile == null);

        mapedFileQueue.shutdown(1000);
        mapedFileQueue.destroy();
        allocateMapedFileService.shutdown();
        System.out.println("MapedFileQueue.findMapedFileByOffset() OK");
    }


    @Test
    public void test_commit() {
        final String fixedMsg = "0123456789abcdef";
        System.out.println("================================================================");
        AllocateMapedFileService allocateMapedFileService = new AllocateMapedFileService();
        allocateMapedFileService.start();
        MapedFileQueue mapedFileQueue =
                new MapedFileQueue("./unit_test_store/c/", 1024, allocateMapedFileService);

        for (int i = 0; i < 1024; i++) {
            MappedFile mappedFile = mapedFileQueue.getLastMapedFile();
            assertTrue(mappedFile != null);
            boolean result = mappedFile.appendMessage(fixedMsg.getBytes());
            assertTrue(result);
        }

        // 不断尝试提交
        boolean result = mapedFileQueue.commit(0);
        assertFalse(result);
        assertEquals(1024 * 1, mapedFileQueue.getCommittedWhere());
        System.out.println("1 " + result + " " + mapedFileQueue.getCommittedWhere());

        result = mapedFileQueue.commit(0);
        assertFalse(result);
        assertEquals(1024 * 2, mapedFileQueue.getCommittedWhere());
        System.out.println("2 " + result + " " + mapedFileQueue.getCommittedWhere());

        result = mapedFileQueue.commit(0);
        assertFalse(result);
        assertEquals(1024 * 3, mapedFileQueue.getCommittedWhere());
        System.out.println("3 " + result + " " + mapedFileQueue.getCommittedWhere());

        result = mapedFileQueue.commit(0);
        assertFalse(result);
        assertEquals(1024 * 4, mapedFileQueue.getCommittedWhere());
        System.out.println("4 " + result + " " + mapedFileQueue.getCommittedWhere());

        result = mapedFileQueue.commit(0);
        assertFalse(result);
        assertEquals(1024 * 5, mapedFileQueue.getCommittedWhere());
        System.out.println("5 " + result + " " + mapedFileQueue.getCommittedWhere());

        result = mapedFileQueue.commit(0);
        assertFalse(result);
        assertEquals(1024 * 6, mapedFileQueue.getCommittedWhere());
        System.out.println("6 " + result + " " + mapedFileQueue.getCommittedWhere());

        mapedFileQueue.shutdown(1000);
        mapedFileQueue.destroy();
        allocateMapedFileService.shutdown();
        System.out.println("MapedFileQueue.commit() OK");
    }


    @Test
    public void test_getMapedMemorySize() {
        final String fixedMsg = "abcd";
        System.out.println("================================================================");
        AllocateMapedFileService allocateMapedFileService = new AllocateMapedFileService();
        allocateMapedFileService.start();
        MapedFileQueue mapedFileQueue =
                new MapedFileQueue("./unit_test_store/d/", 1024, allocateMapedFileService);

        for (int i = 0; i < 1024; i++) {
            MappedFile mappedFile = mapedFileQueue.getLastMapedFile();
            assertTrue(mappedFile != null);
            boolean result = mappedFile.appendMessage(fixedMsg.getBytes());
            assertTrue(result);
        }

        assertEquals(fixedMsg.length() * 1024, mapedFileQueue.getMapedMemorySize());

        mapedFileQueue.shutdown(1000);
        mapedFileQueue.destroy();
        allocateMapedFileService.shutdown();
        System.out.println("MapedFileQueue.getMapedMemorySize() OK");
    }

}
