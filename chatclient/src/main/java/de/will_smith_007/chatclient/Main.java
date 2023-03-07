package de.will_smith_007.chatclient;

import de.will_smith_007.chatclient.client.Client;
import de.will_smith_007.chatclient.logger.SimpleLogger;

public class Main {

    public static void main(String[] args) {
        new Client("host",
                9009,
                "SuperSecretToken",
                new SimpleLogger()).connect();
    }
}
