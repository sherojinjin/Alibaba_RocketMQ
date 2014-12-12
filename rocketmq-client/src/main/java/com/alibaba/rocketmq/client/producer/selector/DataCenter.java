package com.alibaba.rocketmq.client.producer.selector;

public enum  DataCenter {

    SA("SA", 1), USE("USE", 2), USW("USW", 3), SG("SG", 4);

    private String name;

    private int index;

    private DataCenter(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getName(int index) {
        for (DataCenter dataCenter : DataCenter.values()) {
            if (dataCenter.index == index) {
                return dataCenter.name;
            }
        }
        return null;
    }
}
