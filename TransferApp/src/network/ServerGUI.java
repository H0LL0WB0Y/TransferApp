package network;

import network.utils.Logger;
import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private Logger logger = Logger.getInstance("server_gui.log");
    private Server server;
    private JTextArea logArea;
    private JLabel ipLabel;
    private JButton startStopButton;
    private boolean isRunning = false;

    public ServerGUI() {
        setTitle("TransferApp - Servidor PC");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla

        // --- Diseño de la interfaz ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: IP y Puerto
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String ip = NetworkUtils.getLocalIPAddress();
        ipLabel = new JLabel("IP: " + ip + " | Puerto: 9090", SwingConstants.CENTER);
        ipLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(ipLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel central: Área de logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior: Botón y espacio para el QR (por ahora un placeholder)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startStopButton = new JButton("Iniciar Servidor");
        startStopButton.addActionListener(e -> toggleServer());
        
        // Placeholder para el QR
        JLabel qrPlaceholder = new JLabel("[ Aquí irá el Código QR ]");
        qrPlaceholder.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        qrPlaceholder.setPreferredSize(new Dimension(150, 150));
        
        bottomPanel.add(startStopButton);
        bottomPanel.add(qrPlaceholder);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        log("Sistema listo. IP detectada: " + ip);
    }

    private void toggleServer() {
        if (!isRunning) {
            try {
                server = new Server(9090, "recibidos", this::log);
                // Iniciamos el servidor en un hilo separado para no congelar la interfaz
                new Thread(() -> server.start()).start();
                
                startStopButton.setText("Detener Servidor");
                startStopButton.setBackground(Color.RED);
                startStopButton.setForeground(Color.WHITE);
                isRunning = true;
                log("Servidor iniciado correctamente.");
                
                // AQUÍ GENERAREMOS EL QR EN EL PRÓXIMO PASO
                
            } catch (Exception e) {
                log("ERROR: " + e.getMessage());
            }
        } else {
            if (server != null) {
                server.stop();
            }
            startStopButton.setText("Iniciar Servidor");
            startStopButton.setBackground(null);
            startStopButton.setForeground(null);
            isRunning = false;
            log("Servidor detenido.");
        }
    }

    // Método para que el servidor pueda escribir en la interfaz
    public void log(String message) {
        // Swing requiere que las actualizaciones de UI se hagan en el hilo de eventos
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll al final
        });
        // También lo guardamos en el archivo de log
        Logger.getInstance().info(message);
    }

    public static void main(String[] args) {
        // Establecemos el manejador de excepciones global
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger.getInstance().error("Excepción no manejada: " + throwable.getMessage(), throwable);
        });

        // Creamos la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}