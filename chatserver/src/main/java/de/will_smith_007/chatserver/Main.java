package de.will_smith_007.chatserver;

import de.will_smith_007.chatserver.logger.SimpleLogger;
import de.will_smith_007.chatserver.server.ChatServer;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) throws Exception {
        final SimpleLogger simpleLogger = new SimpleLogger();

        new ChatServer(9009,
                new LinkedList<>(),
                new LinkedList<>(),
                simpleLogger).runServer();
    }
}
