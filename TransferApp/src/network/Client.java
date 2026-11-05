package network;

import network.utils.Logger;
import java.net.Socket;
import java.io.IOException;

public class Client {
    private String serverAddress;
    private int serverPort;
    
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
    
    public void connect() {
        try {
            // Nos conectamos al servidor
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Conectado al servidor: " + serverAddress);
            
            // Aquí enviarías archivos
            // Por ahora solo cerramos la conexión
            socket.close();
            
        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Cambia "localhost" por la IP de tu PC si pruebas desde otro dispositivo
        Client client = new Client("localhost", 9090);
        client.connect();
    }
}