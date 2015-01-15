package com.ndpmedia.rocketmq.babel.example;

import com.ndpmedia.rocketmq.babel.Message;
import com.ndpmedia.rocketmq.babel.Producer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ProducerClient {
    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "10921"));

    public static void main(String[] args) throws TException {
        TTransport transport = null;
        try {
            transport = new TSocket("localhost", PORT);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            Producer.Client client = new Producer.Client(protocol);
            Message message = new Message();
            message.setTopic("T_QuickStart");
            message.setData("Test".getBytes());
            client.send(message);
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
}
