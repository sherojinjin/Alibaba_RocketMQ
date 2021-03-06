package com.ndpmedia.rocketmq.lsr.paxos.core;

import static com.ndpmedia.rocketmq.lsr.common.ProcessDescriptor.processDescriptor;

import java.util.Deque;

import com.ndpmedia.rocketmq.lsr.paxos.UnBatcher;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Accept;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Prepare;
import com.ndpmedia.rocketmq.lsr.paxos.messages.PrepareOK;
import com.ndpmedia.rocketmq.lsr.paxos.messages.Propose;
import com.ndpmedia.rocketmq.lsr.paxos.network.Network;
import com.ndpmedia.rocketmq.lsr.paxos.replica.ClientBatchID;
import com.ndpmedia.rocketmq.lsr.paxos.replica.ClientBatchManager;
import com.ndpmedia.rocketmq.lsr.paxos.replica.ClientBatchManager.FwdBatchRetransmitter;
import com.ndpmedia.rocketmq.lsr.paxos.storage.ClientBatchStore;
import com.ndpmedia.rocketmq.lsr.paxos.storage.ConsensusInstance;
import com.ndpmedia.rocketmq.lsr.paxos.storage.ConsensusInstance.LogEntryState;
import com.ndpmedia.rocketmq.lsr.paxos.storage.Log;
import com.ndpmedia.rocketmq.lsr.paxos.storage.Storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents part of paxos which is responsible for responding on the
 * <code>Prepare</code> message, and also sending <code>Accept</code> after
 * receiving proper <code>Propose</code>.
 */
class Acceptor {
    private final Paxos paxos;
    private final Storage storage;
    private final Network network;

    /**
     * Initializes new instance of <code>Acceptor</code>.
     * 
     * @param paxos - the paxos the acceptor belong to
     * @param storage - data associated with the paxos
     * @param network - used to send responses
     * 
     */
    public Acceptor(Paxos paxos, Storage storage, Network network) {
        this.paxos = paxos;
        this.storage = storage;
        this.network = network;
    }

    /**
     * Promises not to accept a proposal numbered less than message view. Sends
     * the proposal with the highest number less than message view that it has
     * accepted if any. If message view equals current view, then it may be a
     * retransmission or out-of-order delivery. If the process already accepted
     * this proposal, then the proposer doesn't need anymore the prepareOK
     * message. Otherwise it might need the message, so resent it.
     * 
     * @param msg received prepare message
     * @see Prepare
     */
    public void onPrepare(Prepare msg, int sender) {
        assert paxos.getDispatcher().amIInDispatcher() : "Thread should not be here: " +
                                                         Thread.currentThread();

        if (!paxos.isActive())
            return;

        // TODO: JK: When can we skip responding to a prepare message?
        // Is detecting stale prepare messages it worth it?

        logger.info("{} From {}", msg, sender);

        Log log = storage.getLog();

        if (msg.getFirstUncommitted() < log.getLowestAvailableId()) {
            // We're MUCH MORE up-to-date than the replica that sent Prepare
            paxos.startProposer();
            return;
        }

        ConsensusInstance[] v = new ConsensusInstance[Math.max(
                log.getNextId() - msg.getFirstUncommitted(), 0)];
        for (int i = msg.getFirstUncommitted(); i < log.getNextId(); i++) {
            v[i - msg.getFirstUncommitted()] = log.getInstance(i);
        }

        PrepareOK m = new PrepareOK(msg.getView(), v, storage.getEpoch());
        logger.info("Sending {}", m);

        network.sendMessage(m, sender);
    }

    /**
     * Accepts proposals higher or equal than the current view.
     * 
     * @param message - received propose message
     * @param sender - the id of replica that send the message
     */
    public void onPropose(final Propose message, final int sender) {
        assert message.getView() == storage.getView() : "Msg.view: " + message.getView() +
                                                        ", view: " + storage.getView();
        assert paxos.getDispatcher().amIInDispatcher();

        ConsensusInstance instance = storage.getLog().getInstance(message.getInstanceId());
        // The propose is so old, that it's log has already been erased
        if (instance == null) {
            logger.debug("Ignoring old message: {}", message);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("onPropose. View:instance: {}:{}", message.getView(),
                    message.getInstanceId());
        }

        Deque<ClientBatchID> cbids = null;
        if (processDescriptor.indirectConsensus) {
            cbids = UnBatcher.unpackCBID(message.getValue());

            // leader must have the values
            if (!paxos.isLeader()) {

                // as follower, we may be missing the real value. If so, need to
                // wait for it.

                if (!ClientBatchStore.instance.hasAllBatches(cbids)) {
                    logger.debug("Missing batch values for instance {}. Delaying onPropose.",
                            instance.getId());
                    FwdBatchRetransmitter fbr = ClientBatchStore.instance.getClientBatchManager().fetchMissingBatches(
                            cbids,
                            new ClientBatchManager.Hook() {

                                @Override
                                public void hook() {
                                    paxos.getDispatcher().execute(new Runnable() {
                                        public void run() {
                                            onPropose(message, sender);
                                        }
                                    });
                                }
                            }, false);
                    instance.setFwdBatchForwarder(fbr);

                    return;
                }
            }
        }

        // In FullSS, updating state leads to setting new value if needed, which
        // syncs to disk
        instance.updateStateFromKnown(message.getView(), message.getValue());

        if (processDescriptor.indirectConsensus) {
            // prevent multiple unpacking
            instance.setClientBatchIds(cbids);
        }

        assert instance.getValue() != null;

        // leader will not send the accept message;
        if (!paxos.isLeader()) {

            if (storage.getFirstUncommitted() + (processDescriptor.windowSize * 3) < message.getInstanceId()) {
                // the instance is so new that we must be out of date.
                paxos.getCatchup().forceCatchup();
            }

            if (paxos.isActive())
                network.sendToOthers(new Accept(message));
        }

        // we could have decided the instance earlier
        if (instance.getState() == LogEntryState.DECIDED) {
            logger.trace("Instance already decided: {}", message.getInstanceId());
            return;
        }

        // The local process accepts immediately the proposal
        instance.getAccepts().set(processDescriptor.localId);
        // The propose message works as an implicit accept from the leader.
        instance.getAccepts().set(sender);

        // Check if we can decide (n<=3 or if some accepts overtook propose)
        if (instance.isMajority()) {
            paxos.decide(instance.getId());
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(Acceptor.class);
}
