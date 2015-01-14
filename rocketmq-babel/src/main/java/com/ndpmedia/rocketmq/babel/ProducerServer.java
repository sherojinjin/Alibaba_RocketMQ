package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.log.ClientLogger;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import java.io.IOException;

public class ProducerServer {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "3210"));

    public static void main(String[] args) throws IOException {
        TServer server = null;
        try {
            TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
            Producer.Processor processor = new Producer.Processor<ProducerService>(new ProducerService());
            TThreadedSelectorServer.Args serverArgs =
                    new TThreadedSelectorServer.Args(new TNonblockingServerSocket(PORT))
                            .protocolFactory(protocolFactory)
                            .processor(processor);

            server = new TThreadedSelectorServer(serverArgs);
            server.serve();
            LOGGER.info("Thrift Server starts.");
        } catch (TTransportException e) {
            e.printStackTrace();
        }

    }
}
