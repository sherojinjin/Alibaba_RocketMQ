package com.ndpmedia.rocketmq.ip.model;

/**
 * Created by lizhanhui on 11/26/14.
 */
public class IPPair {
    private String innerIP;
    private String publicIP;

    public IPPair() {}

    public IPPair(String innerIP, String publicIP) {
        this.innerIP = innerIP;
        this.publicIP = publicIP;
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
}
