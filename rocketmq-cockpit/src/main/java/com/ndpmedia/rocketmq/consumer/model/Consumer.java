package com.ndpmedia.rocketmq.consumer.model;

import com.alibaba.rocketmq.remoting.protocol.LanguageCode;

/**
 * consumer bean.
 */
public class Consumer {
    private String clientId;
    private String clientAddr;
    private LanguageCode language;
    private int version;


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    public String getClientAddr() {
        return clientAddr;
    }


    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }


    public LanguageCode getLanguage() {
        return language;
    }


    public void setLanguage(LanguageCode language) {
        this.language = language;
    }


    public int getVersion() {
        return version;
    }


    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Consumer{" +
                "clientId='" + clientId + '\'' +
                ", clientAddr='" + clientAddr + '\'' +
                ", language=" + language +
                ", version=" + version +
                '}';
    }
}
