package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultLocalMessageStore implements LocalMessageStore {

    private static final String DEFAULT_STORE_LOCATION = "/dianyi/data/";

    private static final String LOCAL_MESSAGE_STORE_FOLDER_NAME = ".localMessageStore";

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int MESSAGES_PER_FILE = 100000;

    private final AtomicLong writeIndex = new AtomicLong(0L);
    private final AtomicLong writeOffSet = new AtomicLong(0L);

    private final AtomicLong readIndex = new AtomicLong(0L);
    private final AtomicLong readOffSet = new AtomicLong(0L);

    private String storeLocation = System.getProperty("defaultLocalMessageStoreLocation",
            DEFAULT_STORE_LOCATION);

    private File localMessageStoreDirectory;

    private ConcurrentHashMap<Long, File> messageStoreNameFileMapping = new ConcurrentHashMap<Long, File>();

    private File configFile;

    private RandomAccessFile randomAccessFile;

    private ReentrantLock lock = new ReentrantLock();

    private LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>(50000);

    private ScheduledExecutorService flushConfigPeriodicallyByTimeExecutorService;

    private ScheduledExecutorService flushConfigPeriodicallyByMessageNumberExecutorService;

    private volatile boolean ready = false;

    private static final int MAXIMUM_NUMBER_OF_DIRTY_MESSAGE_IN_QUEUE = 1000;

    private static final float DISK_HIGH_WATER_LEVEL = 0.75F;

    private static final float DISK_WARNING_WATER_LEVEL = 0.65F;

    private volatile long lastFlushTime = -1;

    private volatile long lastWarnTime = -1;

    public DefaultLocalMessageStore(String storeName) {
        //For convenience of development.
        if (DEFAULT_STORE_LOCATION.equals(storeLocation)) {
            File defaultStoreLocation = new File(DEFAULT_STORE_LOCATION);
            if (!defaultStoreLocation.exists()) {
                storeLocation = System.getProperty("user.home") + File.separator + LOCAL_MESSAGE_STORE_FOLDER_NAME;
            } else {
                storeLocation = storeLocation.endsWith(File.separator)
                        ? storeLocation + LOCAL_MESSAGE_STORE_FOLDER_NAME
                        : storeLocation + File.separator + LOCAL_MESSAGE_STORE_FOLDER_NAME;
            }
        }

        localMessageStoreDirectory = new File(storeLocation, storeName);

        if (!localMessageStoreDirectory.exists()) {
            if (!localMessageStoreDirectory.mkdirs()) {
                throw new RuntimeException("Local message store directory does not exist and unable to create one");
            }
        }

        loadConfig();

        flushConfigPeriodicallyByTimeExecutorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryImpl("LocalMessageStoreFlushConfigServicePeriodicallyByTime"));

        flushConfigPeriodicallyByTimeExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        flushConfigPeriodicallyByMessageNumberExecutorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryImpl("LocalMessageStoreFlushConfigServicePeriodicallyByMessageNumber"));

        flushConfigPeriodicallyByMessageNumberExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (messageQueue.size() > MAXIMUM_NUMBER_OF_DIRTY_MESSAGE_IN_QUEUE) {
                    flush();
                }
            }
        }, 50, 100, TimeUnit.MILLISECONDS);

        ready = true;

        LOGGER.info("Local Message store starts to operate.");
    }

    /**
     * This method will execute on startup.
     */
    private void loadConfig() {
        configFile = new File(localMessageStoreDirectory, ".config");
        if (configFile.exists() && configFile.canRead()) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                Properties properties = new Properties();
                properties.load(inputStream);

                writeIndex.set(null == properties.getProperty("writeIndex") ? 0L :
                        Long.parseLong(properties.getProperty("writeIndex")));
                writeOffSet.set(null == properties.getProperty("writeOffSet") ? 0L :
                        Long.parseLong(properties.getProperty("writeOffSet")));
                readIndex.set(null == properties.getProperty("readIndex") ? 0L :
                        Long.parseLong(properties.getProperty("readIndex")));
                readOffSet.set(null == properties.getProperty("readOffSet") ? 0L :
                        Long.parseLong(properties.getProperty("readOffSet")));

                String[] dataFiles = localMessageStoreDirectory.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return (!".config".equals(name)) && name.matches("\\d+");
                    }
                });

                for (String dataFile : dataFiles) {
                    messageStoreNameFileMapping.putIfAbsent(Long.parseLong(dataFile),
                            new File(localMessageStoreDirectory, dataFile));
                }

                File lastWrittenFileName = messageStoreNameFileMapping
                        .get(writeIndex.longValue() / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);

                if (null == lastWrittenFileName && writeIndex.longValue() % MESSAGES_PER_FILE != 0) {
                    throw new RuntimeException("Data corrupted");
                }

                if (null != lastWrittenFileName) {
                    randomAccessFile = new RandomAccessFile(lastWrittenFileName, "rw");
                    if (writeOffSet.longValue() > 0) {
                        randomAccessFile.seek(writeOffSet.longValue());
                    }
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Load configuration error", e);
            } catch (IOException e) {
                LOGGER.error("Load configuration error", e);
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        //ignore.
                    }
                }
            }
        }
    }

    /**
     * This method is synchronized as there may be multiple threads executing this thread.
     */
    private synchronized void updateConfig() {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(configFile, false));
            bufferedWriter.write("writeIndex=" + writeIndex.longValue());
            bufferedWriter.newLine();
            bufferedWriter.write("writeOffSet=" + writeOffSet.longValue());
            bufferedWriter.newLine();
            bufferedWriter.write("readIndex=" + readIndex.longValue());
            bufferedWriter.newLine();
            bufferedWriter.write("readOffSet=" + readOffSet.longValue());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            LOGGER.error("Unable to update config file", e);
        } finally {
            if (null != bufferedWriter) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    //ignore.
                }
            }
        }
        LOGGER.info("LocalMessageStore configuration file updated.");
    }

    /**
     * This method is assumed to execute concurrently.
     *
     * @param message Message to stash.
     */
    @Override
    public void stash(Message message) {
        if (!ready) {
            throw new RuntimeException("Message store is not ready. You may have closed it already.");
        }

        try {
            //Block if no space available.
            messageQueue.put(message);
        } catch (InterruptedException e) {
            LOGGER.error("Unable to stash message locally.", e);
        }
    }

    /**
     * Flush message into hark disk. If no sufficient usable disk space, no flush operation will be performed.
     */
    private void flush() {
        flush(false);
    }

    /**
     * Flush messages into hard disk.
     * @param skipAvailableDiskSpaceCheck Indicate if available disk space check should be skipped.
     */
    private void flush(boolean skipAvailableDiskSpaceCheck) {
        LOGGER.info("Local message store starts to flush.");

        if (!skipAvailableDiskSpaceCheck) {
            float usableDiskSpaceRatio = getUsableDiskSpacePercent();
            if ( usableDiskSpaceRatio < 1 - DISK_HIGH_WATER_LEVEL) {
                long current = System.currentTimeMillis();

                if (current - lastWarnTime > 2000 || -1 == lastWarnTime) {
                    LOGGER.error("No sufficient disk space! Cannot to flush!");
                    lastWarnTime = current;
                }

                if (current - lastFlushTime > 2000 || -1 == lastFlushTime) {
                    updateConfig();
                    lastFlushTime = current;
                }
                return;
            } else if (usableDiskSpaceRatio < 1 - DISK_WARNING_WATER_LEVEL) {
                long current = System.currentTimeMillis();
                if (current - lastWarnTime > 5000 || -1 == lastWarnTime) {
                    LOGGER.warn("Usable disk space now is only: " + usableDiskSpaceRatio + "%!");
                    lastWarnTime = current;
                }
            }
        }

        try {
            if (!lock.tryLock()) {
                lock.lockInterruptibly();
            }
            Message message = messageQueue.poll();
            int numberOfMessageToCommit = 0;

            while (null != message) {
                writeIndex.incrementAndGet();
                long currentWriteIndex = writeIndex.longValue();

                if (1 == currentWriteIndex ||
                        (currentWriteIndex - 1) / MESSAGES_PER_FILE > (currentWriteIndex - 2) / MESSAGES_PER_FILE) {
                    //we need to create a new file.
                    File newMessageStoreFile = new File(localMessageStoreDirectory, String.valueOf(currentWriteIndex));
                    if (!newMessageStoreFile.createNewFile()) {
                        throw new RuntimeException("Unable to create new local message store file");
                    }
                    messageStoreNameFileMapping.putIfAbsent(currentWriteIndex, newMessageStoreFile);

                    //close previous file.
                    if (null != randomAccessFile) {
                        randomAccessFile.close();
                    }
                    File dataFile = messageStoreNameFileMapping.get(currentWriteIndex);
                    randomAccessFile = new RandomAccessFile(dataFile, "rw");
                }

                if (null == randomAccessFile) {
                    File currentWritingDataFile = messageStoreNameFileMapping
                            .get(writeIndex.longValue() / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);

                    randomAccessFile = new RandomAccessFile(currentWritingDataFile, "rw");
                }

                byte[] msgData = JSON.toJSONString(message).getBytes();
                randomAccessFile.writeLong(msgData.length);
                randomAccessFile.write(msgData);
                writeOffSet.set(randomAccessFile.getFilePointer());

                if (writeIndex.longValue() % MESSAGES_PER_FILE == 0) {
                    writeOffSet.set(0L);
                }

                if(++numberOfMessageToCommit % MAXIMUM_NUMBER_OF_DIRTY_MESSAGE_IN_QUEUE == 0) {
                    updateConfig();
                }
                message = messageQueue.poll();
            }
            updateConfig();
        } catch (InterruptedException e) {
            LOGGER.error("Flush messages error", e);
            throw new RuntimeException("Lock exception", e);
        } catch (IOException e) {
            LOGGER.error("Flush messages error", e);
            throw new RuntimeException("IO Error", e);
        } finally {
            LOGGER.info("Local message store flushes completely.");
            lock.unlock();
        }
    }

    private float getUsableDiskSpacePercent() {
        try {
            FileStore fileStore = Files.getFileStore(localMessageStoreDirectory.toPath());
            return fileStore.getUsableSpace() * 1.0F / fileStore.getTotalSpace();
        } catch (IOException e) {
            LOGGER.error("Unable to get disk usage.", e);
            return 0.0F;
        }
    }

    @Override
    public Message[] pop(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n should be positive");
        }

        if (!ready) {
            throw new RuntimeException("Message store is not ready. You may have closed it already.");
        }

        try {
            if (!lock.tryLock()) {
                lock.lockInterruptibly();
            }

            int messageToRead = Math.min(getNumberOfMessageStashed(), n);
            Message[] messages = new Message[messageToRead];

            if (messageToRead == 0) {
                return messages;
            }

            int messageRead = 0;

            //First to retrieve messages from message queue, beginning from head side, which is held in memory.
            Message message = messageQueue.poll();
            while (null != message) {
                messages[messageRead++] = message;
                if (messageRead == messageToRead) { //We've already got all messages we want to pop.
                    return messages;
                }
                message = messageQueue.poll();
            }

            //In case we need more messages, read from local files.
            RandomAccessFile readRandomAccessFile = null;
            File currentReadFile = null;
            while (messageRead < messageToRead && readIndex.longValue() <= writeIndex.longValue()) {
                if (null == readRandomAccessFile) {
                    currentReadFile = messageStoreNameFileMapping
                            .get(readIndex.longValue() / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);
                    if (null == currentReadFile || !currentReadFile.exists()) {
                        throw new RuntimeException("Data file corrupted");
                    }

                    readRandomAccessFile = new RandomAccessFile(currentReadFile, "rw");

                    if (readOffSet.longValue() > 0) {
                        readRandomAccessFile.seek(readOffSet.longValue());
                    }
                }

                long messageSize = readRandomAccessFile.readLong();
                byte[] data = new byte[(int) messageSize];
                readRandomAccessFile.read(data);
                messages[messageRead++] = JSON.parseObject(data, Message.class);
                readIndex.incrementAndGet();
                readOffSet.set(readRandomAccessFile.getFilePointer());

                if (readIndex.longValue() % MESSAGES_PER_FILE == 0 && currentReadFile.exists()) {
                    readRandomAccessFile.close();
                    readRandomAccessFile = null;
                    readOffSet.set(0L);
                    messageStoreNameFileMapping.remove((readIndex.longValue() - 1) / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);
                    if (!currentReadFile.delete()) {
                        LOGGER.warn("Unable to delete used data file: {}", currentReadFile.getAbsolutePath());
                    }
                }
            }
            return messages;
        } catch (InterruptedException e) {
            LOGGER.error("Pop message error", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Pop message error", e);
        } catch (IOException e) {
            LOGGER.error("Pop message error", e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public int getNumberOfMessageStashed() {
        synchronized (DefaultLocalMessageStore.class) {
            return writeIndex.intValue() - readIndex.intValue() + messageQueue.size();
        }
    }

    public void close() throws InterruptedException {
        if (ready) {
            flush(true);
            flushConfigPeriodicallyByTimeExecutorService.shutdown();
            flushConfigPeriodicallyByMessageNumberExecutorService.shutdown();

            flushConfigPeriodicallyByTimeExecutorService.awaitTermination(30, TimeUnit.SECONDS);
            flushConfigPeriodicallyByMessageNumberExecutorService.awaitTermination(30, TimeUnit.SECONDS);
        }

        ready = false;
        LOGGER.info("Default local message store shuts down completely");
    }

    public boolean isReady() {
        return ready;
    }
}