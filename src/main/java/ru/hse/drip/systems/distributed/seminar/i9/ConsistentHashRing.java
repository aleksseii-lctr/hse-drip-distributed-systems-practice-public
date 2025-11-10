package ru.hse.drip.systems.distributed.seminar.i9;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Реализация согласованного хеширования (Consistent Hashing)
 * без виртуальных нод (базовый подход)
 *
 * Основано на подходе из Alex Xu "System Design Interview", Chapter 5
 */
public class ConsistentHashRing {
    // Кольцо: позиция на кольце (hash) -> UUID узла
    private final TreeMap<Long, UUID> ring;

    // Обратный маппинг: UUID узла -> его позиция на кольце (для быстрого удаления)
    private final SortedMap<UUID, Long> nodeHashMap;

    public ConsistentHashRing() {
        this.ring = new TreeMap<>();
        this.nodeHashMap = new TreeMap<>();
    }

    /**
     * Хеш-функция на основе SHA-256
     * Преобразует строку в положительное 64-битное число
     *
     * @param key строка для хеширования
     * @return положительное значение long, представляющее позицию на кольце
     */
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));

            // Берем первые 8 байт из SHA-256 хеша и собираем их в long
            long hashValue = 0;
            for (int i = 0; i < 8; i++) {
                hashValue = (hashValue << 8) | (digest[i] & 0xFF);
            }

            // Возвращаем абсолютное значение для положительной позиции
            return Math.abs(hashValue);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Добавить узел в кольцо
     *
     * @param nodeId UUID узла
     */
    public void addNode(UUID nodeId) {
        long hash = hash(nodeId.toString());
        ring.put(hash, nodeId);
        nodeHashMap.put(nodeId, hash);
        System.out.println("✅ Узел '" + nodeId + "' добавлен на позицию ");
    }

    /**
     * Удалить узел из кольца
     *
     * @param nodeId UUID узла для удаления
     */
    public void removeNode(UUID nodeId) {
        long hash = nodeHashMap.get(nodeId);
        ring.remove(hash);
        nodeHashMap.remove(nodeId);
        System.out.println("❌ Узел '" + nodeId + "' удален");
    }

    /**
     * Получить узел для данного ключа (по часовой стрелке)
     *
     * @param key ключ данных
     * @return UUID узла, ответственного за этот ключ, или null если нет доступных узлов
     */
    public UUID getNode(String key) {
        long keyHash = hash(key);
        Map.Entry<Long, UUID> nodeDataFrom = ring.ceilingEntry(keyHash);

        if  (nodeDataFrom == null) {
            return ring.firstEntry().getValue();
        }
        return nodeDataFrom.getValue();
    }

    /**
     * Количество узлов
     */
    public int getNodeCount() {
        return ring.size();
    }

    /**
     * Список всех узлов
     */
    public List<UUID> getAllNodes() {
        return new ArrayList<>(new LinkedHashSet<>(ring.values()));
    }

    /**
     * Получить позицию ключа на кольце (для визуализации)
     *
     * @param key ключ для хеширования
     * @return позиция ключа на кольце
     */
    public long getKeyPosition(String key) {
        return hash(key);
    }

    /**
     * Получить все узлы с их позициями (для визуализации)
     *
     * @return map с позициями и UUID узлов
     */
    public TreeMap<Long, UUID> getRingEntries() {
        return new TreeMap<>(ring);
    }

    /**
     * Отладочная печать кольца
     */
    public void printRingInfo() {
        System.out.println("\n📊 ИНФОРМАЦИЯ О КОЛЬЦЕ:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Всего узлов: " + ring.size());
        System.out.println("\nРаспределение позиций на кольце:");

        for (Map.Entry<Long, UUID> entry : ring.entrySet()) {
            System.out.printf("  %-38s -> позиция: %d%n", entry.getValue(), entry.getKey());
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
