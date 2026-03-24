package ru.hse.drip.systems.distributed.seminar.i24.fault;

import ru.hse.drip.systems.distributed.seminar.i24.common.NodeState;

import java.util.HashMap;
import java.util.Map;

public class HeartbeatPingDetector {
    private final long timeoutMs;
    private final Map<String, Long> lastSeen = new HashMap<>();

    public HeartbeatPingDetector(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void onHeartbeat(String nodeId, long timestampMs) {
        lastSeen.put(nodeId, timestampMs);
    }

    public boolean ping(String nodeId, boolean reachable, long nowMs) {
        if (reachable) {
            onHeartbeat(nodeId, nowMs);
            return true;
        }
        return false;
    }

    public NodeState stateOf(String nodeId, long nowMs) {
        Long ts = lastSeen.get(nodeId);
        if (ts == null) {
            return NodeState.SUSPECT;
        }
        long delta = nowMs - ts;
        if (delta <= timeoutMs) {
            return NodeState.UP;
        }
        if (delta <= timeoutMs * 2) {
            return NodeState.SUSPECT;
        }
        return NodeState.DOWN;
    }
}
