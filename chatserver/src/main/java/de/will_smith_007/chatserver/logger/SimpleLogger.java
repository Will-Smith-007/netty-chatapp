package de.will_smith_007.chatserver.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public void log(Level level, String message) {
        final Date date = new Date(System.currentTimeMillis());
        final String formattedDateTime = SIMPLE_DATE_FORMAT.format(date);
        System.out.println("[" + formattedDateTime + "] " + level + ": " + message);
    }

    public enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR;
    }
}
