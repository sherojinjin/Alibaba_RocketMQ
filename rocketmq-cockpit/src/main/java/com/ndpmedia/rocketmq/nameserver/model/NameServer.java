package com.ndpmedia.rocketmq.nameserver.model;

import java.util.Date;

public class NameServer {

    private int id;

    private String url;

    private String ip;

    private String port;

    private long create_time;

    private long update_time;

    private Date date = new Date();

    public NameServer() {}

    public NameServer(String url, long create_time)
    {
        this.url = url;
        if (url.contains(":"))
        {
            this.ip = url.split(":")[0];
            this.port = url.split(":")[1];
        }
        else
        {
            this.ip = url;
        }

        this.create_time = create_time;
        this.date = new Date(create_time);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void makeUrl()
    {
        this.url = this.ip + ":" + this.port;
    }

    public void makeDate()
    {
        this.date = 0 < this.update_time ? new Date(this.update_time) : new Date(this.create_time);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
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

    @Override
    public String toString()
    {
        return "NameServer{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", date=" + date +
                '}';
    }
}
