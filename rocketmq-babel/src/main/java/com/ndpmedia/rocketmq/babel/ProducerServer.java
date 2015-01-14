package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.log.ClientLogger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import java.io.IOException;

public class ProducerServer {
    private static final Logger LOGGER = ClientLogger.getLog();
    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "10921"));

    public static void main(String[] args) throws IOException {
        TServer server = null;
        try {
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            Producer.Processor processor = new Producer.Processor<ProducerService>(new ProducerService());
            TThreadPoolServer.Args serverArgs =
                    new TThreadPoolServer.Args(new TServerSocket(PORT))
                            .protocolFactory(protocolFactory)
                            .processor(processor);

            server = new TThreadPoolServer(serverArgs);
            LOGGER.info("Thrift Server starts. Port: " + PORT);
            server.serve();
        } catch (TTransportException e) {
            LOGGER.error("Producer Thrift Server got an error", e);
        }  finally {
            if (null != server) {
                server.stop();
            }
        }

    }
}
