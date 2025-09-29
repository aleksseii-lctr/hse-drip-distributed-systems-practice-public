package ru.hse.drip.systems.distributed.seminar.i3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int SERVER_PORT = 1337;

    private static List<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws IOException {
        System.out.println("started server on port " + SERVER_PORT);
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("connected another client...");

            var handler = new ClientHandler(clientSocket, clients);
            clients.add(handler);
            EXECUTOR.submit(handler);
        }

    }

}
