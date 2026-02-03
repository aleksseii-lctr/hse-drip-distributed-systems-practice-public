package ru.hse.drip.systems.distributed.seminar.i17;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transaction plan: which participant should do which operation.
 * Operation is a simple string to keep the lab minimal (e.g. "DEBIT alice 100").
 */
public final class TxPlan {
    private final Map<String, String> participantOps; // participantId -> op

    public TxPlan(Map<String, String> participantOps) {
        this.participantOps = new LinkedHashMap<>(participantOps); // preserve order
    }

    public Map<String, String> participantOps() {
        return Collections.unmodifiableMap(participantOps);
    }

    public String opFor(String participantId) {
        return participantOps.get(participantId);
    }
}
