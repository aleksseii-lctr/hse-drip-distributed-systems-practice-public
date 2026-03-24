package ru.hse.drip.systems.distributed.seminar.i24.fault;

import ru.hse.drip.systems.distributed.seminar.i24.common.NodeState;

import java.util.ArrayDeque;
import java.util.Deque;

public class TimeoutFreeFailureDetector {
    private final Deque<Long> intervals = new ArrayDeque<>();
    private final int windowSize;
    private long lastHeartbeat = -1;

    public TimeoutFreeFailureDetector(int windowSize) {
        this.windowSize = windowSize;
    }

    public void recordHeartbeat(long timestampMs) {
        if (lastHeartbeat != -1) {
            long interval = timestampMs - lastHeartbeat;
            intervals.addLast(interval);
            if (intervals.size() > windowSize) {
                intervals.removeFirst();
            }
        }
        lastHeartbeat = timestampMs;
    }

    public long expectedArrival() {
        if (lastHeartbeat == -1 || intervals.isEmpty()) {
            return Long.MAX_VALUE;
        }
        long sum = 0;
        for (Long interval : intervals) {
            sum += interval;
        }
        long average = sum / intervals.size();
        return lastHeartbeat + average;
    }

    public NodeState stateAt(long nowMs) {
        if (lastHeartbeat == -1) {
            return NodeState.SUSPECT;
        }
        if (intervals.isEmpty()) {
            return NodeState.UP;
        }
        long expected = expectedArrival();
        long grace = averageInterval() / 2;
        if (nowMs <= expected) {
            return NodeState.UP;
        }
        if (nowMs <= expected + grace) {
            return NodeState.SUSPECT;
        }
        return NodeState.DOWN;
    }

    private long averageInterval() {
        long sum = 0;
        for (Long interval : intervals) {
            sum += interval;
        }
        return intervals.isEmpty() ? 0 : sum / intervals.size();
    }
}
