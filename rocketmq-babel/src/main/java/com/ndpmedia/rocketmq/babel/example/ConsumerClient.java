package com.ndpmedia.rocketmq.babel.example;

import com.ndpmedia.rocketmq.babel.Consumer;
import com.ndpmedia.rocketmq.babel.MessageExt;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

import java.io.IOException;
import java.util.List;

public class ConsumerClient {
    public static void main(String[] args) throws TException, IOException {
        TNonblockingTransport transport = new TNonblockingSocket("localhost", 3211);
        transport.open();
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        TAsyncClientManager clientManager = new TAsyncClientManager();
        Consumer.AsyncClient client = new Consumer.AsyncClient(protocolFactory, clientManager, transport);
        client.pull(new AsyncMethodCallback() {
            @Override
            public void onComplete(Object response) {
                if (response instanceof List) {
                    List<MessageExt> messageList = (List<MessageExt>)response;
                    for (MessageExt msg : messageList) {
                        System.out.println(msg.getTopic());
                    }
                }
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}