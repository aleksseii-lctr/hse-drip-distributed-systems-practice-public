package ru.hse.drip.systems.distributed.seminar.i17;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO (students): implement participant-side of 2PC.
 * Requirements (minimum for lab):
 * - onPrepare(txId, op):
 *   - If tx already in PREPARED -> return COMMIT (idempotent).
 *   - If tx already COMMITTED -> return COMMIT.
 *   - If tx already ABORTED -> return ABORT.
 *   - Otherwise:
 *       1) Ask shard.prepare(txId, op) to validate/reserve.
 *       2) If vote COMMIT -> log "txId|PREPARED|op" BEFORE replying commit.
 *       3) If vote ABORT -> log "txId|VOTE_ABORT|op" (optional but useful for debugging).
 * - onCommit(txId):
 *   - If already COMMITTED -> return (idempotent).
 *   - If PREPARED -> apply shard.commit(txId, op), then log "txId|COMMITTED|".
 * - onAbort(txId):
 *   - If already ABORTED -> return.
 *   - If PREPARED -> shard.abort(txId, op), log "txId|ABORTED|".
 * <p>
 * Recovery:
 * - Constructor should call recoverFromLog():
 *   - replay PREPARED (restore state and call shard.recoverPrepared(txId, op))
 *   - replay COMMITTED / ABORTED final states
 */
public class ParticipantServer {
    private final String id;
    private final BankShard shard;
    private final DurableLog log;

    private boolean crashed = false;

    // txId -> record
    private final Map<String, TxRecord> records = new HashMap<>();

    public ParticipantServer(String id, BankShard shard, DurableLog log) throws IOException {
        this.id = id;
        this.shard = shard;
        this.log = log;
        recoverFromLog();
    }

    public String id() {
        return id;
    }

    public BankShard shard() {
        return shard;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public synchronized Vote onPrepare(TxId txId, String operation) throws IOException {
        ensureAlive();
        TxRecord existingRecord = records.get(txId.value());
        if (existingRecord != null) {
            switch (existingRecord.state) {
                case PREPARED, COMMITTED:
                    return Vote.COMMIT;
                case ABORTED:
                    return Vote.ABORT;
                default:
                    break;
            }
        }

        Vote prepareVote = shard.prepare(txId, operation);
        if (prepareVote == Vote.COMMIT) {
            append(String.format("%s|PREPARED|%s", txId.value(), escape(operation)));
            records.put(txId.value(), new TxRecord(ParticipantState.PREPARED, operation));
        } else if (prepareVote == Vote.ABORT) {
            append(String.format("%s|VOTE_ABORT|%s", txId.value(), escape(operation)));
            records.put(txId.value(), new TxRecord(ParticipantState.ABORTED, operation));
        }
        return prepareVote;
    }

    public synchronized void onCommit(TxId txId) throws IOException {
        ensureAlive();

        TxRecord existingRecord = records.get(txId.value());
        if (existingRecord == null) {
            throw new RuntimeException("commit on event w/o prepare: " + txId.value());
        }
        var state = existingRecord.state;
        if (state != ParticipantState.PREPARED) {
            return;
        }

        shard.commit(txId, existingRecord.operation);
        append(String.format("%s|COMMITED|%s", txId.value(), escape(existingRecord.operation)));
        records.put(txId.value(), new TxRecord(ParticipantState.COMMITTED, existingRecord.operation));
    }

    public synchronized void onAbort(TxId txId) throws IOException {
        ensureAlive();

        TxRecord existingRecord = records.get(txId.value());
        if (existingRecord == null) {
            return;
        }
        if (existingRecord.state == ParticipantState.ABORTED) {
            return;
        }
        if (existingRecord.state == ParticipantState.COMMITTED) {
            throw new RuntimeException("abort on commited tx: " + txId.value());
        }

        shard.abort(txId, existingRecord.operation);
        append(String.format("%s|ABORTED|%s", txId.value(), escape(existingRecord.operation)));
        records.put(txId.value(), new TxRecord(ParticipantState.ABORTED, existingRecord.operation));
    }

    public synchronized ParticipantState stateOf(TxId txId) {
        TxRecord r = records.get(txId.value());
        return r == null ? ParticipantState.INIT : r.state;
    }

    // ---- recovery & helpers ----

    private void recoverFromLog() throws IOException {
        List<String> lines = log.readAll();
        for (String line : lines) {
            String[] parts = line.split("\\|", 3);
            if (parts.length < 2) continue;
            String tx = parts[0];
            String event = parts[1];
            String payload = parts.length == 3 ? unescape(parts[2]) : "";

            switch (event) {
                case "PREPARED":
                    records.put(tx, new TxRecord(ParticipantState.PREPARED, payload));
                    // rebuild reservation
                    shard.recoverPrepared(TxId.of(tx), payload);
                    break;
                case "COMMITTED":
                    // commit might have already applied before crash; for simplicity, assume it did.
                    TxRecord prev = records.get(tx);
                    String op = prev != null ? prev.operation : "";
                    records.put(tx, new TxRecord(ParticipantState.COMMITTED, op));
                    break;
                case "ABORTED":
                    TxRecord prev2 = records.get(tx);
                    String op2 = prev2 != null ? prev2.operation : "";
                    records.put(tx, new TxRecord(ParticipantState.ABORTED, op2));
                    break;
                case "VOTE_ABORT":
                    records.put(tx, new TxRecord(ParticipantState.ABORTED, payload));
                    break;
                default:
                    throw new RuntimeException("unknown event " + event);
            }
        }
    }

    private void append(String record) throws IOException {
        log.append(record);
    }

    private void ensureAlive() {
        if (crashed) {
            throw new IllegalStateException(id + " is crashed");
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("|", "\\p");
    }

    private static String unescape(String s) {
        return s.replace("\\p", "|").replace("\\n", "\n").replace("\\\\", "\\");
    }

    private static final class TxRecord {
        final ParticipantState state;
        final String operation;

        TxRecord(ParticipantState state, String operation) {
            this.state = state;
            this.operation = operation;
        }
    }
}
