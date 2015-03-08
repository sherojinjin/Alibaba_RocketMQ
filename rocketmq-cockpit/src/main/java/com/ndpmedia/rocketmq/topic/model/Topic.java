package com.ndpmedia.rocketmq.topic.model;

/**
 * topic bean.
 */
public class Topic {
    public static int DefaultReadQueueNums = 16;
    public static int DefaultWriteQueueNums = 16;

    private int id;
    private String topic;
    private String cluster_name;
    private String broker_address;
    private int write_queue_num = DefaultWriteQueueNums;
    private int read_queue_num = DefaultReadQueueNums;
    private long create_time;
    private long update_time;
    private boolean order;
    private boolean allow;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCluster_name() {
        return cluster_name;
    }

    public void setCluster_name(String cluster_name) {
        this.cluster_name = cluster_name;
    }

    public String getBroker_address() {
        return broker_address;
    }

    public void setBroker_address(String broker_address) {
        this.broker_address = broker_address;
    }

    public int getWrite_queue_num() {
        return write_queue_num;
    }

    public void setWrite_queue_num(int write_queue_num) {
        this.write_queue_num = write_queue_num;
    }

    public int getRead_queue_num() {
        return read_queue_num;
    }

    public void setRead_queue_num(int read_queue_num) {
        this.read_queue_num = read_queue_num;
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

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }

    public boolean isAllow() {
        return allow;
    }

    public void setAllow(boolean allow) {
        this.allow = allow;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "topic='" + topic + '\'' +
                ", cluster_name='" + cluster_name + '\'' +
                ", broker_address='" + broker_address + '\'' +
                ", write_queue_num=" + write_queue_num +
                ", read_queue_num=" + read_queue_num +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                ", order=" + order +
                '}';
    }
}
