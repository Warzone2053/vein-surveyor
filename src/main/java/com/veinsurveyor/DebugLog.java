package com.veinsurveyor;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLog {
    private static final String LOG_FILE = "veinsurveyor_debug.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String formatted = "[" + timestamp + "] " + message;
        System.out.println("[VeinSurveyor] " + message);
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(formatted);
        } catch (Throwable ignored) {}
    }

    public static synchronized void log(String message, Throwable t) {
        log(message + " - ERROR: " + t);
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            t.printStackTrace(out);
        } catch (Throwable ignored) {}
    }
}
