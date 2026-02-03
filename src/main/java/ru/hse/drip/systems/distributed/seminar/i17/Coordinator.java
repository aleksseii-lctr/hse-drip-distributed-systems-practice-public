package ru.hse.drip.systems.distributed.seminar.i17;

import java.io.IOException;
import java.util.*;

/**
 * TODO (students): implement 2PC coordinator.
 * Requirements (minimum for lab):
 * 1) Phase 1: send PREPARE(txId, op) to every participant and collect votes.
 *    - Use retries on RpcTimeoutException.
 *    - If any participant votes ABORT or times out after retries -> global decision ABORT.
 * 2) Persist:
 *    - Log BEGIN with the plan BEFORE starting prepare phase (already done for you).
 *    - Log DECISION (COMMIT/ABORT) BEFORE broadcasting it (WAL idea).
 * 3) Phase 2:
 *    - Broadcast COMMIT or ABORT to all participants (with retries).
 * 4) Implement recoverAndFinish():
 *    - Read log, find tx with DECISION, re-broadcast decision (idempotency should make it safe).
 */
public class Coordinator {
    private final String id;
    private final DurableLog log;
    private final UnreliableNetwork net;

    private final int maxRpcAttempts;
    private final int maxDecisionBroadcastAttempts;

    public Coordinator(String id, DurableLog log, UnreliableNetwork net,
                       int maxRpcAttempts,
                       int maxDecisionBroadcastAttempts) {
        this.id = id;
        this.log = log;
        this.net = net;
        this.maxRpcAttempts = maxRpcAttempts;
        this.maxDecisionBroadcastAttempts = maxDecisionBroadcastAttempts;
    }

    public String id() {
        return id;
    }

    public Decision run(TxPlan plan) throws IOException {
        TxId txId = TxId.newId();
        logBegin(txId, plan);

        // TODO: Phase 1 (prepare + votes)
        // Hints:
        // - Keep Map<String, Vote> votes
        // - For each participant, call net.rpcPrepare(id, pId, txId, op) with retries
        // - Decide COMMIT only if all votes are COMMIT

        throw new UnsupportedOperationException("TODO: implement Coordinator.run()");
    }

    public void recoverAndFinish() throws IOException {
        // TODO: parse log and re-broadcast decisions
        throw new UnsupportedOperationException("TODO: implement Coordinator.recoverAndFinish()");
    }

    // ---- helpers you will likely need ----

    private void logBegin(TxId txId, TxPlan plan) throws IOException {
        log.append(String.format("%s|BEGIN|%s", txId.value(), CoordinatorRecovery.serializePlan(plan)));
    }

    private void logDecision(TxId txId, Decision decision) throws IOException {
        log.append(String.format("%s|DECISION|%s", txId.value(), decision.name()));
    }

    // Suggested helper (optional): prepare with retries
    private Vote requestPrepareWithRetries(TxId txId, String participantId, String op) {
        for (int attempt = 1; attempt <= maxRpcAttempts; attempt++) {
            try {
                return net.rpcPrepare(id, participantId, txId, op);
            } catch (RpcTimeoutException e) {
                // retry
            } catch (Exception e) {
                return Vote.ABORT;
            }
        }
        return Vote.ABORT;
    }

    // Suggested helper (optional): broadcast commit/abort with retries
    private void broadcastCommitWithRetries(TxId txId, TxPlan plan) {
        for (int attempt = 1; attempt <= maxDecisionBroadcastAttempts; attempt++) {
            boolean allOk = true;
            for (String pId : plan.participantOps().keySet()) {
                try {
                    net.rpcCommit(id, pId, txId);
                } catch (RpcTimeoutException e) {
                    allOk = false;
                }
            }
            if (allOk) return;
        }
    }

    private void broadcastAbortWithRetries(TxId txId, TxPlan plan) {
        for (int attempt = 1; attempt <= maxDecisionBroadcastAttempts; attempt++) {
            boolean allOk = true;
            for (String pId : plan.participantOps().keySet()) {
                try {
                    net.rpcAbort(id, pId, txId);
                } catch (RpcTimeoutException e) {
                    allOk = false;
                }
            }
            if (allOk) return;
        }
    }
}
