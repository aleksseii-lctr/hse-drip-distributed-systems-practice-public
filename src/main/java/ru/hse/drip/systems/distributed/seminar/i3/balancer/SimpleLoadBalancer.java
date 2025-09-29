package ru.hse.drip.systems.distributed.seminar.i3.balancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleLoadBalancer {

    private static final int PORT = 9999;

    private static final List<InetSocketAddress> BACKENDS = List.of(
            new InetSocketAddress("localhost", 8881),
            new InetSocketAddress("localhost", 8882),
            new InetSocketAddress("localhost", 8883)
    );

    private static AtomicInteger currentBackendIdx = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        System.out.println("started load balancer on port " + PORT);
        ServerSocket balancerSocket = new ServerSocket(PORT);

        while (true) {
            Socket clietSocket = balancerSocket.accept();
            var backend = getNextBackend();
            new Thread(new ProxyConnectionRunner(clietSocket, backend)).start();
        }
    }

    private static InetSocketAddress getNextBackend() {
        int idx = currentBackendIdx.incrementAndGet() % BACKENDS.size();
        return BACKENDS.get(idx);
    }

}
