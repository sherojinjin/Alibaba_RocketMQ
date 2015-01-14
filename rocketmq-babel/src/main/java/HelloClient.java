import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;

public class HelloClient {
    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "3210"));
    public static void main(String[] args) throws TException, IOException, InterruptedException {
        TTransport transport = new TSocket("localhost", PORT);
        transport.open();
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Hello.Client client = new Hello.Client(protocol);
        client.echo("Msg");
        transport.close();
    }
}
