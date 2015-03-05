package com.ndpmedia.rocketmq.lsr.paxos.replica;

import com.ndpmedia.rocketmq.lsr.common.ClientReply;

/**
 * Handles the reply produced by the Service, passing it to client
 */
public interface ClientProxy {
    /** Called upon generating the answer for previous request */
    void send(ClientReply clientReply);
}
