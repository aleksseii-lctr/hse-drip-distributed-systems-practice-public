package ru.hse.drip.systems.distributed.seminar.i3.balancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class ProxyConnectionRunner implements Runnable {
    private final Socket clientSocket;
    private final InetSocketAddress backend;

    public ProxyConnectionRunner(Socket clientSocket, InetSocketAddress backend) {
        this.clientSocket = clientSocket;
        this.backend = backend;
    }

    @Override
    public void run() {
        try (Socket backendSocket = new Socket()) {
            backendSocket.connect(backend);

            Thread forwardToBackend = new Thread(() -> forward(clientSocket, backendSocket));
            Thread forwardToClient = new Thread(() -> forward(backendSocket, clientSocket));

            forwardToBackend.start();
            forwardToClient.start();

            forwardToBackend.join();
            forwardToClient.join();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void forward(Socket from, Socket to) {
        try (InputStream in = from.getInputStream();
             OutputStream out = to.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Ошибка передачи данных: " + e.getMessage());
        }
    }
}
