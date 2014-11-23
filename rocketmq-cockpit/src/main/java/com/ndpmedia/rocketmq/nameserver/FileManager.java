package com.ndpmedia.rocketmq.nameserver;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileManager {

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    private static final Properties CONFIG = new Properties();

    static {
        ClassLoader classLoader = FileManager.class.getClassLoader();
        InputStream inputStream = null;
        try {
            inputStream = classLoader.getResourceAsStream("config.properties");
            CONFIG.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //Ignore.
                }
            }
        }

    }


    public List<String> read() throws IOException {
        try {
            if (!lock.readLock().tryLock()) {
                lock.readLock().lockInterruptibly();
            }

            File nameServerFile = new File(CONFIG.getProperty("name_server_file"));

            if (! nameServerFile.exists()) {
                throw new IOException("Name server file[" + nameServerFile.getCanonicalPath() + "] does not exist");
            }

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(nameServerFile));
                List<String> result = new ArrayList<String>();
                String line = null;
                while (null != (line = bufferedReader.readLine())) {
                    if (!line.isEmpty()) {
                        result.add(line);
                    }
                }
                return result;
            } finally {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }

            }
        } catch (InterruptedException e) {
            throw new IOException("ReadWriteLock error", e);
        } finally {
            lock.readLock().unlock();
        }
    }


    public void write(ConcurrentHashMap<String, Long> map, boolean append) throws IOException {
        try {
            if (!lock.writeLock().tryLock()) {
                lock.writeLock().lockInterruptibly();
            }

            File nameServerFile = new File(CONFIG.getProperty("name_server_file"));
            if (!nameServerFile.exists()) {
                if (!nameServerFile.getParentFile().exists()) {
                    nameServerFile.getParentFile().mkdirs();
                }

                if (!nameServerFile.createNewFile()) {
                    throw new IOException("Failed to create name server file for storing name server list");
                }
            }

            BufferedWriter bufferedWriter = null;

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(nameServerFile, append));

                boolean first = true;
                for (Map.Entry<String, Long> row : map.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.write(row.getKey());
                    bufferedWriter.write(" || ");
                    bufferedWriter.write(row.getValue().toString());
                }
                bufferedWriter.flush();
            } finally {
                if (null != bufferedWriter) {
                    bufferedWriter.close();
                }
            }
        } catch (InterruptedException e) {
            throw new IOException("IO Error", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
