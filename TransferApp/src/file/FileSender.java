package file;

import network.utils.Logger;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class FileSender {
    private static Logger logger = Logger.getInstance("client.log");
    
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 9090;
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setDialogTitle("Selecciona los archivos a enviar");
            
            int result = fileChooser.showOpenDialog(null);
            
            if (result != JFileChooser.APPROVE_OPTION) {
                logger.info("Selección cancelada por el usuario");
                return;
            }
            
            File[] selectedFiles = fileChooser.getSelectedFiles();
            
            if (selectedFiles.length == 0) {
                logger.info("No se seleccionaron archivos");
                return;
            }
            
            logger.info("Archivos seleccionados: " + selectedFiles.length);
            for (File f : selectedFiles) {
                logger.info("  - " + f.getName() + " (" + formatSize(f.length()) + ")");
            }
            
            Socket socket = new Socket(serverAddress, serverPort);
            logger.info("Conectado al servidor " + serverAddress + ":" + serverPort);
            
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(selectedFiles.length);
            
            long totalSize = 0;
            for (File f : selectedFiles) {
                totalSize += f.length();
            }
            logger.info("Tamaño total: " + formatSize(totalSize));
            
            for (int i = 0; i < selectedFiles.length; i++) {
                File file = selectedFiles[i];
                
                logger.info("[" + (i + 1) + "/" + selectedFiles.length + "] Enviando: " + file.getName());
                
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                
                byte[] buffer = new byte[4096];
                long sent = 0;
                int bytesRead;
                
                while ((bytesRead = bis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                    sent += bytesRead;
                }
                
                bis.close();
                fis.close();
                
                logger.info("Archivo enviado: " + file.getName());
            }
            
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String response = dis.readUTF();
            
            if (response.equals("OK")) {
                logger.info("Todos los archivos enviados correctamente");
            } else {
                logger.error("El servidor respondió: " + response);
            }
            
            socket.close();
            
        } catch (IOException e) {
            logger.error("Error al enviar archivos", e);
        }
    }
    
    private static String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}