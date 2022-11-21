package Allocator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger extends Thread {
    private static Logger instance;
    private static DateTimeFormatter dateTimeFormatter;

    private Logger() {
        dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SS");
    }

    public static Logger getInstance() {
        if(instance == null) instance = new Logger();
        return instance;
    }

    public void log(Object s) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[" + dateTimeFormatter.format(now) + "]" + "{" + Thread.currentThread().getId() + "} " + s);
    }
}