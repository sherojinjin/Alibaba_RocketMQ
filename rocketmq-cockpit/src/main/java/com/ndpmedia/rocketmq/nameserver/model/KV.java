package com.ndpmedia.rocketmq.nameserver.model;

public class KV {

    private long id;

    private String nameSpace;

    private String key;

    private String value;

    private KVStatus status = KVStatus.DRAFT;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public KVStatus getStatus() {
        return status;
    }

    public void setStatus(KVStatus status) {
        this.status = status;
    }
}
