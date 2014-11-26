package com.ndpmedia.rocketmq.nameserver.impl;

import com.ndpmedia.rocketmq.io.Constants;
import com.ndpmedia.rocketmq.io.FileManager;
import com.ndpmedia.rocketmq.nameserver.NameServerManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service("nameServerManager")
public class NameServerManagerImpl implements NameServerManager {

    private static final ConcurrentHashMap<String, Long> NAME_SERVERS = new ConcurrentHashMap<String, Long>(16);

    private static final String NAME_SERVER_FILE_KEY = "name_server_file";

    private FileManager fileManager;

    @Override
    public Set<String> list() {
        try {
            if (NAME_SERVERS.isEmpty()) {
                List<String> rows = fileManager.read(NAME_SERVER_FILE_KEY);
                for (String row : rows) {
                    String[] segments = row.split(Constants.REGEX_SEPARATOR);
                    if (segments.length == 2) {
                        NAME_SERVERS.putIfAbsent(segments[0].trim(), Long.parseLong(segments[1].trim()));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NAME_SERVERS.keySet();
    }

    @Override
    public Set<String> list(boolean fromDisk) {
        try {
            NAME_SERVERS.clear();

            List<String> rows = fileManager.read(NAME_SERVER_FILE_KEY);
            for (String row : rows) {
                String[] segments = row.split(Constants.REGEX_SEPARATOR);
                if (segments.length == 2) {
                    NAME_SERVERS.putIfAbsent(segments[0].trim(), Long.parseLong(segments[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return NAME_SERVERS.keySet();
    }


    @Override
    public ConcurrentHashMap<String, Long> listAll(boolean fromDisk) {
        try {
            NAME_SERVERS.clear();

            List<String> rows = fileManager.read(NAME_SERVER_FILE_KEY);
            for (String row : rows) {
                String[] segments = row.split(Constants.REGEX_SEPARATOR);
                if (segments.length == 2) {
                    NAME_SERVERS.putIfAbsent(segments[0].trim(), Long.parseLong(segments[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return NAME_SERVERS;
    }

    @Override
    public Set<String> add(String nameServer) {
        if (null == nameServer || nameServer.trim().isEmpty()) {
            return NAME_SERVERS.keySet();
        }
        try {
            list(true);
             NAME_SERVERS.putIfAbsent(nameServer, System.currentTimeMillis());
             fileManager.write(NAME_SERVER_FILE_KEY, NAME_SERVERS, false);   //could be better by appending.
             return list(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return NAME_SERVERS.keySet();
    }

    @Override
    public Set<String> remove(String nameServer) {

        if (null == nameServer || nameServer.trim().isEmpty()) {
            return NAME_SERVERS.keySet();
        }

        try {
            list(true);
            NAME_SERVERS.remove(nameServer);
            fileManager.write(NAME_SERVER_FILE_KEY, NAME_SERVERS, false);
        } catch (IOException e) {
           e.printStackTrace();
        }
        return NAME_SERVERS.keySet();
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
}
