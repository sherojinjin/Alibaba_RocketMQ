import com.alibaba.rocketmq.client.log.ClientLogger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;

import java.io.IOException;

public class HelloSimpleServer {

    private static final Logger LOGGER = ClientLogger.getLog();

    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "3210"));

    public static void main(String[] args) throws IOException {
        TServer server = null;
        try {
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            Hello.Processor processor = new Hello.Processor<Hello.Iface>(new Hello.Iface() {

                @Override
                public String echo(String msg) throws TException {
                    System.out.println("OK: " + msg);
                    return msg;
                }
            });
            TSimpleServer.Args serverArgs =
                    new TSimpleServer.Args(new TServerSocket(PORT))
                            .protocolFactory(protocolFactory)
                            .processor(processor);

            server = new TSimpleServer(serverArgs);
            LOGGER.info("Thrift Server starts. Port: " + PORT);
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }

    }
}
