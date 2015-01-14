import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

import java.io.IOException;

public class HelloClient {
    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "3210"));
    public static void main(String[] args) throws TException, IOException, InterruptedException {
        TNonblockingTransport transport = new TNonblockingSocket("localhost", PORT);
        transport.startConnect();
        while (!transport.finishConnect()) {
            Thread.sleep(100);
        }
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Hello.Client client = new Hello.Client(protocol);
        client.echo("Msg");
        transport.close();
    }
}
