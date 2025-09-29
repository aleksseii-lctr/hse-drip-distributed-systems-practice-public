package ru.hse.drip.systems.distributed.seminar.i3.classwork.node;

import java.io.IOException;

public class ServerNode2 {

    public static void main(String[] args) throws IOException {
        BaseChatServers.startServer(8882);
    }

}
