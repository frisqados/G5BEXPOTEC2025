package forms_proyect_expotec;

import com.formdev.flatlaf.FlatDarkLaf; // Importa FlatDarkLaf
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import util.UserSession; // Asegúrate de que esta importación sea correcta si usas UserSession al inicio

public class SplashScreen extends JFrame {

    private Timer timer;
    private float opacity = 1.0f;

    public SplashScreen() {
        // Establecer el Look and Feel de FlatLaf antes de inicializar componentes Swing
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatDarkLaf: " + e.getMessage());
            // Manejar el error, quizás usar el L&F por defecto o notificar al usuario
        }

        setUndecorated(true);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel principal que contendrá el logo y el mensaje de bienvenida
        JPanel panel = new JPanel();
        panel.setBackground(new Color(25, 25, 25)); // Fondo oscuro para FlatDarkLaf
        panel.setLayout(new GridBagLayout()); // Usar GridBagLayout para centrar y organizar mejor
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Menos padding para no empujar tanto

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0); // Espacio entre componentes
        gbc.anchor = GridBagConstraints.CENTER;

        // Carga y muestra el logo como imagen
        ImageIcon logoIcon = null;
        try {
            // Usa el ClassLoader para cargar el recurso, es más robusto
            java.net.URL imageUrl = getClass().getResource("/Image/logo.png");
            if (imageUrl != null) {
                Image originalImage = new ImageIcon(imageUrl).getImage();
                logoIcon = new ImageIcon(originalImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH)); // Ajusta tamaño
            } else {
                System.err.println("Error: No se pudo cargar la imagen del logo. Verifique la ruta del recurso: /Image/logo.png");
            }
        } catch (Exception e) {
            System.err.println("Excepción al cargar el logo: " + e.getMessage());
        }
        
        JLabel logoLabel;
        if (logoIcon != null) {
            logoLabel = new JLabel(logoIcon);
        } else {
            logoLabel = new JLabel("<html><font color='red'>Logo no disponible</font></html>"); // Mensaje de error más visible
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        }
        panel.add(logoLabel, gbc);

        // Label de bienvenida
        JLabel label = new JLabel("¡Bienvenido a Metshop!");
        label.setForeground(Color.WHITE); // Texto blanco para contraste con fondo oscuro
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        gbc.gridy = 1; // Ubica el mensaje debajo del logo
        panel.add(label, gbc);

        add(panel, BorderLayout.CENTER);

        // Configuración del temporizador para el fade out
        Timer delayTimer = new Timer(3000, e -> { // Espera 3 segundos
            ((Timer) e.getSource()).stop(); // Detiene este timer
            startFadeOut(); // Inicia el efecto de desvanecimiento
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    private void startFadeOut() {
        timer = new Timer(50, new ActionListener() { // Timer para el fade out gradual
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f; // Reduce la opacidad
                if (opacity <= 0) {
                    timer.stop();
                    dispose(); // Cierra esta ventana splash
                    
                    // Abrir la ventana principal o de login
                    SwingUtilities.invokeLater(() -> {
                        // Antes de abrir LoginForm, podrías verificar si el usuario ya está logueado
                        // Si tienes un UserSession.isLoggedIn()
                        if (UserSession.isLoggedIn()) {
                            new PrincipalForm().setVisible(true);
                        } else {
                            new LoginForm().setVisible(true);
                        }
                    });
                } else {
                    setOpacity(opacity); // Aplica la nueva opacidad
                }
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        // Asegúrate de que FlatLaf esté inicializado antes de crear el SplashScreen
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatDarkLaf in main: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new SplashScreen().setVisible(true);
        });
    }
}