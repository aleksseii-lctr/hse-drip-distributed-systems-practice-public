package ru.hse.drip.systems.distributed.seminar.i3.classwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ChatClient {

    private static final int SERVER_PORT = 9999;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", SERVER_PORT);
        System.out.println("connected to server on port: " + SERVER_PORT);

        var out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
        );
        var in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        new Thread(() -> readServerMessage(in)).start();

        var console = new BufferedReader(new InputStreamReader(System.in));
        String userMsg;
        while ((userMsg = console.readLine()) != null) {
            out.write(userMsg + "\n");
            out.flush();
        }
    }

    private static void readServerMessage(BufferedReader in) {
        String msg;
        while (true) {
            try {
                if ((msg = in.readLine()) != null) {
                    System.out.println("<< " + msg);
                }
            } catch (IOException e) {
                System.out.println("server sent fin");
            }

        }
    }

}
