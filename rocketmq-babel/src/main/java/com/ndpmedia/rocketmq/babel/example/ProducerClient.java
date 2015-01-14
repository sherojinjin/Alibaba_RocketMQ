package com.ndpmedia.rocketmq.babel.example;

import com.ndpmedia.rocketmq.babel.Message;
import com.ndpmedia.rocketmq.babel.Producer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

import java.io.IOException;

public class ProducerClient {
    public static void main(String[] args) throws TException {
        TNonblockingTransport transport = null;
        try {
            transport = new TNonblockingSocket("localhost", 3210);
            transport.startConnect();


            while (!transport.finishConnect()) {
                Thread.sleep(100);
            }

            TProtocol protocol = new TCompactProtocol(transport);
            Producer.Client client = new Producer.Client(protocol);
            Message message = new Message();
            message.setTopic("T_QuickStart");
            message.setData("Test".getBytes());
            client.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
}
