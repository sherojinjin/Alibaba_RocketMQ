package com.alibaba.rocketmq.client.producer.selector;

public class DataCenterLocator {

    private static final String USE_IP_PATTERN = "10.1";
    private static final String SG_IP_PATTERN = "10.2";
    private static final String USW_IP_PATTERN = "10.3";
    private static final String SA_IP_PATTERN = "10.5";
    private static final String LONDON_IP_PATTERN = "10.11";

    //10.1 USE
    //10.2 SG
    //10.3 USW
    //10.5 SA
    //10.11 London
    public static DataCenter locate(String ip) {
        if (null == ip) {
            throw new IllegalArgumentException("IP cannot be null");
        }

        if (ip.startsWith(USE_IP_PATTERN)) {
            return DataCenter.USE;
        }

        if (ip.startsWith(SG_IP_PATTERN)) {
            return DataCenter.SG;
        }

        if (ip.startsWith(USW_IP_PATTERN)) {
            return DataCenter.USW;
        }

        if (ip.startsWith(SA_IP_PATTERN)) {
            return DataCenter.SA;
        }

        return null;
    }

}
