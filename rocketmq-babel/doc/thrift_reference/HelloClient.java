import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;

public class HelloClient {
    private static final int PORT = Integer.parseInt(System.getProperty("RocketMQProducerPort", "3210"));
    public static void main(String[] args) throws TException, IOException, InterruptedException {
        TTransport transport = new TSocket("localhost", PORT);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        Hello.Client client = new Hello.Client(protocol);
        for (int i = 0; i < 10; i++) {
            String msg = client.echo("Msg: " + i);
            System.out.println(msg);
        }
        transport.close();
    }
}
