package com.ndpmedia.rocketmq.babel.example;

import com.ndpmedia.rocketmq.babel.Message;
import com.ndpmedia.rocketmq.babel.Producer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ProducerClient {
    public static void main(String[] args) throws TException {
        TTransport transport = new TSocket("localhost", 3210);
        transport.open();

        TProtocol protocol = new TCompactProtocol(transport);
        Producer.Client client = new Producer.Client(protocol);
        Message message = new Message();
        message.setTopic("T_QuickStart");
        message.setData("Test".getBytes());
        client.send(message);
    }
}
