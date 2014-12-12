package com.alibaba.rocketmq.client.producer.concurrent;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.message.Message;
import org.slf4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultLocalMessageStore implements LocalMessageStore {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int MESSAGES_PER_FILE = 100000;

    private final AtomicLong writeIndex = new AtomicLong(0L);
    private final AtomicLong writeOffSet = new AtomicLong(0L);

    private final AtomicLong readIndex = new AtomicLong(0L);
    private final AtomicLong readOffSet = new AtomicLong(0L);

    private static final String STORE_LOCATION = System.getProperty("defaultLocalMessageStoreLocation",
            System.getProperty("user.home") + File.separator + ".localMessageStore");

    private File localMessageStoreDirectory;

    private ConcurrentHashMap<Long, File> messageStoreNameFileMapping = new ConcurrentHashMap<Long, File>();

    private File configFile;

    private RandomAccessFile randomAccessFile;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DefaultLocalMessageStore(String producerGroup) {
        localMessageStoreDirectory = new File(STORE_LOCATION, producerGroup);

        if (!localMessageStoreDirectory.exists()) {
            if (!localMessageStoreDirectory.mkdirs()) {
                throw new RuntimeException("Local message store directory does not exist and unable to create one");
            }
        }
        loadConfig();
    }

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
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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

    private void updateConfig() {
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
            LOGGER.error("Unable to update config file", e.getMessage());
        } finally {
            if (null != bufferedWriter) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    //ignore.
                }
            }
        }
    }

    @Override
    public void stash(Message message) {
        LOGGER.debug("Stashing message: {}", JSON.toJSONString(message));
        try {
            if (!lock.writeLock().tryLock()) {
                lock.writeLock().lockInterruptibly();
            }
            writeIndex.incrementAndGet();
            long currentWriteIndex = writeIndex.longValue();

            if (1 == currentWriteIndex ||
                    (currentWriteIndex -1) / MESSAGES_PER_FILE > (currentWriteIndex - 2) / MESSAGES_PER_FILE) {
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

            //Fix possible discrepancy
            if (readIndex.get() > writeIndex.get()) {
                readIndex.lazySet(writeIndex.get());
            }

            updateConfig();
        } catch (InterruptedException e) {
            throw new RuntimeException("Lock exception", e);
        } catch (IOException e) {
            throw new RuntimeException("IO Error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Message[] pop() {
        int messageCount = getNumberOfMessageStashed();
        if (messageCount == 0) {
            return new Message[0];
        } else {
            try {
                if(!lock.readLock().tryLock()) {
                    lock.readLock().lockInterruptibly();
                }
                messageCount = getNumberOfMessageStashed();
                Message[] messages = new Message[messageCount];
                int messageRead = 0;
                RandomAccessFile readRandomAccessFile = null;
                File currentReadFile = null;
                while (messageRead < messageCount) {
                    if(readIndex.get() > writeIndex.get()) {
                        break;
                    }
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
                    byte[] data = new byte[(int)messageSize];
                    readRandomAccessFile.read(data);
                    messages[messageRead++] = JSON.parseObject(data, Message.class);
                    readIndex.incrementAndGet();
                    readOffSet.set(readRandomAccessFile.getFilePointer());

                    if (readIndex.longValue() % MESSAGES_PER_FILE == 0 && currentReadFile.exists()) {
                        readRandomAccessFile.close();
                        readRandomAccessFile = null;
                        readOffSet.set(0L);
                        messageStoreNameFileMapping
                                .remove((readIndex.longValue() - 1) / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);
                        if (!currentReadFile.delete()) {
                            LOGGER.warn("Unable to delete used data file: {}", currentReadFile.getAbsolutePath());
                        }
                    }
                }
                updateConfig();
                return messages;
            } catch (InterruptedException e) {
                LOGGER.error("Pop message error, caused by {}", e.getMessage());
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                LOGGER.error("Pop message error, caused by {}", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("Pop message error, caused by {}", e.getMessage());
                e.printStackTrace();
            } finally {
                lock.readLock().unlock();
            }
            return null;
        }
    }

    @Override
    public Message[] pop(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n should be positive");
        }

        int messageCount = getNumberOfMessageStashed();

        if (messageCount <= n) {
            return pop();
        } else {
            try {
                if(!lock.readLock().tryLock()) {
                    lock.readLock().lockInterruptibly();
                }
                Message[] messages = new Message[n];
                int messageRead = 0;
                RandomAccessFile readRandomAccessFile = null;
                File currentReadFile = null;
                while (messageRead < n) {
                    if(readIndex.get() > writeIndex.get()) {
                        break;
                    }

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
                    byte[] data = new byte[(int)messageSize];
                    readRandomAccessFile.read(data);
                    messages[messageRead++] = JSON.parseObject(data, Message.class);
                    readIndex.incrementAndGet();
                    readOffSet.set(readRandomAccessFile.getFilePointer());

                    if (readIndex.longValue() % MESSAGES_PER_FILE == 0 && currentReadFile.exists()) {
                        readRandomAccessFile.close();
                        readRandomAccessFile = null;
                        readOffSet.set(0L);
                        messageStoreNameFileMapping
                                .remove((readIndex.longValue() - 1) / MESSAGES_PER_FILE * MESSAGES_PER_FILE + 1);
                        if (!currentReadFile.delete()) {
                            LOGGER.warn("Unable to delete used data file: {}", currentReadFile.getAbsolutePath());
                        }
                    }
                }
                updateConfig();
                return messages;
            } catch (InterruptedException e) {
                LOGGER.error("Pop message error, caused by {}", e.getMessage());
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                LOGGER.error("Pop message error, caused by {}", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("Pop message error, caused by {}", e.getMessage());
                e.printStackTrace();
            } finally {
                lock.readLock().unlock();
            }
            return null;
        }
    }

    public int getNumberOfMessageStashed() {
        synchronized (DefaultLocalMessageStore.class) {
            return writeIndex.intValue() - readIndex.intValue();
        }
    }

    public void close() {
        updateConfig();
    }
}
