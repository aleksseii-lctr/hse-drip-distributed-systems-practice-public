package ru.hse.drip.systems.distributed.seminar.i3.node;

import java.io.IOException;

public class ServerNode1 {

    public static void main(String[] args) throws IOException {
        BaseChatServers.startServer(8881);
    }

}
