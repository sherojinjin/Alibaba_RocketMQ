package com.ndpmedia.rocketmq.lsr.paxos.recovery;

import static com.ndpmedia.rocketmq.lsr.common.ProcessDescriptor.processDescriptor;

import java.io.IOException;

import com.ndpmedia.rocketmq.lsr.paxos.SnapshotProvider;
import com.ndpmedia.rocketmq.lsr.paxos.core.Paxos;
import com.ndpmedia.rocketmq.lsr.paxos.storage.FullSSDiscWriter;
import com.ndpmedia.rocketmq.lsr.paxos.storage.SingleNumberWriter;
import com.ndpmedia.rocketmq.lsr.paxos.storage.Storage;
import com.ndpmedia.rocketmq.lsr.paxos.storage.SynchronousStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullSSRecovery extends RecoveryAlgorithm {
    private final String logPath;
    private Paxos paxos;

    public FullSSRecovery(SnapshotProvider snapshotProvider, String logPath)
            throws IOException
    {
        this.logPath = logPath;
        Storage storage = createStorage();
        paxos = new Paxos(snapshotProvider, storage);

    }

    public void start() throws IOException {
        fireRecoveryFinished();
    }

    private Storage createStorage() throws IOException {

        logger.info("Reading log from: {}", logPath);
        FullSSDiscWriter writer = new FullSSDiscWriter(logPath);

        Storage storage = new SynchronousStorage(writer);

        // Client batches and ViewEpochIdGenerator use epoch in FullSS
        SingleNumberWriter epochFile = new SingleNumberWriter(logPath, "sync.epoch");
        storage.setEpoch(new long[] {epochFile.readNumber() + 1});
        epochFile.writeNumber(storage.getEpoch()[0]);

        if (processDescriptor.isLocalProcessLeader(storage.getView())) {
            storage.setView(storage.getView() + 1);
        }
        return storage;
    }

    @Override
    public Paxos getPaxos() {
        return paxos;
    }

    private final static Logger logger = LoggerFactory.getLogger(FullSSRecovery.class);
}
