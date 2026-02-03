package ru.hse.drip.systems.distributed.seminar.i17;

import java.util.HashMap;
import java.util.Map;

/**
 * Very small "shard" holding account balances.
 * We simulate the idea of locks/reservations in prepare phase.
 */
public class BankShard {
    private final String shardId;
    private final Map<String, Long> balances = new HashMap<>();
    // txId -> reserved amount for a debit (only for debit operations)
    private final Map<String, Reservation> reservations = new HashMap<>();

    public BankShard(String shardId) {
        this.shardId = shardId;
    }

    public String shardId() {
        return shardId;
    }

    public void setBalance(String account, long amount) {
        balances.put(account, amount);
    }

    public long getBalance(String account) {
        return balances.getOrDefault(account, 0L);
    }

    public Map<String, Long> snapshotBalances() {
        return new HashMap<>(balances);
    }

    /**
     * Prepare operation: validate and, for DEBIT, reserve funds for this tx.
     */
    public synchronized Vote prepare(TxId txId, String operation) {
        Operation op = Operation.parse(operation);
        if (op.type == OperationType.CREDIT) {
            // Always can credit in this simplified model.
            return Vote.COMMIT;
        }
        // DEBIT:
        long current = getBalance(op.account);
        long alreadyReserved = reservedTotalForAccount(op.account);
        long available = current - alreadyReserved;
        if (available < op.amount) {
            return Vote.ABORT;
        }
        // Reserve for this tx.
        reservations.put(txId.value(), new Reservation(op.account, op.amount));
        return Vote.COMMIT;
    }

    /**
     * Recreate in-memory reservation after restart (for a tx that was PREPARED).
     */
    public synchronized void recoverPrepared(TxId txId, String operation) {
        Operation op = Operation.parse(operation);
        if (op.type == OperationType.DEBIT) {
            reservations.put(txId.value(), new Reservation(op.account, op.amount));
        }
    }

    public synchronized void commit(TxId txId, String operation) {
        Operation op = Operation.parse(operation);
        if (op.type == OperationType.CREDIT) {
            long current = getBalance(op.account);
            balances.put(op.account, current + op.amount);
            return;
        }
        // DEBIT:
        Reservation r = reservations.remove(txId.value());
        if (r == null) {
            throw new IllegalStateException("No reservation for tx=" + txId + " on " + shardId);
        }
        long current = getBalance(op.account);
        balances.put(op.account, current - op.amount);
    }

    public synchronized void abort(TxId txId, String operation) {
        Operation op = Operation.parse(operation);
        if (op.type == OperationType.DEBIT) {
            reservations.remove(txId.value()); // release
        }
        // credit abort: nothing to do
    }

    private long reservedTotalForAccount(String account) {
        long sum = 0;
        for (Reservation r : reservations.values()) {
            if (r.account.equals(account)) sum += r.amount;
        }
        return sum;
    }

    private static final class Reservation {
        final String account;
        final long amount;

        Reservation(String account, long amount) {
            this.account = account;
            this.amount = amount;
        }
    }

    private enum OperationType { DEBIT, CREDIT }

    private static final class Operation {
        final OperationType type;
        final String account;
        final long amount;

        Operation(OperationType type, String account, long amount) {
            this.type = type;
            this.account = account;
            this.amount = amount;
        }

        static Operation parse(String s) {
            // Format: "DEBIT <account> <amount>" or "CREDIT <account> <amount>"
            String[] parts = s.trim().split("\\s+");
            if (parts.length != 3) throw new IllegalArgumentException("Bad operation: " + s);
            OperationType t = OperationType.valueOf(parts[0].toUpperCase());
            String acc = parts[1];
            long amt = Long.parseLong(parts[2]);
            if (amt < 0) throw new IllegalArgumentException("Negative amount: " + s);
            return new Operation(t, acc, amt);
        }
    }
}
