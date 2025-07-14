package forms_proyect_expotec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.border.EmptyBorder;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import controlador.conexion;
import util.UserSession;
import forms_proyect_expotec.FormularioRecuperarContrasena;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import util.LimitDocumentFilter;

public class LoginForm extends JFrame {

    private JPanel panelLateral;
    private JPanel panelCampos;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JLabel lblFechaActual;
    private JLabel lblTogglePassword;

    // Define el límite de caracteres
    private static final int MAX_CHARS = 50;

    public LoginForm() {
        setTitle("Inicio de Sesión");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        panelLateral = new JPanel();
        panelLateral.setBackground(Color.BLACK);
        panelLateral.setPreferredSize(new Dimension(600, 700));
        panelLateral.setLayout(new GridBagLayout());

        ImageIcon logoIcon = null;
        try {
            Image originalImage = new ImageIcon(getClass().getResource("/Image/logo.png")).getImage();
            if (originalImage != null) {
                logoIcon = new ImageIcon(originalImage.getScaledInstance(450, 450, Image.SCALE_SMOOTH));
            } else {
                System.err.println("Error: No se pudo cargar la imagen del logo. Verifique la ruta del recurso.");
            }
        } catch (Exception e) {
            System.err.println("Excepción al cargar el logo: " + e.getMessage());
        }

        JLabel lblLogo = new JLabel(logoIcon);
        panelLateral.add(lblLogo);
        add(panelLateral, BorderLayout.WEST);

        panelCampos = new JPanel();
        panelCampos.setBackground(new Color(60, 63, 65));
        panelCampos.setLayout(new GridBagLayout());
        panelCampos.setBorder(new EmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Bienvenido de Nuevo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelCampos.add(lblTitulo, gbc);

        lblFechaActual = new JLabel();
        lblFechaActual.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFechaActual.setForeground(Color.WHITE);
        gbc.gridy = 1;
        panelCampos.add(lblFechaActual, gbc);
        obtenerFechaActual();

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel lblEmail = new JLabel("Correo:");
        lblEmail.setForeground(Color.WHITE);
        panelCampos.add(lblEmail, gbc);
        gbc.gridx = 1;
        txtEmail = new RoundedTextField(20);
        // Aplica el DocumentFilter al campo de correo
        ((AbstractDocument) txtEmail.getDocument()).setDocumentFilter(new LimitDocumentFilter(MAX_CHARS));
        panelCampos.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setForeground(Color.WHITE);
        panelCampos.add(lblPassword, gbc);

        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setOpaque(false);
        txtPassword = new RoundedPasswordField(20);
        // Aplica el DocumentFilter al campo de contraseña
        ((AbstractDocument) txtPassword.getDocument()).setDocumentFilter(new LimitDocumentFilter(MAX_CHARS));
        passwordPanel.add(txtPassword, BorderLayout.CENTER);

        lblTogglePassword = new JLabel();
        lblTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblTogglePassword.setPreferredSize(new Dimension(30, 0));
        lblTogglePassword.setHorizontalAlignment(SwingConstants.CENTER);

        // Texto de fallback para el icono si no se carga la imagen
        lblTogglePassword.setText("👁️");
        lblTogglePassword.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblTogglePassword.setForeground(Color.WHITE);

        lblTogglePassword.addMouseListener(new MouseAdapter() {
            private boolean passwordVisible = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (passwordVisible) {
                    txtPassword.setEchoChar('*');
                    lblTogglePassword.setText("👁️");
                } else {
                    txtPassword.setEchoChar((char) 0);
                    lblTogglePassword.setText("🚫");
                }
                passwordVisible = !passwordVisible;
            }
        });
        passwordPanel.add(lblTogglePassword, BorderLayout.EAST);
        panelCampos.add(passwordPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton btnLogin = new RoundedButton("Iniciar Sesión");
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    iniciarSesion();
                } catch (SQLException ex) {
                    Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        panelCampos.add(btnLogin, gbc);

        gbc.gridy = 5;
        JButton btnNoTengoCuenta = new JButton("¿No tienes una cuenta? Regístrate aquí");
        btnNoTengoCuenta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNoTengoCuenta.setForeground(Color.WHITE);
        btnNoTengoCuenta.setBackground(new Color(60, 63, 65));
        btnNoTengoCuenta.setBorderPainted(false);
        btnNoTengoCuenta.setFocusPainted(false);
        btnNoTengoCuenta.setContentAreaFilled(false);
        btnNoTengoCuenta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNoTengoCuenta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegistroForm().setVisible(true);
                dispose();
            }
        });
        panelCampos.add(btnNoTengoCuenta, gbc);

        gbc.gridy = 6;
        JButton btnOlvideContrasena = new JButton("¿Olvidaste tu contraseña?");
        btnOlvideContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnOlvideContrasena.setForeground(Color.WHITE);
        btnOlvideContrasena.setBackground(new Color(60, 63, 65));
        btnOlvideContrasena.setBorderPainted(false);
        btnOlvideContrasena.setFocusPainted(false);
        btnOlvideContrasena.setContentAreaFilled(false);
        btnOlvideContrasena.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOlvideContrasena.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FormularioRecuperarContrasena().setVisible(true);
                dispose();
            }
        });
        panelCampos.add(btnOlvideContrasena, gbc);

        add(panelCampos, BorderLayout.CENTER);
    }

    private void obtenerFechaActual() {
        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "GT"));
        lblFechaActual.setText("Fecha: " + fecha.format(formatter));
    }

    private void iniciarSesion() throws SQLException {
        String correo = txtEmail.getText().trim();
        String contrasenia = new String(txtPassword.getPassword());

        if (correo.isEmpty() || contrasenia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa tu correo y contraseña.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ya tienes una validación de formato de correo, esta es buena.
        if (!correo.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa un formato de correo electrónico válido.", "Correo Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Agrega una validación explícita de longitud aquí también, aunque el DocumentFilter ya lo limita visualmente
        if (correo.length() > MAX_CHARS || contrasenia.length() > MAX_CHARS) {
            JOptionPane.showMessageDialog(this, "El correo o la contraseña exceden el límite de " + MAX_CHARS + " caracteres.", "Límite de Caracteres Excedido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection connection = new conexion().getConnection()) {
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT id_usuario, nombre, correo FROM usuarios WHERE correo = ? AND contraseña = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, correo);
                pstmt.setString(2, contrasenia);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("id_usuario");
                        String userName = rs.getString("nombre");
                        String userEmail = rs.getString("correo");

                        UserSession.login(userId, userName, userEmail);

                        JOptionPane.showMessageDialog(this, "¡Bienvenido, " + userName + "!", "Inicio de Sesión Exitoso", JOptionPane.INFORMATION_MESSAGE);

                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                // Aquí puedes hacer tareas pesadas si las hubiera
                                Thread.sleep(1200); // Simulación de carga
                                return null;
                            }

                            @Override
                           protected void done() { 
                               
                                new PrincipalForm().setVisible(true);
                                dispose();
                            }
                        };
                        worker.execute();
                   
                    } else {
                        JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al iniciar sesión: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (UserSession.isLoggedIn()) {
                new PrincipalForm().setVisible(true);
            } else {
                new LoginForm().setVisible(true);
            }
        });
    }

    class RoundedTextField extends JTextField {

        private Shape shape;

        public RoundedTextField(int size) {
            super(size);
            setOpaque(false);
            setBackground(new Color(80, 83, 85));
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            super.paintComponent(g2);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.dispose();
        }

        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
            return shape.contains(x, y);
        }
    }

    class RoundedPasswordField extends JPasswordField {

        private Shape shape;

        public RoundedPasswordField(int size) {
            super(size);
            setOpaque(false);
            setBackground(new Color(80, 83, 85));
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            super.paintComponent(g2);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.dispose();
        }

        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
            return shape.contains(x, y);
        }
    }

    class RoundedButton extends JButton {

        private Shape shape;

        public RoundedButton(String label) {
            super(label);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isArmed()) {
                g2.setColor(Color.DARK_GRAY);
            } else {
                g2.setColor(getBackground());
            }
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            super.paintComponent(g2);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }

        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
            return shape.contains(x, y);
        }
    }
}
