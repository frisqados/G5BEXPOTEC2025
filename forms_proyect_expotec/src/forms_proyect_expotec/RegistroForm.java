package forms_proyect_expotec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.border.EmptyBorder;
import java.awt.geom.RoundRectangle2D;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import controlador.conexion;
import util.UserSession;

public class RegistroForm extends JFrame {

    private JPanel panelLateral;
    private JPanel panelCampos;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblFechaActual;

    public RegistroForm() {
        setTitle("Registro de Usuario");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


        panelLateral = new JPanel();
        panelLateral.setBackground(Color.BLACK);
        panelLateral.setPreferredSize(new Dimension(600, 700));
        panelLateral.setLayout(new GridBagLayout());


        ImageIcon logoIcon = new ImageIcon(new ImageIcon("src/Image/logo.png").getImage().getScaledInstance(450, 450, Image.SCALE_SMOOTH));
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

        JLabel lblTitulo = new JLabel("Crear una Nueva Cuenta");
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
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(Color.WHITE);
        panelCampos.add(lblNombre, gbc);
        gbc.gridx = 1;
        txtNombre = new RoundedTextField(20);
        panelCampos.add(txtNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblApellido = new JLabel("Apellido:");
        lblApellido.setForeground(Color.WHITE);
        panelCampos.add(lblApellido, gbc);
        gbc.gridx = 1;
        txtApellido = new RoundedTextField(20);
        panelCampos.add(txtApellido, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblEmail = new JLabel("Correo:");
        lblEmail.setForeground(Color.WHITE);
        panelCampos.add(lblEmail, gbc);
        gbc.gridx = 1;
        txtEmail = new RoundedTextField(20);
        panelCampos.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setForeground(Color.WHITE);
        panelCampos.add(lblPassword, gbc);
        gbc.gridx = 1;
        txtPassword = new RoundedPasswordField(20);
        panelCampos.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel lblConfirmPassword = new JLabel("Confirmar Contraseña:");
        lblConfirmPassword.setForeground(Color.WHITE);
        panelCampos.add(lblConfirmPassword, gbc);
        gbc.gridx = 1;
        txtConfirmPassword = new RoundedPasswordField(20);
        panelCampos.add(txtConfirmPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton btnRegistrar = new RoundedButton("Registrarse");
        btnRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarUsuario();
            }
        });
        panelCampos.add(btnRegistrar, gbc);

        gbc.gridy = 8;
        JButton btnYaTengoCuenta = new JButton("¿Ya tienes una cuenta? Inicia Sesión aquí");
        btnYaTengoCuenta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnYaTengoCuenta.setForeground(Color.WHITE);
        btnYaTengoCuenta.setBackground(new Color(60, 63, 65));
        btnYaTengoCuenta.setBorderPainted(false);
        btnYaTengoCuenta.setFocusPainted(false);
        btnYaTengoCuenta.setContentAreaFilled(false);
        btnYaTengoCuenta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYaTengoCuenta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(RegistroForm.this, "Redirigiendo a la pantalla de inicio de sesión...");
                new form2().setVisible(rootPaneCheckingEnabled);
                dispose();
            }
        });
        panelCampos.add(btnYaTengoCuenta, gbc);

        add(panelCampos, BorderLayout.CENTER);
    }

    private void obtenerFechaActual() {
        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy");
        lblFechaActual.setText("Fecha: " + fecha.format(formatter));
    }

    private boolean verificarUsuarioExistente(String correo) {
        conexion conn = new conexion();
        conn.conectar();
        String sql = "SELECT COUNT(*) FROM usuarios WHERE correo = ?";
        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, correo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al verificar usuario existente: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            conn.desconectar();
        }
        return false;
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String correo = txtEmail.getText().trim();
        String contrasenia = new String(txtPassword.getPassword());
        String confirmContrasenia = new String(txtConfirmPassword.getPassword());

        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || contrasenia.isEmpty() || confirmContrasenia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!contrasenia.equals(confirmContrasenia)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error de Contraseña", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!correo.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un formato de correo electrónico válido.", "Correo Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (verificarUsuarioExistente(correo)) {
            JOptionPane.showMessageDialog(this, "El correo electrónico ya está registrado. Por favor, intente con otro.", "Usuario Existente", JOptionPane.WARNING_MESSAGE);
            return;
        }


        conexion conn = new conexion();
        conn.conectar();

        String sql = "INSERT INTO usuarios (nombre, apellido, correo, contraseña) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.setString(3, correo);
            pstmt.setString(4, contrasenia);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Usuario registrado exitosamente!", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

                txtNombre.setText("");
                txtApellido.setText("");
                txtEmail.setText("");
                txtPassword.setText("");
                txtConfirmPassword.setText("");

            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el usuario.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar usuario: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            conn.desconectar();
        }
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
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            super.paintComponent(g2);
            g2.dispose();
        }
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            g2.dispose();
        }
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15);
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
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            super.paintComponent(g2);
            g2.dispose();
        }
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            g2.dispose();
        }
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 15, 15);
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
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            super.paintComponent(g2);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            g2.dispose();
        }

        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
            return shape.contains(x, y);
        }
    }
}