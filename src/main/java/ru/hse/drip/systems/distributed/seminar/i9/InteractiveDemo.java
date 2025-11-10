package ru.hse.drip.systems.distributed.seminar.i9;

import java.util.Scanner;
import java.util.UUID;

/**
 * Интерактивное консольное приложение для демонстрации согласованного хеширования
 */
public class InteractiveDemo {
    private ShardingSystem system;
    private Scanner scanner;

    public InteractiveDemo() {
        this.system = new ShardingSystem();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printHeader();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addNodeInteractive();
                    break;
                case "2":
                    removeNodeInteractive();
                    break;
                case "3":
                    putDataInteractive();
                    break;
                case "4":
                    getDataInteractive();
                    break;
                case "5":
                    system.printStatistics();
                    break;
                case "6":
                    system.printAllData();
                    break;
                case "7":
                    system.printRingInfo();
                    break;
                case "8":
                    system.printRingVisualization();
                    break;
                case "9":
                    runDemo();
                    break;
                case "0":
                    running = false;
                    System.out.println("\n👋 До свидания!\n");
                    break;
                default:
                    System.out.println("❌ Неизвестная команда. Попробуйте еще раз.\n");
            }
        }

        scanner.close();
    }

    private void addNodeInteractive() {
        UUID nodeId = system.addNode();
        System.out.println("Новый узел создан с ID: " + nodeId + "\n");
    }

    private void removeNodeInteractive() {
        System.out.print("Введите UUID узла для удаления: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("❌ UUID не может быть пустым\n");
            return;
        }

        try {
            UUID nodeId = UUID.fromString(input);
            system.removeNode(nodeId);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Некорректный формат UUID\n");
        }
    }

    private void putDataInteractive() {
        System.out.print("Введите ключ: ");
        String key = scanner.nextLine().trim();
        if (key.isEmpty()) {
            System.out.println("❌ Ключ не может быть пустым\n");
            return;
        }

        System.out.print("Введите значение: ");
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            System.out.println("❌ Значение не может быть пустым\n");
            return;
        }

        system.put(key, value);
    }

    private void getDataInteractive() {
        System.out.print("Введите ключ: ");
        String key = scanner.nextLine().trim();
        if (!key.isEmpty()) {
            system.get(key);
        } else {
            System.out.println("❌ Ключ не может быть пустым\n");
        }
    }

    private void runDemo() {
        System.out.println("\n📌 АВТОМАТИЧЕСКАЯ ДЕМОНСТРАЦИЯ");
        System.out.println("══════════════════════════════════════════════════════════════\n");

        // Шаг 1: Добавление узлов
        System.out.println("Шаг 1: Добавляем 3 узла...");
        system.addNode();
        system.addNode();
        system.addNode();
        System.out.println();

        system.printRingInfo();

        // Шаг 2: Добавление данных
        System.out.println("Шаг 2: Добавляем данные...");
        String[] keys = {
                "user:1001", "user:1002", "user:1003", "user:1004", "user:1005",
                "user:1006", "user:1007", "user:1008", "user:1009", "user:1010"
        };

        for (String key : keys) {
            system.put(key, "data_" + key);
        }
        System.out.println();

        system.printStatistics();
        system.printAllData();

        // Шаг 3: Добавление нового узла
        System.out.println("Шаг 3: Добавляем 4-й узел...");
        system.addNode();
        System.out.println();

        System.out.println("⚠️  В реальной системе здесь произойдет ребалансировка данных.\n");

        system.printStatistics();

        // Шаг 4: Добавление новых данных
        System.out.println("Шаг 4: Добавляем еще данные...");
        String[] newKeys = {"user:2001", "user:2002", "user:2003", "user:2004", "user:2005"};
        for (String key : newKeys) {
            system.put(key, "data_" + key);
        }
        System.out.println();

        system.printStatistics();
        system.printAllData();

        // Шаг 5: Чтение данных
        System.out.println("Шаг 5: Читаем данные...");
        system.get("user:1001");
        System.out.println();
        system.get("user:2003");
        System.out.println();

        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("Демонстрация завершена!\n");
    }

    private void printHeader() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ДЕМОНСТРАЦИЯ СОГЛАСОВАННОГО ХЕШИРОВАНИЯ                       ║");
        System.out.println("║  (Consistent Hashing - базовый подход без виртуальных нод)    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
    }

    private void printMenu() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        ГЛАВНОЕ МЕНЮ                           ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Добавить узел                                              ║");
        System.out.println("║  2. Удалить узел                                               ║");
        System.out.println("║  3. Вставить данные                                            ║");
        System.out.println("║  4. Получить данные                                            ║");
        System.out.println("║  5. Статистика распределения                                   ║");
        System.out.println("║  6. Вывести все данные                                         ║");
        System.out.println("║  7. Информация о кольце                                        ║");
        System.out.println("║  8. Визуализация кольца                                        ║");
        System.out.println("║  9. Запустить автоматическую демонстрацию                     ║");
        System.out.println("║  0. Выход                                                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.print("Выберите пункт: ");
    }

    public static void main(String[] args) {
        InteractiveDemo demo = new InteractiveDemo();
        demo.start();
    }
}

