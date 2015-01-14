package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.exception.MQClientException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
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
                            .transportFactory(new TFramedTransport.Factory())
                            .protocolFactory(protocolFactory)
                            .processor(processor);

            serverArgs.workerThreads(2);
            server = new TThreadedSelectorServer(serverArgs);
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
