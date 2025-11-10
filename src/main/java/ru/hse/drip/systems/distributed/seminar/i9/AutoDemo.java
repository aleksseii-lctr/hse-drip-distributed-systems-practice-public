package ru.hse.drip.systems.distributed.seminar.i9;

/**
 * Автоматическая демонстрация согласованного хеширования
 * Запускает предопределенный сценарий без пользовательского ввода
 */
public class AutoDemo {
    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   ДЕМОНСТРАЦИЯ СОГЛАСОВАННОГО ХЕШИРОВАНИЯ                      ║");
        System.out.println("║   (автоматический режим)                                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        ShardingSystem system = new ShardingSystem();

        // Сценарий 1: добавление узлов
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("СЦЕНАРИЙ 1: Добавление трех узлов");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        system.addNode();
        system.addNode();
        system.addNode();
        System.out.println();

        system.printRingInfo();

        // Сценарий 2: добавление данных
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("СЦЕНАРИЙ 2: Добавление 10 элементов данных");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        String[] initialKeys = {
                "user:1001", "user:1002", "user:1003", "user:1004", "user:1005",
                "user:1006", "user:1007", "user:1008", "user:1009", "user:1010"
        };

        for (String key : initialKeys) {
            system.put(key, "data_for_" + key);
        }
        System.out.println();

        system.printStatistics();
        system.printAllData();
        system.printRingVisualization();

        // Сценарий 3: добавление нового узла
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("СЦЕНАРИЙ 3: Добавление 4-го узла (демонстрация преимущества)");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        System.out.println("КЛЮЧЕВОЙ МОМЕНТ:");
        System.out.println("При простом хешировании (hash(key) % N) изменение N вызвало бы");
        System.out.println("перемещение ~75% данных (очень плохо!)\n");
        System.out.println("С согласованным хешированием нужно переместить гораздо меньше!\n");

        system.addNode();
        System.out.println();

        system.printStatistics();
        system.printAllData();

        // Сценарий 4: добавление новых данных
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("СЦЕНАРИЙ 4: Добавление новых элементов данных");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        String[] newKeys = {
                "user:2001", "user:2002", "user:2003", "user:2004", "user:2005"
        };

        for (String key : newKeys) {
            system.put(key, "data_for_" + key);
        }
        System.out.println();

        system.printStatistics();
        system.printAllData();

        // Сценарий 5: чтение данных
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("СЦЕНАРИЙ 5: Чтение данных");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        system.get("user:1001");
        System.out.println();
        system.get("user:2005");
        System.out.println();
        system.get("user:9999");
        System.out.println();

        // Финал
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        System.out.println("📌 КЛЮЧЕВЫЕ ВЫВОДЫ:");
        System.out.println("   ✅ Согласованное хеширование минимизирует перемещение данных");
        System.out.println("   ✅ При добавлении узла перемещается ~1/N данных вместо (N-1)/N");
        System.out.println("   ✅ Система остается стабильной при масштабировании\n");
    }
}

