package com.ndpmedia.rocketmq.producer.model;

import com.alibaba.rocketmq.remoting.protocol.LanguageCode;

/**
 * Created by Administrator on 2014/12/30.
 */
public class Producer {
    private String id;
    private String addr;
    private LanguageCode language;
    private String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LanguageCode getLanguage() {

        return language;
    }

    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    public String getAddr() {

        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
