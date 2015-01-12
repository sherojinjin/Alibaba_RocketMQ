package com.ndpmedia.rocketmq.babel.example;

import com.ndpmedia.rocketmq.babel.Consumer;
import com.ndpmedia.rocketmq.babel.MessageModel;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

import java.io.IOException;

public class ConsumerClient {
    public static void main(String[] args) throws TException, IOException {
        TNonblockingTransport transport = new TNonblockingSocket("localhost", 3211);
        transport.open();
        TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
        TAsyncClientManager clientManager = new TAsyncClientManager();
        Consumer.AsyncClient client = new Consumer.AsyncClient(protocolFactory, clientManager, transport);
        client.setConsumerGroup("CG_QuickStart", new AsyncMethodCallbackWrapper());
        client.setMessageModel(MessageModel.CLUSTERING, new AsyncMethodCallbackWrapper());
        client.registerTopic("T_QuickStart", "*", new AsyncMethodCallbackWrapper());
        client.start(new AsyncMethodCallbackWrapper());

        client.pull(new AsyncMethodCallback() {
            @Override
            public void onComplete(Object response) {

            }

            @Override
            public void onError(Exception exception) {

            }
        });
    }
}

class AsyncMethodCallbackWrapper implements AsyncMethodCallback {

    /**
     * This method will be called when the remote side has completed invoking
     * your method call and the result is fully read. For oneway method calls,
     * this method will be called as soon as we have completed writing out the
     * request.
     *
     * @param response
     */
    @Override
    public void onComplete(Object response) {

    }

    /**
     * This method will be called when there is an unexpected clientside
     * exception. This does not include application-defined exceptions that
     * appear in the IDL, but rather things like IOExceptions.
     *
     * @param exception
     */
    @Override
    public void onError(Exception exception) {

    }
}
