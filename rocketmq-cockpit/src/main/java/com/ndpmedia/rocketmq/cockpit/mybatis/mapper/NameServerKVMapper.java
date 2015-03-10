package com.ndpmedia.rocketmq.cockpit.mybatis.mapper;

import com.ndpmedia.rocketmq.cockpit.model.KV;

import java.util.List;

public interface NameServerKVMapper {

    KV get(long id);

    List<KV> list();

    long persist(KV kv);

    void update(KV kv);
}
