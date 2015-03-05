package com.ndpmedia.rocketmq.put.consensus.listeners;

public interface CommitListener {
    void onCommit(final Object commitData);
}
