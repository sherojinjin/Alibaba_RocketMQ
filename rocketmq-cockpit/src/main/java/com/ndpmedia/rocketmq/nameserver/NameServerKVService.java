package com.ndpmedia.rocketmq.nameserver;

import com.ndpmedia.rocketmq.nameserver.model.KV;

import java.util.List;

public interface NameServerKVService {
    void add(KV kv);

    void delete(KV kv);

    void delete(long id);

    void update(KV kv);

    KV get(long id);

    List<KV> list();
}
