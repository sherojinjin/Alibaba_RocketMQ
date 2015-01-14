package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.exception.MQClientException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

public class ConsumerServer {

    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQConsumerPort", "3211"));

    public static void main(String[] args) {

        TServer server = null;

        try {
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            Consumer.AsyncProcessor processor = new Consumer.AsyncProcessor<ConsumerService>(new ConsumerService());
            TThreadPoolServer.Args serverArgs =
                    new TThreadPoolServer.Args(new TNonblockingServerSocket(PORT))
                            .protocolFactory(protocolFactory)
                            .processor(processor);

            server = new TThreadPoolServer(serverArgs);
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

}
