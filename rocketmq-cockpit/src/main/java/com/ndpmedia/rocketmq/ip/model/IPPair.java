package com.ndpmedia.rocketmq.ip.model;

/**
 * Created by lizhanhui on 11/26/14.
 */
public class IPPair {
    private int id;
    private String innerIP;
    private String publicIP;
    private long create_time;
    private long update_time;

    public IPPair() {}

    public IPPair(String innerIP, String publicIP) {
        this.innerIP = innerIP;
        this.publicIP = publicIP;
        this.create_time = System.currentTimeMillis();
    }

    public String getInnerIP() {
        return innerIP;
    }

    public void setInnerIP(String innerIP) {
        this.innerIP = innerIP;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }
}
