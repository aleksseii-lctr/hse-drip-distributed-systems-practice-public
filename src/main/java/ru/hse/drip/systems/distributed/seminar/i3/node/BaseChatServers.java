package ru.hse.drip.systems.distributed.seminar.i3.node;

import ru.hse.drip.systems.distributed.seminar.i3.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseChatServers {

    private static List<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public static void startServer(int port) throws IOException {
        System.out.println("started server on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("connected another client...");

            var handler = new ClientHandler(clientSocket, clients);
            clients.add(handler);
            EXECUTOR.submit(handler);
        }

    }
}
