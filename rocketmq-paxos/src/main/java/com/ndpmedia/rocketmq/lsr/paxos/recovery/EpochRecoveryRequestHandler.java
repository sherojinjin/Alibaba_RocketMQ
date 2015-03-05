package com.ndpmedia.rocketmq.lsr.paxos.recovery;

import static com.ndpmedia.rocketmq.lsr.common.ProcessDescriptor.processDescriptor;

import java.util.BitSet;

import com.ndpmedia.rocketmq.lsr.paxos.core.Paxos;
import com.ndpmedia.rocketmq.lsr.paxos.core.Proposer;
import com.ndpmedia.rocketmq.lsr.paxos.core.Proposer.ProposerState;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Message;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Recovery;
import com.ndpmedia.rocketmq.lsr.paxos.messages.RecoveryAnswer;
import com.ndpmedia.rocketmq.lsr.paxos.network.MessageHandler;
import com.ndpmedia.rocketmq.lsr.paxos.storage.Storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpochRecoveryRequestHandler implements MessageHandler {
    private final Paxos paxos;

    public EpochRecoveryRequestHandler(Paxos paxos) {
        this.paxos = paxos;
    }

    public void onMessageReceived(Message message, final int sender) {
        final Recovery recovery = (Recovery) message;

        paxos.getDispatcher().submit(new Runnable() {
            public void run() {
                Storage storage = paxos.getStorage();
                if (storage.getEpoch()[sender] > recovery.getEpoch()) {
                    logger.info("Got stale recovery message from {} ({})", sender, recovery);
                    return;
                }

                if (paxos.getLeaderId() == sender) {
                    // if current leader is recovering, we cannot respond
                    // and we should change a leader

                    logger.info(processDescriptor.logMark_Benchmark,
                            "Delaying receive {} (view change forced)", recovery);

                    paxos.getProposer().prepareNextView();
                    onMessageReceived(recovery, sender);
                    return;
                }

                logger.info(processDescriptor.logMark_Benchmark, "Received {}", recovery);

                if (paxos.isLeader() && paxos.getProposer().getState() == ProposerState.PREPARING) {
                    paxos.getProposer().executeOnPrepared(new Proposer.Task() {

                        public void onPrepared() {
                            onMessageReceived(recovery, sender);
                        }

                        public void onFailedToPrepare() {
                            onMessageReceived(recovery, sender);
                        }
                    });
                    return;
                }

                storage.updateEpoch(recovery.getEpoch(), sender);
                RecoveryAnswer answer = new RecoveryAnswer(storage.getView(),
                        storage.getEpoch(),
                        storage.getLog().getNextId());
                paxos.getNetwork().sendMessage(answer, sender);
            }
        });
    }

    public void onMessageSent(Message message, BitSet destinations) {
    }

    private final static Logger logger = LoggerFactory.getLogger(EpochRecoveryRequestHandler.class);
}
