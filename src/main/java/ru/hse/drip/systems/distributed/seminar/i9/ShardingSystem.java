package ru.hse.drip.systems.distributed.seminar.i9;

import java.util.*;

/**
 * Система для управления шардированием с согласованным хешированием
 */
public class ShardingSystem {
    private final ConsistentHashRing hashRing;
    // Реестр узлов: UUID -> ShardNode
    private final Map<UUID, ShardNode> nodes;

    public ShardingSystem() {
        this.hashRing = new ConsistentHashRing();
        this.nodes = new HashMap<>();
    }

    /**
     * Добавить новый узел в систему
     *
     * @return UUID созданного узла
     */
    public UUID addNode() {
        var uuid = UUID.randomUUID();
        hashRing.addNode(uuid);
        nodes.put(uuid, new ShardNode(uuid));
        return uuid;
    }

    /**
     * Удалить узел из системы
     *
     * @param nodeId UUID узла для удаления
     */
    public void removeNode(UUID nodeId) {
        if (!nodes.containsKey(nodeId)) {
            System.out.println("not found node with id " + nodeId);
            return;
        }
        hashRing.removeNode(nodeId);
        nodes.remove(nodeId);
    }

    public void put(String key, String value) {
        // Определяем узел, ответственный за данный ключ через consistent hashing
        UUID nodeUuid = hashRing.getNode(key);
        if (nodeUuid == null) {
            System.out.println("not found any free node for key " + key);
        }

        var node  = nodes.get(nodeUuid);
        node.put(key, value);
        System.out.println("✅ Ключ '" + key + "' → узел '" + "" + "' | значение: '" + value + "'");
    }

    public String get(String key) {
        // Определяем узел через consistent hashing - тот же алгоритм, что и при put
        // Определяем узел, ответственный за данный ключ через consistent hashing
        UUID nodeUuid = hashRing.getNode(key);
        if (nodeUuid == null) {
            System.out.printf("not found any free node for key " + key + "\n");
            return null;
        }

        ShardNode shardNode = nodes.get(nodeUuid);
        String value = shardNode.get(key);
        if (value != null) {
            System.out.println("✅ Найдено в узле '" + nodeUuid + "': " + value);
        } else {
            System.out.println("❌ Ключ '" + key + "' не найден в узле '" + nodeUuid + "'");
        }
        return value;
    }

    public void printStatistics() {
        System.out.println("\n📊 СТАТИСТИКА РАСПРЕДЕЛЕНИЯ ДАННЫХ:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        int totalKeys = 0;
        int maxKeys = 0;
        int minKeys = Integer.MAX_VALUE;

        // Сортируем узлы по UUID для стабильного вывода
        for (Map.Entry<UUID, ShardNode> entry : new TreeMap<>(nodes).entrySet()) {
            UUID nodeId = entry.getKey();
            ShardNode node = entry.getValue();
            int keyCount = node.getKeyCount();

            totalKeys += keyCount;
            maxKeys = Math.max(maxKeys, keyCount);
            minKeys = Math.min(minKeys, keyCount);

            String bar = "█".repeat(Math.max(1, keyCount / 2));
            System.out.printf("%-38s: %2d ключей %s%n", nodeId, keyCount, bar);
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("Всего ключей: %d%n", totalKeys);

        if (nodes.size() > 0) {
            double average = (double) totalKeys / nodes.size();
            double ratio = maxKeys > 0 ? (double) maxKeys / Math.max(average, 1) : 1.0;
            System.out.printf("Среднее на узел: %.1f%n", average);
            System.out.printf("Коэффициент неравномерности (max/avg): %.2f%n", ratio);

            if (ratio > 1.5) {
                System.out.println("⚠️  Распределение неравномерное!");
            } else {
                System.out.println("✅ Распределение относительно равномерное");
            }
        }
        System.out.println();
    }

    public void printAllData() {
        System.out.println("\n📋 СОДЕРЖИМОЕ ВСЕХ УЗЛОВ:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Сортируем узлы по UUID для стабильного вывода
        for (Map.Entry<UUID, ShardNode> entry : new TreeMap<>(nodes).entrySet()) {
            entry.getValue().printContents();
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    public void printRingInfo() {
        hashRing.printRingInfo();
    }

    /**
     * Визуализация распределения узлов и ключей на кольце
     * Отображает линейное представление кольца с метками
     */
    public void printRingVisualization() {
        System.out.println("\n🔄 ВИЗУАЛИЗАЦИЯ КОЛЬЦА:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (nodes.isEmpty()) {
            System.out.println("Кольцо пустое - нет узлов");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            return;
        }

        // Собираем все позиции на кольце
        TreeMap<Long, UUID> ringEntries = hashRing.getRingEntries();

        // Собираем все ключи данных с их позициями
        Map<Long, List<String>> keyPositions = new TreeMap<>();
        for (Map.Entry<UUID, ShardNode> entry : nodes.entrySet()) {
            ShardNode node = entry.getValue();
            for (String key : node.getAllKeys()) {
                long position = hashRing.getKeyPosition(key);
                keyPositions.computeIfAbsent(position, k -> new ArrayList<>()).add(key);
            }
        }

        // Визуализация кольца
        int ringWidth = 80;  // ширина отображения кольца
        System.out.println("Легенда: [N] = узел, (K) = ключ данных\n");

        // Создаем массив символов для визуализации
        char[] visual = new char[ringWidth];
        Arrays.fill(visual, '─');

        // Отмечаем узлы на кольце
        Map<Integer, String> labels = new TreeMap<>();
        for (Map.Entry<Long, UUID> entry : ringEntries.entrySet()) {
            long position = entry.getKey();
            double percentage = (double) position / Long.MAX_VALUE;
            int index = (int) (percentage * (ringWidth - 1));
            visual[index] = 'N';

            // Сокращаем UUID для отображения (первые 8 символов)
            String shortId = entry.getValue().toString().substring(0, 8);
            labels.put(index, "[" + shortId + "]");
        }

        // Отмечаем ключи данных
        Map<Integer, Integer> keyCount = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry : keyPositions.entrySet()) {
            long position = entry.getKey();
            double percentage = (double) position / Long.MAX_VALUE;
            int index = (int) (percentage * (ringWidth - 1));

            // Если на этой позиции нет узла, ставим метку ключа
            if (visual[index] != 'N') {
                visual[index] = '●';
            }
            keyCount.put(index, entry.getValue().size());
        }

        // Выводим верхнюю границу
        System.out.println("┌" + "─".repeat(ringWidth) + "┐");

        // Выводим визуализацию
        System.out.print("│");
        for (char c : visual) {
            System.out.print(c);
        }
        System.out.println("│");

        // Выводим нижнюю границу
        System.out.println("└" + "─".repeat(ringWidth) + "┘");

        // Выводим метки узлов
        if (!labels.isEmpty()) {
            System.out.println("\nУзлы на кольце:");
            for (Map.Entry<Integer, String> entry : labels.entrySet()) {
                double percentage = (double) entry.getKey() / ringWidth * 100;
                System.out.printf("  %s на позиции ~%.1f%%%n", entry.getValue(), percentage);
            }
        }

        // Выводим информацию о ключах
        if (!keyCount.isEmpty()) {
            int totalKeys = keyCount.values().stream().mapToInt(Integer::intValue).sum();
            System.out.printf("%nВсего ключей на кольце: %d (отмечены символом ●)%n", totalKeys);
        }

        System.out.println("\nПримечание: Кольцо замыкается - конец соединяется с началом");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}

