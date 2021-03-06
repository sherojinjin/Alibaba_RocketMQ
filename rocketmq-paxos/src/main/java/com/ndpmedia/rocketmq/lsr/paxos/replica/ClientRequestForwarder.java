package com.ndpmedia.rocketmq.lsr.paxos.replica;

import static com.ndpmedia.rocketmq.lsr.common.ProcessDescriptor.processDescriptor;

import java.util.BitSet;

import com.ndpmedia.rocketmq.lsr.common.ClientRequest;
import com.ndpmedia.rocketmq.lsr.paxos.core.Paxos;
import com.ndpmedia.rocketmq.lsr.paxos.messages.ForwardClientRequests;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Message;
import com.ndpmedia.rocketmq.lsr.paxos.messages.MessageType;
import com.ndpmedia.rocketmq.lsr.paxos.network.MessageHandler;
import com.ndpmedia.rocketmq.lsr.paxos.network.Network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRequestForwarder {

    private final Paxos paxos;
    private final Network network;
    private ClientRequestManager clientRequestManager = null;

    public ClientRequestForwarder(Paxos paxos) {
        this.paxos = paxos;
        this.network = paxos.getNetwork();
    }

    public void setClientRequestManager(ClientRequestManager clientRequestManager) {
        this.clientRequestManager = clientRequestManager;
    }

    void forward(ClientRequest[] requests) {
        // The object that will be sent.
        ForwardClientRequests fReqMsg = new ForwardClientRequests(requests);

        logger.debug("Forwarding requests: {}", fReqMsg);

        int leaderId = paxos.getLeaderId();
        if (processDescriptor.localId == leaderId) {
            // wtfu?
            if (clientRequestManager != null)
                clientRequestManager.dispatchOnClientRequest(requests, null);
        } else
            network.sendMessage(fReqMsg, leaderId);
    }

    public void start() {
        Network.addMessageListener(MessageType.ForwardedClientRequests, new MessageHandler() {

            public void onMessageSent(Message message, BitSet destinations) {
                assert false;
            }

            public void onMessageReceived(Message msg, int sender) {
                assert msg instanceof ForwardClientRequests;
                ForwardClientRequests fcr = (ForwardClientRequests) msg;
                ClientRequest[] requests = fcr.getRequests();
                if (clientRequestManager != null)
                    clientRequestManager.dispatchOnClientRequest(requests, null);
            }
        });
    }

    static final Logger logger = LoggerFactory.getLogger(ClientRequestForwarder.class);
}
