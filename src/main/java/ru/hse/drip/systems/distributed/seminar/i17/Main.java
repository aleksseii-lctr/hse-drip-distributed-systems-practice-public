package ru.hse.drip.systems.distributed.seminar.i17;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("data");
        if (args.length >= 1) {
            dir = Path.of(args[0]);
        }
        System.out.println("Using data dir: " + dir.toAbsolutePath());
        Scenarios.runAll(dir);
    }
}
