package com.ndpmedia.rocketmq.nameserver.impl;

import com.ndpmedia.rocketmq.nameserver.NameServerKVService;
import com.ndpmedia.rocketmq.nameserver.model.KV;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TODO implement this.
 */
@Service("nameServerKVService")
public class NameServerKVServiceImpl implements NameServerKVService {

    @Override
    public void add(KV kv) {

    }

    @Override
    public void delete(KV kv) {

    }

    @Override
    public void delete(long id) {

    }

    @Override
    public void update(KV kv) {

    }

    @Override
    public KV get(long id) {
        return null;
    }

    @Override
    public List<KV> list() {
        return null;
    }
}
