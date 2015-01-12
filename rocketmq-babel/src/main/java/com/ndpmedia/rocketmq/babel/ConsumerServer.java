package com.ndpmedia.rocketmq.babel;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

public class ConsumerServer {

    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQConsumerPort", "3211"));

    public static void main(String[] args) {

        TServer server = null;

        try {
            TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
            Consumer.AsyncProcessor processor = new Consumer.AsyncProcessor<ConsumerService>(new ConsumerService());
            TThreadedSelectorServer.Args serverArgs =
                    new TThreadedSelectorServer.Args(new TNonblockingServerSocket(PORT))
                            .protocolFactory(protocolFactory)
                            .processor(processor);

            server = new TThreadedSelectorServer(serverArgs);
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

}
