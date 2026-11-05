package network.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static Logger instance;
    private PrintWriter writer;
    private String logFile;
    
    private Logger(String logFile) {
        this.logFile = logFile;
        try {
            // true = append mode (no sobrescribe, agrega al final)
            writer = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            System.err.println("No se pudo crear el archivo de log: " + e.getMessage());
        }
    }
    
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger("app.log");
        }
        return instance;
    }
    
    public static Logger getInstance(String logFile) {
        if (instance == null) {
            instance = new Logger(logFile);
        }
        return instance;
    }
    
    public void info(String message) {
        log("INFO", message);
    }
    
    public void error(String message) {
        log("ERROR", message);
    }
    
    public void error(String message, Throwable throwable) {
        log("ERROR", message);
        log("ERROR", getStackTrace(throwable));
    }
    
    public void debug(String message) {
        log("DEBUG", message);
    }
    
    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Escribimos en el archivo
        if (writer != null) {
            writer.println(logEntry);
        }
        
        // También mostramos en consola
        System.out.println(logEntry);
    }
    
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}