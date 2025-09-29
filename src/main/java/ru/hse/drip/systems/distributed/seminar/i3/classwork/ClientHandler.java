package ru.hse.drip.systems.distributed.seminar.i3.classwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private final List<ClientHandler> clients;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("Server received: " + msg);
                broadcast(msg);
            }
        } catch (IOException e) {
            System.out.println("client disconnected");
        }
    }

    private void broadcast(String message) {
        clients.forEach(client -> {
            try {
                client.out.write(message + "\n");
                client.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
