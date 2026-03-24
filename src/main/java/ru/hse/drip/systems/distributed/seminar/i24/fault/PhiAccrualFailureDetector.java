package ru.hse.drip.systems.distributed.seminar.i24.fault;

import java.util.ArrayDeque;
import java.util.Deque;

public class PhiAccrualFailureDetector {
    private final Deque<Long> intervals = new ArrayDeque<>();
    private final int windowSize;
    private long lastHeartbeat = -1;

    public PhiAccrualFailureDetector(int windowSize) {
        this.windowSize = windowSize;
    }

    public void heartbeat(long timestampMs) {
        if (lastHeartbeat != -1) {
            long interval = timestampMs - lastHeartbeat;
            intervals.addLast(interval);
            if (intervals.size() > windowSize) {
                intervals.removeFirst();
            }
        }
        lastHeartbeat = timestampMs;
    }

    public double phi(long nowMs) {
        if (lastHeartbeat == -1 || intervals.isEmpty()) {
            return 0.0;
        }
        double mean = mean();
        double stdDev = Math.max(1.0, stdDev(mean));
        double elapsed = nowMs - lastHeartbeat;
        double z = (elapsed - mean) / stdDev;
        double cdf = 1.0 / (1.0 + Math.exp(-z));
        double survival = Math.max(1e-12, 1.0 - cdf);
        return -Math.log10(survival);
    }

    public boolean isSuspected(long nowMs, double threshold) {
        return phi(nowMs) >= threshold;
    }

    private double mean() {
        double sum = 0;
        for (Long i : intervals) {
            sum += i;
        }
        return sum / intervals.size();
    }

    private double stdDev(double mean) {
        double sum = 0;
        for (Long i : intervals) {
            double diff = i - mean;
            sum += diff * diff;
        }
        return Math.sqrt(sum / intervals.size());
    }
}
