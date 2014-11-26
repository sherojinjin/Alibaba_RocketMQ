package com.ndpmedia.rocketmq.ip.impl;

import com.ndpmedia.rocketmq.io.Constants;
import com.ndpmedia.rocketmq.io.FileManager;
import com.ndpmedia.rocketmq.ip.IPMappingManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("ipMappingManager")
public class IPMappingManagerImpl implements IPMappingManager {

    private static final String IP_FILE_KEY = "ip_mapping_file";

    private FileManager fileManager;

    private static final ConcurrentHashMap<String, String> MAPPING = new ConcurrentHashMap<String, String>();

    @Override
    public ConcurrentHashMap<String, String> refresh() throws IOException{
        List<String> rows = fileManager.read(IP_FILE_KEY);
        MAPPING.clear();
        for (String row : rows) {
            if (null != row && !row.isEmpty()) {
                String[] segments = row.trim().split(Constants.REGEX_SEPARATOR);
                if (segments.length == 2) {
                    MAPPING.put(segments[0].trim(), segments[1].trim());
                }
            }
        }

        return MAPPING;
    }

    @Override
    public void remove(String innerIP) throws IOException {
        refresh();
        MAPPING.remove(innerIP);
        fileManager.write(IP_FILE_KEY, MAPPING, false);
    }

    @Override
    public void add(String innerIP, String publicIP) throws IOException {
        refresh();
        MAPPING.put(innerIP, publicIP);
        fileManager.write(IP_FILE_KEY, MAPPING, false);
    }

    @Override
    public String lookup(String privateIP) throws IOException {
        if (MAPPING.isEmpty()) {
            refresh();
        }
        return MAPPING.get(privateIP);
    }

    @Override
    public Map<String, String> list() throws IOException {
        if (MAPPING.isEmpty()) {
            return refresh();
        }
        return MAPPING;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
}
