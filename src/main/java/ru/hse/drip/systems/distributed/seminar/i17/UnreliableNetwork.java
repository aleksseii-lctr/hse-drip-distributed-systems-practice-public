package ru.hse.drip.systems.distributed.seminar.i17;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A tiny unreliable RPC layer:
 * - can drop first N messages of a given type to a given node
 * - can duplicate messages with probability
 * - can simulate partitions (block from->to)
 * - can crash a node on first delivery of a given message type (e.g. crash on COMMIT)
 *
 * We keep it synchronous to make the lab doable in 90 minutes.
 */
public class UnreliableNetwork {
    public enum MsgType { PREPARE, COMMIT, ABORT }

    private final Random rnd;
    private final Map<String, ParticipantServer> participants = new HashMap<>();

    // (type|toId) -> remaining drops
    private final Map<String, Integer> dropFirst = new HashMap<>();

    // (type|toId) -> crash on first such message?
    private final Map<String, Boolean> crashOnFirst = new HashMap<>();
    private final Map<String, Boolean> crashAlreadyFired = new HashMap<>();

    // (from|to) -> blocked?
    private final Map<String, Boolean> partitions = new HashMap<>();

    private double duplicateProbability = 0.0;

    public UnreliableNetwork(long seed) {
        this.rnd = new Random(seed);
    }

    public void register(ParticipantServer p) {
        participants.put(p.id(), p);
    }

    public void setDuplicateProbability(double p) {
        this.duplicateProbability = Math.max(0.0, Math.min(1.0, p));
    }

    public void dropFirst(MsgType type, String toId, int count) {
        dropFirst.put(key(type, toId), Math.max(0, count));
    }

    public void crashOnFirst(MsgType type, String toId) {
        crashOnFirst.put(key(type, toId), true);
    }

    public void setPartition(String fromId, String toId, boolean blocked) {
        partitions.put(partKey(fromId, toId), blocked);
    }

    public Vote rpcPrepare(String fromId, String toId, TxId txId, String op) throws RpcTimeoutException {
        maybeFail(fromId, toId, MsgType.PREPARE);
        ParticipantServer p = mustGet(toId);
        try {
            Vote v = p.onPrepare(txId, op);
            maybeDuplicate(() -> {
                try { p.onPrepare(txId, op); } catch (IOException ignored) {}
            });
            return v;
        } catch (IllegalStateException ise) {
            throw new RpcTimeoutException("prepare timeout/crash: " + ise.getMessage());
        } catch (IOException ioe) {
            throw new RpcTimeoutException("prepare IO: " + ioe.getMessage());
        }
    }

    public void rpcCommit(String fromId, String toId, TxId txId) throws RpcTimeoutException {
        maybeFail(fromId, toId, MsgType.COMMIT);
        ParticipantServer p = mustGet(toId);
        try {
            p.onCommit(txId);
            maybeDuplicate(() -> {
                try { p.onCommit(txId); } catch (IOException ignored) {}
            });
        } catch (IllegalStateException ise) {
            throw new RpcTimeoutException("commit timeout/crash: " + ise.getMessage());
        } catch (IOException ioe) {
            throw new RpcTimeoutException("commit IO: " + ioe.getMessage());
        }
    }

    public void rpcAbort(String fromId, String toId, TxId txId) throws RpcTimeoutException {
        maybeFail(fromId, toId, MsgType.ABORT);
        ParticipantServer p = mustGet(toId);
        try {
            p.onAbort(txId);
            maybeDuplicate(() -> {
                try { p.onAbort(txId); } catch (IOException ignored) {}
            });
        } catch (IllegalStateException ise) {
            throw new RpcTimeoutException("abort timeout/crash: " + ise.getMessage());
        } catch (IOException ioe) {
            throw new RpcTimeoutException("abort IO: " + ioe.getMessage());
        }
    }

    private void maybeFail(String fromId, String toId, MsgType type) throws RpcTimeoutException {
        if (Boolean.TRUE.equals(partitions.get(partKey(fromId, toId)))) {
            throw new RpcTimeoutException("partition blocks " + fromId + " -> " + toId);
        }

        String k = key(type, toId);

        // Crash hook (fires once)
        if (Boolean.TRUE.equals(crashOnFirst.get(k)) && !Boolean.TRUE.equals(crashAlreadyFired.get(k))) {
            crashAlreadyFired.put(k, true);
            ParticipantServer p = participants.get(toId);
            if (p != null) p.setCrashed(true);
            throw new RpcTimeoutException("node " + toId + " crashed on " + type);
        }

        Integer left = dropFirst.get(k);
        if (left != null && left > 0) {
            dropFirst.put(k, left - 1);
            throw new RpcTimeoutException("dropped " + type + " to " + toId + " (left=" + (left - 1) + ")");
        }
    }

    private void maybeDuplicate(Runnable action) {
        if (duplicateProbability <= 0.0) return;
        if (rnd.nextDouble() < duplicateProbability) {
            action.run();
        }
    }

    private ParticipantServer mustGet(String id) {
        ParticipantServer p = participants.get(id);
        if (p == null) throw new IllegalArgumentException("Unknown participant: " + id);
        return p;
    }

    private static String key(MsgType type, String toId) {
        return type.name() + "|" + toId;
    }

    private static String partKey(String from, String to) {
        return from + "->" + to;
    }
}
