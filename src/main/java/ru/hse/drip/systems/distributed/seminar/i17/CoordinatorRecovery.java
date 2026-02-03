package ru.hse.drip.systems.distributed.seminar.i17;

import java.io.IOException;
import java.util.*;

/**
 * Minimal recovery parser for coordinator log:
 * txId|BEGIN|<plan>
 * txId|DECISION|COMMIT/ABORT
 */
public final class CoordinatorRecovery {
    public static final class TxMeta {
        final String txId;
        TxPlan plan;
        Decision decision;

        TxMeta(String txId) {
            this.txId = txId;
        }
    }

    private final Map<String, TxMeta> txs;

    private CoordinatorRecovery(Map<String, TxMeta> txs) {
        this.txs = txs;
    }

    public Map<String, TxMeta> txs() {
        return txs;
    }

    public static CoordinatorRecovery fromLog(DurableLog log) throws IOException {
        List<String> lines = log.readAll();
        Map<String, TxMeta> txs = new LinkedHashMap<>();
        for (String line : lines) {
            String[] parts = line.split("\\|", 3);
            if (parts.length < 2) continue;
            String txId = parts[0];
            String event = parts[1];
            String payload = parts.length == 3 ? parts[2] : "";

            TxMeta meta = txs.computeIfAbsent(txId, TxMeta::new);
            switch (event) {
                case "BEGIN":
                    meta.plan = deserializePlan(payload);
                    break;
                case "DECISION":
                    meta.decision = Decision.valueOf(payload.trim());
                    break;
                default:
                    // ignore
            }
        }
        return new CoordinatorRecovery(txs);
    }

    // Plan serialization: "p1=op1; p2=op2; ..."
    public static String serializePlan(TxPlan plan) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : plan.participantOps().entrySet()) {
            if (!first) sb.append(";");
            first = false;
            sb.append(escape(e.getKey())).append("=").append(escape(e.getValue()));
        }
        return sb.toString();
    }

    public static TxPlan deserializePlan(String s) {
        Map<String, String> map = new LinkedHashMap<>();
        if (s == null || s.isEmpty()) return new TxPlan(map);
        String[] entries = s.split(";");
        for (String entry : entries) {
            if (entry.isEmpty()) continue;
            String[] kv = entry.split("=", 2);
            if (kv.length != 2) continue;
            map.put(unescape(kv[0]), unescape(kv[1]));
        }
        return new TxPlan(map);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\s").replace("=", "\\e").replace("|", "\\p");
    }

    private static String unescape(String s) {
        return s.replace("\\p", "|").replace("\\e", "=").replace("\\s", ";").replace("\\\\", "\\");
    }
}
