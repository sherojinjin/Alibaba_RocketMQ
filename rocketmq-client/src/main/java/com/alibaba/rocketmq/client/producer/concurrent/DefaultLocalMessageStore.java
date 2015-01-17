package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.ThreadFactoryImpl;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.StashableMessage;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultLocalMessageStore implements LocalMessageStore {

    private static final String DEFAULT_STORE_LOCATION = "/dianyi/data/";

    private static final String LOCAL_MESSAGE_STORE_FOLDER_NAME = ".localMessageStore";

    private static final String ABORT_FILE_NAME = ".abort";

    private static final String CONFIG_FILE_NAME = ".config";

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int MESSAGES_PER_FILE = 10;

    private final AtomicLong writeIndex = new AtomicLong(0L);
    private final AtomicLong writeOffSet = new AtomicLong(0L);

    private final AtomicLong readIndex = new AtomicLong(0L);
    private final AtomicLong readOffSet = new AtomicLong(0L);

    private static final int MAGIC_CODE = 0xAABBCCDD ^ 1880681586 + 8;

    private String storeLocation = System.getProperty("defaultLocalMessageStoreLocation", DEFAULT_STORE_LOCATION);

    private File localMessageStoreDirectory;

    private ConcurrentHashMap<Long, File> messageStoreNameFileMapping = new ConcurrentHashMap<Long, File>();

    private File configFile;

    private RandomAccessFile writeRandomAccessFile;

    private ReentrantLock lock = new ReentrantLock();

    private LinkedBlockingQueue<StashableMessage> messageQueue = new LinkedBlockingQueue<StashableMessage>(50000);

    private ScheduledExecutorService flushConfigPeriodicallyByTimeExecutorService;

    private ScheduledExecutorService flushConfigPeriodicallyByMessageNumberExecutorService;

    private volatile boolean ready = false;

    private static final int MAXIMUM_NUMBER_OF_DIRTY_MESSAGE_IN_QUEUE = 1000;

    private static final int UPDATE_CONFIG_PER_FLUSHING_NUMBER_OF_MESSAGE = 500;

    private static final float DISK_HIGH_WATER_LEVEL = 0.75F;

    private static final float DISK_WARNING_WATER_LEVEL = 0.65F;

    private volatile long lastFlushTime = -1;

    private volatile long lastWarnTime = -1;

    private static final String ACCESS_FILE_MODE = "rws";

    public DefaultLocalMessageStore(String storeName) throws IOException {
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

        if (isLastShutdownAbort()) {
            init(true);
        } else {
            init(false);
        }

        flushConfigPeriodicallyByTimeExecutorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryImpl("LocalMessageStoreFlushConfigServicePeriodicallyByTime"));

        flushConfigPeriodicallyByTimeExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }, 0, 1000 * 10, TimeUnit.MILLISECONDS);

        flushConfigPeriodicallyByMessageNumberExecutorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryImpl("LocalMessageStoreFlushConfigServicePeriodicallyByMessageNumber"));

        flushConfigPeriodicallyByMessageNumberExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (messageQueue.size() > MAXIMUM_NUMBER_OF_DIRTY_MESSAGE_IN_QUEUE) {
                    flush();
                }
            }
        }, 1000 * 3, 1000 * 3, TimeUnit.MILLISECONDS);

        ready = true;

        try {
            createAbortFile();
        } catch (IOException e) {
            LOGGER.error("Failed to create abort file.", e);
            throw e;
        }

        LOGGER.info("Local Message store starts to operate.");
    }

    private boolean isLastShutdownAbort() {
        File abortFile = new File(localMessageStoreDirectory, ABORT_FILE_NAME);
        return abortFile.exists();
    }

    private void createAbortFile() throws IOException {
        File abortFile = new File(localMessageStoreDirectory, ABORT_FILE_NAME);
        if (abortFile.exists()) {
            LOGGER.error("Abort file already exists.");
        } else {
            if (!abortFile.createNewFile()) {
                LOGGER.error("Failed to create abort file");
            }
        }
    }

    private void deleteAbortFile() {
        File abortFile = new File(localMessageStoreDirectory, ABORT_FILE_NAME);
        if (abortFile.exists()) {
            if (!abortFile.delete()) {
                LOGGER.error("Failed to delete abort file");
            }
        } else {
            LOGGER.error("Abort file does not exist");
        }
    }

    /**
     * This method will execute on startup.
     */
    private void init(boolean needToRecoverData) throws IOException {
        configFile = new File(localMessageStoreDirectory, CONFIG_FILE_NAME);
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

                String[] dataFiles = getMessageDataFiles();
                for (String dataFile : dataFiles) {
                    messageStoreNameFileMapping.putIfAbsent(Long.parseLong(dataFile),
                            new File(localMessageStoreDirectory, dataFile));
                }

                if (!isMessageDataComplete(dataFiles)) {
                    throw new RuntimeException("Message data files are corrupted and unable to recover automatically");
                }

                if (needToRecoverData) {
                    long readIndexLong = readIndex.longValue();
                    long writeIndexLong = writeIndex.longValue();

                    long minMessageFileName = 0;
                    long maxMessageFileName = 0;

                    if (dataFiles.length > 0) {
                        minMessageFileName = Long.parseLong(dataFiles[0]);
                        maxMessageFileName = Long.parseLong(dataFiles[dataFiles.length - 1]);
                    }

                    //Remove possibly existing deprecated message data files.
                    for (String dataFile : dataFiles) {
                        long dataFileLong = Long.parseLong(dataFile);
                        if (dataFileLong < readIndexLong - MESSAGES_PER_FILE) {
                            File messageDataFile = new File(localMessageStoreDirectory, dataFile);
                            if (!messageDataFile.delete()) {
                                LOGGER.error("Failed to delete deprecated message data file: {}",
                                        messageDataFile.getCanonicalPath());
                                throw new IOException("Failed to delete file: " + messageDataFile.getAbsoluteFile());
                            } else {
                                minMessageFileName += MESSAGES_PER_FILE;
                                messageStoreNameFileMapping.remove(dataFileLong);
                            }
                        } else {
                            break;
                        }
                    }

                    //Fix possible discrepancies.
                    if (!messageStoreNameFileMapping.isEmpty()) {
                        if (readIndexLong < minMessageFileName) {
                            readIndex.set(minMessageFileName - 1);
                            readOffSet.set(0);
                        }

                        if (writeIndexLong < maxMessageFileName
                                || writeIndexLong > maxMessageFileName + MESSAGES_PER_FILE) {
                            writeIndex.set(maxMessageFileName - 1);
                            writeOffSet.set(0);
                            recoverWriteAheadData(messageStoreNameFileMapping.get(maxMessageFileName));
                        }
                    } else {
                        //Reset all indexes and offsets to 0 if there is any discrepancy and there is no message
                        // data file at the same tile.
                        if (readIndex.longValue() != writeIndex.longValue()
                                || readOffSet.longValue() != writeOffSet.longValue()) {
                            readIndex.set(0);
                            readOffSet.set(0);
                            writeIndex.set(0);
                            writeOffSet.set(0);
                            messageStoreNameFileMapping.clear();
                        }
                    }

                    updateConfig();
                    deleteAbortFile();
                }

                File lastWrittenFileName = messageStoreNameFileMapping
                        .get(writeIndex.longValue() / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);

                if (null == lastWrittenFileName && writeIndex.longValue() % MESSAGES_PER_FILE != 0) {
                    throw new RuntimeException("Data corrupted");
                }

                if (null != lastWrittenFileName) {
                    writeRandomAccessFile = new RandomAccessFile(lastWrittenFileName, ACCESS_FILE_MODE);
                    if (writeOffSet.longValue() > 0) {
                        writeRandomAccessFile.seek(writeOffSet.longValue());
                    }
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Initializing default local store fails.", e);
                throw e;
            } catch (IOException e) {
                LOGGER.error("Initializing default local store fails.", e);
                throw e;
            } finally {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
        } else {
            //There is no configuration file.
            String[] dataFiles = getMessageDataFiles();
            if (!isMessageDataComplete(dataFiles)) {
                throw new RuntimeException("Message data files are corrupted and unable to recover automatically");
            }

            if (dataFiles.length > 0) {
                readIndex.set(Long.parseLong(dataFiles[0]) - 1);
                readOffSet.set(0);

                final int len = dataFiles.length;
                writeIndex.set(Long.parseLong(dataFiles[len - 1]) - 1);
                writeOffSet.set(0);
                recoverWriteAheadData(new File(localMessageStoreDirectory, dataFiles[len - 1]));
            }
        }
    }

    private String[] getMessageDataFiles() {
       String[] dataFiles = localMessageStoreDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("\\d+");
            }
        });

        Arrays.sort(dataFiles, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Long.parseLong(o1) < Long.parseLong(o2) ? -1 : 1;
            }
        });

        return dataFiles;
    }

    /**
     * This method checks if there are missing message data files.
     * @param dataFiles sorted message data file names in ascending order.
     * @return true if there is no missing message data file; false otherwise.
     */
    private boolean isMessageDataComplete(String[] dataFiles) {
        if (null == dataFiles || 0 == dataFiles.length) {
            return true;
        }

        long[] dataFileLongArray = new long[dataFiles.length];
        boolean complete = true;
        int i = 0;
        for (String dataFile: dataFiles) {
            dataFileLongArray[i] = Long.parseLong(dataFile);
            if (i > 0 && dataFileLongArray[i] != (dataFileLongArray[i - 1] + MESSAGES_PER_FILE)) {
                for (long j = dataFileLongArray[i - 1] + MESSAGES_PER_FILE; j < dataFileLongArray[i]; j += MESSAGES_PER_FILE) {
                    LOGGER.error("Found missing message data file:"+ localMessageStoreDirectory.getAbsolutePath()
                            + File.separator + String.valueOf(j));
                }
                complete = false;
            }
            i++;
        }
        return complete;
    }

    private void recoverWriteAheadData(File dataFile) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(dataFile, "r");
        try {
            int recoveredMessageNumber = 0;
            while (recoveredMessageNumber++ < MESSAGES_PER_FILE) {
                if (writeOffSet.longValue() + 4 + 4 > randomAccessFile.length()) {
                    break;
                }

                int messageSize = randomAccessFile.readInt();
                int magicCode = randomAccessFile.readInt();
                if (magicCode != MAGIC_CODE) {
                    break;
                }

                if (writeOffSet.longValue() + 4 + 4 + messageSize > randomAccessFile.length()) {
                    break;
                }

                byte[] messageData = new byte[messageSize];
                randomAccessFile.readFully(messageData);

                try {
                    JSON.parseObject(messageData, StashableMessage.class);
                } catch (Exception e) {
                    break;
                }

                writeIndex.incrementAndGet();
                if (writeIndex.longValue() % MESSAGES_PER_FILE == 0) {
                    writeOffSet.set(0);
                } else {
                    writeOffSet.addAndGet(4 + 4 + messageSize);
                }
            }
        } finally {
            randomAccessFile.close();
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
            StashableMessage stashableMessage = message.buildStashableMessage();
            messageQueue.put(stashableMessage);
        } catch (InterruptedException e) {
            LOGGER.error("Unable to stash message locally.", e);
            LOGGER.error("Fatal Error: Message [" + JSON.toJSONString(message) + "] is lost.");
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
     *
     * @param skipAvailableDiskSpaceCheck Indicate if available disk space check should be skipped.
     */
    private void flush(boolean skipAvailableDiskSpaceCheck) {
        LOGGER.info("Local message store starts to flush.");

        if (!skipAvailableDiskSpaceCheck) {
            float usableDiskSpaceRatio = getUsableDiskSpacePercent();
            if (usableDiskSpaceRatio < 1 - DISK_HIGH_WATER_LEVEL) {
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
                long currentWriteIndex = writeIndex.longValue() + 1;
                if (1 == currentWriteIndex ||
                        (currentWriteIndex - 1) / MESSAGES_PER_FILE > (currentWriteIndex - 2) / MESSAGES_PER_FILE) {
                    //we need to create a new file.
                    File newMessageStoreFile = new File(localMessageStoreDirectory, String.valueOf(currentWriteIndex));
                    if (!newMessageStoreFile.createNewFile()) {
                        throw new RuntimeException("Unable to create new local message store file");
                    }
                    messageStoreNameFileMapping.putIfAbsent(currentWriteIndex, newMessageStoreFile);

                    //close previous file.
                    if (null != writeRandomAccessFile) {
                        writeRandomAccessFile.close();
                    }
                    File dataFile = messageStoreNameFileMapping.get(currentWriteIndex);
                    writeRandomAccessFile = new RandomAccessFile(dataFile, ACCESS_FILE_MODE);
                }

                if (null == writeRandomAccessFile) {
                    File currentWritingDataFile = messageStoreNameFileMapping
                            .get(writeIndex.longValue() / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);

                    writeRandomAccessFile = new RandomAccessFile(currentWritingDataFile, ACCESS_FILE_MODE);
                }

                byte[] msgData = JSON.toJSONString(message).getBytes();
                writeRandomAccessFile.writeInt(msgData.length);
                writeRandomAccessFile.writeInt(MAGIC_CODE);
                writeRandomAccessFile.write(msgData);
                writeOffSet.addAndGet(4 + 4 + msgData.length);
                writeIndex.incrementAndGet();
                if (writeIndex.longValue() % MESSAGES_PER_FILE == 0) {
                    writeOffSet.set(0L);
                }

                if (++numberOfMessageToCommit % UPDATE_CONFIG_PER_FLUSHING_NUMBER_OF_MESSAGE == 0) {
                    updateConfig();
                }

                //Prepare for next round.
                message = messageQueue.poll();
            }

            updateConfig();
        } catch (InterruptedException e) {
            LOGGER.error("Flush messages error", e);
        } catch (IOException e) {
            LOGGER.error("Flush messages error", e);
        } catch (Exception e) {
            LOGGER.error("Flush messages error", e);
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
    public StashableMessage[] pop(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n should be positive");
        }

        if (!ready) {
            throw new RuntimeException("Message store is not ready. You may have closed it already.");
        }


        //In case we need more messages, read from local files.
        RandomAccessFile readRandomAccessFile = null;
        try {
            if (!lock.tryLock()) {
                lock.lockInterruptibly();
            }

            int messageToRead = Math.min(getNumberOfMessageStashed(), n);
            StashableMessage[] messages = new StashableMessage[messageToRead];

            if (messageToRead == 0) {
                return messages;
            }

            int messageRead = 0;

            //First retrieve messages from message queue, beginning from head side, which is held in memory.
            StashableMessage message = messageQueue.poll();
            while (null != message) {
                messages[messageRead++] = message;
                if (messageRead == messageToRead) { //We've already got all messages we want to pop.
                    return messages;
                }
                message = messageQueue.poll();
            }


            File currentReadFile = null;
            while (messageRead < messageToRead && readIndex.longValue() <= writeIndex.longValue()) {
                if (null == readRandomAccessFile) {
                    currentReadFile = messageStoreNameFileMapping
                            .get(readIndex.longValue() / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);
                    if (null == currentReadFile || !currentReadFile.exists()) {
                        throw new RuntimeException("Data file corrupted");
                    }

                    readRandomAccessFile = new RandomAccessFile(currentReadFile, ACCESS_FILE_MODE);

                    if (readOffSet.longValue() > 0) {
                        readRandomAccessFile.seek(readOffSet.longValue());
                    }
                }

                if (readOffSet.longValue() + 4 + 4 > readRandomAccessFile.length()) {
                    LOGGER.error("Data inconsistent!");
                    break;
                }

                int messageSize = readRandomAccessFile.readInt();


                int magicCode = readRandomAccessFile.readInt();
                if (magicCode != MAGIC_CODE) {
                    LOGGER.error("Data inconsistent!");
                }

                if (readOffSet.longValue() + 4 + 4 + messageSize > readRandomAccessFile.length()) {
                    LOGGER.error("Data inconsistent!");
                    break;
                }

                byte[] data = new byte[messageSize];

                readRandomAccessFile.readFully(data);

                messages[messageRead++] = JSON.parseObject(data, StashableMessage.class);
                readIndex.incrementAndGet();
                readOffSet.addAndGet(4 + 4 + messageSize); //message_size_int + magic_code_int + messageSize.

                if (readIndex.longValue() % MESSAGES_PER_FILE == 0) {
                    readOffSet.set(0L);
                    readRandomAccessFile.close();
                    readRandomAccessFile = null;
                    messageStoreNameFileMapping.remove((readIndex.longValue() - 1) / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);

                    if (currentReadFile.exists()) {
                        if (!currentReadFile.delete()) {
                            LOGGER.warn("Unable to delete used data file: {}", currentReadFile.getAbsolutePath());
                        }
                    }
                }
            }

            if (messageRead < messageToRead) {
                StashableMessage[] result = new StashableMessage[messageRead];
                System.arraycopy(messages, 0, result, 0, messageRead);
                LOGGER.error("IO Error! Number of messages read is less than expected!");
                return result;
            } else {
                return messages;
            }

        } catch (InterruptedException e) {
            LOGGER.error("Pop message error", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("Pop message error", e);
        } catch (IOException e) {
            LOGGER.error("Pop message error", e);
            LOGGER.error("readIndex:" + readIndex.longValue() + ", writeIndex:" + writeIndex.longValue()
                    + ", readOffset:" + readOffSet.longValue() + ", writeOffset:" + writeOffSet.longValue());
        } finally {
            if (null != readRandomAccessFile) {
                try {
                    readRandomAccessFile.close();
                } catch (IOException e) {
                    LOGGER.error("Unexpected IO error", e);
                }
            }

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

        deleteAbortFile();

        LOGGER.info("Default local message store shuts down completely");
    }

    public boolean isReady() {
        return ready;
    }
}