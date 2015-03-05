package com.ndpmedia.rocketmq.put.consensus;

import com.ndpmedia.rocketmq.put.consensus.listeners.CommitListener;
import com.ndpmedia.rocketmq.put.consensus.listeners.RecoveryListener;

public interface Commitable {
    void commit(Object commitData);

    boolean addCommitListener(CommitListener listener);

    boolean removeCommitListener(CommitListener listener);

    boolean addRecoveryListener(RecoveryListener listener);

    boolean removeRecoveryListener(RecoveryListener listener);
}
