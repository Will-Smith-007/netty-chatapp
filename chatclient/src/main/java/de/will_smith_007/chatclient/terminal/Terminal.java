package de.will_smith_007.chatclient.terminal;

import io.netty.channel.Channel;
import io.netty.util.CharsetUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Terminal {

    private static volatile boolean isRunning = false;
    private static volatile Thread thread = null;

    private final Channel clientChannel;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public Terminal(@NotNull Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    public void startTerminal() {
        if (isRunning) return;
        isRunning = true;
        thread = new Thread(() -> {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, CharsetUtil.UTF_8));
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.length() == 0) continue;
                    final long currentTimeMillis = System.currentTimeMillis();
                    final Date date = new Date(currentTimeMillis);
                    final String formattedDateTime = simpleDateFormat.format(date);
                    clientChannel.writeAndFlush("[" + formattedDateTime + "] " + clientChannel.localAddress() + " schrieb: " + line, clientChannel.voidPromise());
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopTerminal() {
        if (!isRunning) return;
        isRunning = false;
        thread.interrupt();
    }
}
