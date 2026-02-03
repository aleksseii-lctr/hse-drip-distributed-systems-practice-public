package ru.hse.drip.systems.distributed.seminar.i17;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

public class FileDurableLog implements DurableLog {
    private final Path path;

    public FileDurableLog(Path path) {
        this.path = path;
    }

    @Override
    public synchronized void append(String record) throws IOException {
        Files.createDirectories(path.getParent());
        String line = record + System.lineSeparator();
        Files.writeString(path, line,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        // Educational note: no fsync here.
    }

    @Override
    public synchronized List<String> readAll() throws IOException {
        if (!Files.exists(path)) return Collections.emptyList();
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public Path path() {
        return path;
    }
}
