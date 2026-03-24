package ru.hse.drip.systems.distributed.seminar.i24.fault;

import ru.hse.drip.systems.distributed.seminar.i24.common.NodeState;

import java.util.HashMap;
import java.util.Map;

public class GossipFailureDetector {
    public static class MembershipRecord {
        private long heartbeatCounter;
        private long lastUpdateRound;
        private NodeState state = NodeState.UP;

        public MembershipRecord(long heartbeatCounter, long lastUpdateRound) {
            this.heartbeatCounter = heartbeatCounter;
            this.lastUpdateRound = lastUpdateRound;
        }

        public long heartbeatCounter() {
            return heartbeatCounter;
        }

        public long lastUpdateRound() {
            return lastUpdateRound;
        }

        public NodeState state() {
            return state;
        }

        public void setState(NodeState state) {
            this.state = state;
        }

        public void update(long heartbeatCounter, long round) {
            if (heartbeatCounter > this.heartbeatCounter) {
                this.heartbeatCounter = heartbeatCounter;
                this.lastUpdateRound = round;
                this.state = NodeState.UP;
            }
        }
    }

    private final Map<String, MembershipRecord> view = new HashMap<>();
    private final long suspectAfterRounds;
    private final long deadAfterRounds;

    public GossipFailureDetector(long suspectAfterRounds, long deadAfterRounds) {
        this.suspectAfterRounds = suspectAfterRounds;
        this.deadAfterRounds = deadAfterRounds;
    }

    public void observe(String nodeId, long heartbeatCounter, long round) {
        view.computeIfAbsent(nodeId, id -> new MembershipRecord(heartbeatCounter, round))
            .update(heartbeatCounter, round);
    }

    public void merge(Map<String, MembershipRecord> other, long round) {
        for (Map.Entry<String, MembershipRecord> entry : other.entrySet()) {
            observe(entry.getKey(), entry.getValue().heartbeatCounter(), round);
        }
    }

    public void advanceRound(long round) {
        for (MembershipRecord record : view.values()) {
            long silence = round - record.lastUpdateRound();
            if (silence >= deadAfterRounds) {
                record.setState(NodeState.DOWN);
            } else if (silence >= suspectAfterRounds) {
                record.setState(NodeState.SUSPECT);
            }
        }
    }

    public Map<String, MembershipRecord> snapshot() {
        return view;
    }
}
