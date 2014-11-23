package com.ndpmedia.rocketmq.nameserver;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NameServerManagerImpl implements NameServerManager {

    private static final FileManager FILE_MANAGER = new FileManager();

    private static final ConcurrentHashMap<String, Long> NAME_SERVERS = new ConcurrentHashMap<String, Long>(16);


    private static final String REGEX_SEPARATOR = "\\|\\|";

    @Override
    public Set<String> list() {
        try {
            if (NAME_SERVERS.isEmpty()) {
                List<String> rows = FILE_MANAGER.read();
                for (String row : rows) {
                    String[] segments = row.split(REGEX_SEPARATOR);
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

            List<String> rows = FILE_MANAGER.read();
            for (String row : rows) {
                String[] segments = row.split(REGEX_SEPARATOR);
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

            List<String> rows = FILE_MANAGER.read();
            for (String row : rows) {
                String[] segments = row.split(REGEX_SEPARATOR);
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
             FILE_MANAGER.write(NAME_SERVERS, false);   //could be better by appending.
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
            FILE_MANAGER.write(NAME_SERVERS, false);
        } catch (IOException e) {
           e.printStackTrace();
        }
        return NAME_SERVERS.keySet();
    }
}
