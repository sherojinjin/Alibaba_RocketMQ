package com.ndpmedia.rocketmq.babel;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import java.io.IOException;

public class ConsumerServer {
    private static final Logger LOGGER = ClientLogger.getLog();
    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQConsumerPort", "10922"));

    public static void main(String[] args) throws IOException {
        TServer server = null;
        try {
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            Consumer.Processor processor = new Consumer.Processor<ConsumerService>(new ConsumerService());
            TThreadPoolServer.Args serverArgs =
                    new TThreadPoolServer.Args(new TServerSocket(PORT))
                            .protocolFactory(protocolFactory)
                            .processor(processor);
            server = new TThreadPoolServer(serverArgs);
            server.serve();
        } catch (TTransportException e) {
            LOGGER.error("Client Thrift Server got an error", e);
        } catch (InterruptedException e) {
            LOGGER.error("Client Thrift Server got an error", e);
        } catch (MQClientException e) {
            LOGGER.error("Client Thrift Server got an error", e);
        } finally {
            if (null != server) {
                server.stop();
            }
        }
    }

}
