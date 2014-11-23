package com.ndpmedia.rocketmq.nameserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface NameServerManager {

    Set<String> list();

    Set<String> list(boolean fromDisk);

    Set<String> add(String nameServer);

    Set<String> remove(String nameServer);

    public ConcurrentHashMap<String, Long> listAll(boolean fromDisk);
}
