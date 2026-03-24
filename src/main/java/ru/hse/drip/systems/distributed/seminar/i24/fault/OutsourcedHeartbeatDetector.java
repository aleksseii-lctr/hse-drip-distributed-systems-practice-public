package ru.hse.drip.systems.distributed.seminar.i24.fault;

import java.util.List;

public class OutsourcedHeartbeatDetector {
    public record ProbeResult(String observer, String target, boolean reachable) {}

    public boolean isNodeAlive(boolean directReachable, List<ProbeResult> outsourcedChecks) {
        if (directReachable) {
            return true;
        }
        for (ProbeResult result : outsourcedChecks) {
            if (result.reachable()) {
                return true;
            }
        }
        return false;
    }
}
