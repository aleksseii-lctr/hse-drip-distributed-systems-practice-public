package ru.hse.drip.systems.distributed.seminar.i24.demo;

import ru.hse.drip.systems.distributed.seminar.i24.common.Log;
import ru.hse.drip.systems.distributed.seminar.i24.fault.*;

import java.util.List;
import java.util.Map;

public class FaultDetectionDemos {
    public static void runAll() {
        demoHeartbeatPing();
        demoTimeoutFree();
        demoOutsourced();
        demoPhiAccrual();
        demoGossip();
    }

    public static void demoHeartbeatPing() {
        Log.title("Fault detection #1: heartbeats and ping/echo");
        HeartbeatPingDetector detector = new HeartbeatPingDetector(1000);
        detector.onHeartbeat("node-B", 0);
        detector.ping("node-B", true, 800);
        Log.line("t=1200 -> " + detector.stateOf("node-B", 1200));
        Log.line("t=2200 -> " + detector.stateOf("node-B", 2200));
        Log.line("t=3400 -> " + detector.stateOf("node-B", 3400));
    }

    public static void demoTimeoutFree() {
        Log.title("Fault detection #2: timeout-free / adaptive detector");
        TimeoutFreeFailureDetector detector = new TimeoutFreeFailureDetector(5);
        detector.recordHeartbeat(1000);
        detector.recordHeartbeat(2000);
        detector.recordHeartbeat(2950);
        detector.recordHeartbeat(3950);
        Log.line("expected next heartbeat around: " + detector.expectedArrival());
        Log.line("t=4900 -> " + detector.stateAt(4900));
        Log.line("t=5600 -> " + detector.stateAt(5600));
    }

    public static void demoOutsourced() {
        Log.title("Fault detection #3: outsourced heartbeats");
        OutsourcedHeartbeatDetector detector = new OutsourcedHeartbeatDetector();
        boolean alive = detector.isNodeAlive(false, List.of(
            new OutsourcedHeartbeatDetector.ProbeResult("node-C", "node-B", false),
            new OutsourcedHeartbeatDetector.ProbeResult("node-D", "node-B", true)
        ));
        Log.line("node-A cannot reach node-B directly, but helpers say alive = " + alive);
    }

    public static void demoPhiAccrual() {
        Log.title("Fault detection #4: phi-accrual detector");
        PhiAccrualFailureDetector detector = new PhiAccrualFailureDetector(10);
        detector.heartbeat(1000);
        detector.heartbeat(2000);
        detector.heartbeat(3000);
        detector.heartbeat(4000);
        double phiAt4700 = detector.phi(4700);
        double phiAt7000 = detector.phi(7000);
        Log.line("phi(t=4700) = " + String.format("%.3f", phiAt4700) + ", suspect>2 ? " + detector.isSuspected(4700, 2.0));
        Log.line("phi(t=7000) = " + String.format("%.3f", phiAt7000) + ", suspect>2 ? " + detector.isSuspected(7000, 2.0));
    }

    public static void demoGossip() {
        Log.title("Fault detection #5: gossip-based membership");
        GossipFailureDetector nodeA = new GossipFailureDetector(2, 4);
        GossipFailureDetector nodeB = new GossipFailureDetector(2, 4);
        nodeA.observe("node-C", 1, 1);
        nodeA.observe("node-C", 2, 2);
        nodeB.merge(nodeA.snapshot(), 2);
        nodeA.advanceRound(5);
        nodeB.advanceRound(3);
        printView("node-A view", nodeA.snapshot());
        printView("node-B view", nodeB.snapshot());
    }

    private static void printView(String name, Map<String, GossipFailureDetector.MembershipRecord> snapshot) {
        Log.line(name + ":");
        for (Map.Entry<String, GossipFailureDetector.MembershipRecord> entry : snapshot.entrySet()) {
            Log.line("  " + entry.getKey() + " -> hb=" + entry.getValue().heartbeatCounter()
                + ", state=" + entry.getValue().state());
        }
    }
}
