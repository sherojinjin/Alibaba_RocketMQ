package com.ndpmedia.rocketmq.nameserver;

import com.ndpmedia.rocketmq.nameserver.model.NameServer;

import java.util.List;
import java.util.Set;

public interface NameServerManager {

    List<NameServer> list();

    Set<String> listNames();

    List<NameServer> add(String nameServer);

    List<NameServer> remove(String nameServer);
}
