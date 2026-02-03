package ru.hse.drip.systems.distributed.seminar.i17;

import java.io.IOException;
import java.util.List;

/**
 * Very small "WAL-like" append-only log for educational purposes.
 * Each record is a single line: txId|event|payload
 *
 * In real systems you'd worry about fsync, checksums, partial writes, etc.
 */
public interface DurableLog {
    void append(String record) throws IOException;

    List<String> readAll() throws IOException;
}
