package com.ndpmedia.rocketmq.ip;

import com.ndpmedia.rocketmq.ip.model.IPPair;

import java.io.IOException;
import java.util.List;

public interface IPMappingManager
{
    void remove(String innerIP) throws IOException;

    void add(String innerIP, String publicIP) throws IOException;

    String lookup(String privateIP) throws IOException;

    List<IPPair> list() throws IOException;
}
