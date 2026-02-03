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
        // TODO: implement
        // recoverFromLog();
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
        // TODO: implement participant prepare logic + WAL
        throw new UnsupportedOperationException("TODO: implement ParticipantServer.onPrepare()");
    }

    public synchronized void onCommit(TxId txId) throws IOException {
        ensureAlive();
        // TODO: implement commit (idempotent) + WAL
        throw new UnsupportedOperationException("TODO: implement ParticipantServer.onCommit()");
    }

    public synchronized void onAbort(TxId txId) throws IOException {
        ensureAlive();
        // TODO: implement abort (idempotent) + WAL
        throw new UnsupportedOperationException("TODO: implement ParticipantServer.onAbort()");
    }

    public synchronized ParticipantState stateOf(TxId txId) {
        TxRecord r = records.get(txId.value());
        return r == null ? ParticipantState.INIT : r.state;
    }

    // ---- recovery & helpers ----

    private void recoverFromLog() throws IOException {
        // TODO: parse log lines and rebuild records + shard reservations
        List<String> lines = log.readAll();
        // hint: same parsing as in solution: split("\\|", 3)
        throw new UnsupportedOperationException("TODO: implement ParticipantServer.recoverFromLog()");
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
