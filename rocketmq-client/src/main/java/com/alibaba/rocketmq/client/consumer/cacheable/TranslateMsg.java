package com.alibaba.rocketmq.client.consumer.cacheable;

import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;


public class TranslateMsg {
    public static Message getMessageFromMessageExt(MessageExt messageExt) {
        Message result = new Message(messageExt.getTopic(), messageExt.getTags(), messageExt.getKeys(),
                messageExt.getBody());
        result.putUserProperty("msgId", messageExt.getMsgId());
        result.putUserProperty("queueId", messageExt.getQueueId() + "");
        result.putUserProperty("storeSize", messageExt.getStoreSize() + "");
        result.putUserProperty("queueOffset", messageExt.getQueueOffset() + "");
        result.putUserProperty("sysFlag", messageExt.getSysFlag() + "");
        result.putUserProperty("bornTimestamp", messageExt.getBornTimestamp() + "");
        result.putUserProperty("storeTimestamp", messageExt.getStoreTimestamp() + "");
        result.putUserProperty("commitLogOffset", messageExt.getCommitLogOffset() + "");
        result.putUserProperty("bodyCRC", messageExt.getBodyCRC() + "");
        result.putUserProperty("reconsumeTimes", messageExt.getReconsumeTimes() + "");
        result.putUserProperty("preparedTransactionOffset", messageExt.getPreparedTransactionOffset() + "");

        InetSocketAddress bornHost = (InetSocketAddress) messageExt.getBornHost();
        result.putUserProperty("bornHost_hostName", bornHost.getHostName());
        result.putUserProperty("bornHost_port", bornHost.getPort() + "");
        InetAddress born = bornHost.getAddress();
        result.putUserProperty("bornHost_hostAddress", born.getHostAddress());
        result.putUserProperty("bornHost_hostAddressName", born.getHostName());
        result.putUserProperty("bornHost_hostAddressByte", Arrays.toString(born.getAddress()));

        InetSocketAddress storeHost = (InetSocketAddress) messageExt.getStoreHost();
        result.putUserProperty("storeHost_hostName", storeHost.getHostName());
        result.putUserProperty("storeHost_port", storeHost.getPort() + "");
        InetAddress store = storeHost.getAddress();
        result.putUserProperty("storeHost_hostAddress", store.getHostAddress());
        result.putUserProperty("storeHost_hostAddressName", store.getHostName());
        result.putUserProperty("storeHost_hostAddressByte", Arrays.toString(store.getAddress()));

        return result;
    }

    public static MessageExt getMessageExtFromMessage(Message message) {
        InetSocketAddress bornHost = null;
        InetSocketAddress storeHost = null;
        try {
            InetAddress born = InetAddress.getByAddress(message.getProperty("bornHost_hostAddressName"),
                    getByteFromString(message.getProperty("bornHost_hostAddressByte")));
            InetAddress store = InetAddress.getByAddress(message.getProperty("storeHost_hostAddressName"),
                    getByteFromString(message.getProperty("storeHost_hostAddressByte")));
            bornHost = new InetSocketAddress(born, Integer.parseInt(message.getProperty("bornHost_port")));
            storeHost = new InetSocketAddress(store, Integer.parseInt(message.getProperty("storeHost_port")));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" try to change message to messageExt failed! ");
        }

        MessageExt result = new MessageExt(Integer.parseInt(message.getProperty("queueId")),
                Long.parseLong(message.getProperty("bornTimestamp")),
                bornHost,
                Long.parseLong(message.getProperty("storeTimestamp")),
                storeHost,
                message.getProperty("msgId"));

        result.setTopic(message.getTopic());
        result.setTopic(message.getTags());
        result.setKeys(message.getKeys());
        result.setBody(message.getBody());
        result.setMsgId(message.getProperty("msgId"));
        result.setStoreSize(Integer.parseInt(message.getProperty("storeSize")));
        result.setQueueOffset(Long.parseLong(message.getProperty("queueOffset")));
        result.setSysFlag(Integer.parseInt(message.getProperty("sysFlag")));
        result.setCommitLogOffset(Long.parseLong(message.getProperty("commitLogOffset")));
        result.setBodyCRC(Integer.parseInt(message.getProperty("bodyCRC")));
        result.setReconsumeTimes(Integer.parseInt(message.getProperty("reconsumeTimes")));
        result.setPreparedTransactionOffset(Long.parseLong(message.getProperty("preparedTransactionOffset")));

        return result;
    }

    private static byte[] getByteFromString(String address) {
        String[] strings = address.substring(1, address.lastIndexOf("]")).split(",");
        byte[] result = new byte[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = Byte.parseByte(strings[i].trim());
        }
        return result;
    }
}
