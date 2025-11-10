package ru.hse.drip.systems.distributed.seminar.i9;

import java.util.*;

/**
 * Представляет один узел в системе (шард)
 * Хранит часть данных всей системы
 */
public class ShardNode {
    private final UUID nodeId;
    // Локальное хранилище данных: ключ -> значение
    private final Map<String, String> dataStore;

    public ShardNode(UUID nodeId) {
        this.nodeId = nodeId;
        this.dataStore = new HashMap<>();
    }

    public void put(String key, String value) {
        dataStore.put(key, value);
    }

    public String get(String key) {
        return dataStore.get(key);
    }

    public boolean containsKey(String key) {
        return dataStore.containsKey(key);
    }

    public int getKeyCount() {
        return dataStore.size();
    }

    public Set<String> getAllKeys() {
        return new HashSet<>(dataStore.keySet());
    }

    public String remove(String key) {
        return dataStore.remove(key);
    }

    public void clear() {
        dataStore.clear();
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void printContents() {
        System.out.println("  Узел: " + nodeId);
        if (dataStore.isEmpty()) {
            System.out.println("    (пусто)");
            return;
        }

        for (Map.Entry<String, String> entry : dataStore.entrySet()) {
            System.out.println("    [" + entry.getKey() + " = " + entry.getValue() + "]");
        }
    }
}

