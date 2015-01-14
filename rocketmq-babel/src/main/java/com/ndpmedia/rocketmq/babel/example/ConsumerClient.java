package com.ndpmedia.rocketmq.babel.example;

import com.ndpmedia.rocketmq.babel.Consumer;
import com.ndpmedia.rocketmq.babel.MessageExt;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;
import java.util.List;

public class ConsumerClient {
    public static void main(String[] args) throws TException, IOException, InterruptedException {
        TTransport transport = new TSocket("localhost", 3211);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        Consumer.Client client = new Consumer.Client(protocol);

        List<MessageExt> messageList = client.pull();
        while (!messageList.isEmpty()) {
            for (MessageExt msg : messageList) {
                System.out.println(msg.getMsgId());
            }
        }
    }
}