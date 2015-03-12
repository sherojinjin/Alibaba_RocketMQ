package com.ndpmedia.rocketmq.cockpit.util;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
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


    public List<String> read(String fileKey) throws IOException {
        try {
            if (!lock.readLock().tryLock()) {
                lock.readLock().lockInterruptibly();
            }

            File dataFile = new File(CONFIG.getProperty(fileKey));

            if (!dataFile.exists()) {
                File parentFile = dataFile.getParentFile();
                if (!parentFile.exists()) {
                    if (!parentFile.mkdirs()) {
                        throw new IOException("Unable to create parent folder for [" + dataFile.getCanonicalPath() + "]");
                    }
                }

                if (!dataFile.createNewFile()) {
                    throw new IOException("Unable to create file: [" + dataFile.getCanonicalPath() + "]");
                }
            }

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(dataFile));
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


    public <T> void write(String fileKey, final Map<String, T> map, boolean append) throws IOException {
        try {
            if (!lock.writeLock().tryLock()) {
                lock.writeLock().lockInterruptibly();
            }

            File dataFile = new File(CONFIG.getProperty(fileKey));
            if (!dataFile.exists()) {
                if (!dataFile.getParentFile().exists()) {
                    dataFile.getParentFile().mkdirs();
                }

                if (!dataFile.createNewFile()) {
                    throw new IOException("Failed to create file");
                }
            }

            BufferedWriter bufferedWriter = null;

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(dataFile, append));

                boolean first = true;
                for (Map.Entry<String, T> row : map.entrySet()) {
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

    public static Properties getConfig() {
        return CONFIG;
    }
}
