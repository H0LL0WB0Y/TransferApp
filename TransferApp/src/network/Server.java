package network;

import network.utils.Logger;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class Server {
    private ServerSocket serverSocket;
    private int port;
    private String downloadFolder;
    private Logger logger = Logger.getInstance("server.log");
    private Consumer<String> uiLogger; // <-- Nuevo: para enviar mensajes a la GUI

    // Constructor nuevo que acepta la función de log para la UI
    public Server(int port, String downloadFolder, Consumer<String> uiLogger) {
        this.port = port;
        this.downloadFolder = downloadFolder;
        this.uiLogger = uiLogger;
        
        File folder = new File(downloadFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /*public void start() {
        try {
            serverSocket = new ServerSocket(port);
            log("Servidor iniciado en puerto " + port);
            log("Archivos se guardarán en: " + downloadFolder);
            log("Esperando conexiones...\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Conexión recibida de: " + clientSocket.getInetAddress());
                
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            log("Error al iniciar servidor: " + e.getMessage());
        }
    }*/

    // Método auxiliar para loguear en ambos lados
    private void log(String message) {
        if (uiLogger != null) {
            uiLogger.accept(message);  // La GUI se encarga de todo (consola + archivo + ventana)
        } else {
            logger.info(message);  // Solo si no hay GUI (modo consola)
        }
    }
    
    public Server(int port, String downloadFolder) {
        this.port = port;
        this.downloadFolder = downloadFolder;
        
        File folder = new File(downloadFolder);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                logger.info("Carpeta creada: " + downloadFolder);
            } else {
                logger.error("No se pudo crear la carpeta: " + downloadFolder);
            }
        }
    }

    private volatile boolean running = false;
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            serverSocket = new ServerSocket(port);
            log("Servidor iniciado en puerto " + port);
            log("Archivos se guardarán en: " + downloadFolder);
            log("Esperando conexiones...\n");

            while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                log("Conexión recibida de: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (SocketException e) {
                if (running) {
                    log("Error inesperado: " + e.getMessage());
                } else {
                    log("Servidor detenido.");
                }
                break;
                }
            }
        } catch (IOException e) {
            log("Error al iniciar servidor: " + e.getMessage());
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            
            int fileCount = dis.readInt();
            logger.info("Se van a recibir " + fileCount + " archivo(s)");
            
            int receivedCount = 0;
            
            for (int i = 0; i < fileCount; i++) {
                String fileName = dis.readUTF();
                fileName = sanitizeFileName(fileName);
                long fileSize = dis.readLong();
                
                logger.info("[" + (i + 1) + "/" + fileCount + "] " + 
                    fileName + " (" + formatSize(fileSize) + ")");
                
                File outputFile = new File(downloadFolder, fileName);
                outputFile = getUniqueFileName(outputFile);
                
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                
                byte[] buffer = new byte[4096];
                long received = 0;
                int bytesRead;
                
                while (received < fileSize && (bytesRead = dis.read(buffer, 0, 
                        (int) Math.min(buffer.length, fileSize - received))) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    received += bytesRead;
                }
                
                bos.close();
                fos.close();
                receivedCount++;
                
                logger.info("Archivo guardado: " + outputFile.getName());
            }
            
            logger.info("Recibidos " + receivedCount + " archivo(s) correctamente");
            
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeUTF("OK");
            
            clientSocket.close();
            
        } catch (IOException e) {
            logger.error("Error al recibir archivos", e);
        }
    }
    
    private String sanitizeFileName(String fileName) {
        fileName = new File(fileName).getName();
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (fileName.isEmpty() || fileName.equals("_")) {
            fileName = "archivo_" + System.currentTimeMillis();
        }
        return fileName;
    }
    
    private File getUniqueFileName(File file) {
        if (!file.exists()) {
            return file;
        }
        
        String name = file.getName();
        String path = file.getParent();
        String baseName;
        String extension;
        
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        } else {
            baseName = name;
            extension = "";
        }
        
        int counter = 1;
        File newFile;
        do {
            newFile = new File(path, baseName + "(" + counter + ")" + extension);
            counter++;
        } while (newFile.exists());
        
        return newFile;
    }
    
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
    
    public static void main(String[] args) {
        Server server = new Server(9090, "recibidos");
        
        // Capturamos excepciones no manejadas
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger.getInstance().error("Excepción no manejada en hilo: " + thread.getName(), throwable);
        });
        
        server.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                log("Deteniendo servidor...");
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error al cerrar el socket: " + e.getMessage());
        }
    }
}