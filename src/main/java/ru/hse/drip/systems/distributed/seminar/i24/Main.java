package ru.hse.drip.systems.distributed.seminar.i24;

import ru.hse.drip.systems.distributed.seminar.i24.demo.FaultDetectionDemos;
import ru.hse.drip.systems.distributed.seminar.i24.demo.LeaderElectionDemos;

public class Main {
    public static void main(String[] args) {
        FaultDetectionDemos.runAll();
        LeaderElectionDemos.runAll();
    }
}
