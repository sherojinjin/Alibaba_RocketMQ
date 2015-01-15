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
            message.setTopic("T_PARSER");
            int i = 0;
            while (true) {
                message.setData(("Test" + i).getBytes());
                System.out.println("producerClient send msg:"+"Test"+i);
                client.send(message);
                i++;
                Thread.sleep(1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (null != transport) {
                transport.close();
            }
        }
    }
}
