package ru.hse.drip.systems.distributed.seminar.i24.common;

public final class Log {
    private Log() {
    }

    public static void line(String message) {
        System.out.println(message);
    }

    public static void title(String message) {
        System.out.println();
        System.out.println("=== " + message + " ===");
    }
}
