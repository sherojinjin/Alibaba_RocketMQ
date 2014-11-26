package com.ndpmedia.rocketmq.ip;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface IPMappingManager {

    ConcurrentHashMap<String, String> refresh() throws IOException;

    void remove(String innerIP) throws IOException;

    void add(String innerIP, String publicIP) throws IOException;

    String lookup(String privateIP) throws IOException;

    Map<String,String> list() throws IOException;
}
