package com.ndpmedia.rocketmq.lsr.paxos.recovery;

import static com.ndpmedia.rocketmq.lsr.common.ProcessDescriptor.processDescriptor;

import java.io.IOException;

import com.ndpmedia.rocketmq.lsr.paxos.SnapshotProvider;
import com.ndpmedia.rocketmq.lsr.paxos.core.Paxos;
import com.ndpmedia.rocketmq.lsr.paxos.storage.InMemoryStorage;
import com.ndpmedia.rocketmq.lsr.paxos.storage.Storage;

public class CrashStopRecovery extends RecoveryAlgorithm {

    private final Paxos paxos;

    public CrashStopRecovery(SnapshotProvider snapshotProvider) throws IOException {

        Storage storage = new InMemoryStorage();
        if (processDescriptor.isLocalProcessLeader(storage.getView())) {
            storage.setView(storage.getView() + 1);
        }

        paxos = new Paxos(snapshotProvider, storage);
    }

    public void start() throws IOException {
        fireRecoveryFinished();
    }

    public Paxos getPaxos() {
        return paxos;
    }
}
