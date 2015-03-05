package com.ndpmedia.rocketmq.put.consensus;

public interface ConsensusDelegateProposer {
    void propose(Object obj);

    void dispose();
}
