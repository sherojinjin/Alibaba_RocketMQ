package com.ndpmedia.rocketmq.put.consensus.listeners;

public interface RecoveryListener {
    void recoverFromCommit(Object commitData);

    void recoveryFinished();
}
