package com.ndpmedia.rocketmq.nameserver.model;

import java.util.Date;

public class NameServer {

    private String url;

    private Date date = new Date();

    public NameServer() {}

    public NameServer(String url, Date date) {
        this.url = url;
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
