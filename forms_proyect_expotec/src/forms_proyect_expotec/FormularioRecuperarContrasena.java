package forms_proyect_expotec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import controlador.conexion;
import util.EnviarCorreoJakarta;
import util.GeneradorToken;

public class FormularioRecuperarContrasena extends JFrame {

    private JPanel panelCentral;
    private CardLayout cardLayout;
    private JTextField txtEmailRecuperacion;
    private JTextField txtCodigoVerificacion;
    private JPasswordField txtNuevaContrasena;
    private JPasswordField txtConfirmarNuevaContrasena;

    private String codigoGenerado;
    private String emailUsuarioVerificado;

    public FormularioRecuperarContrasena() {
        setTitle("Recuperar Contraseña");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setBackground(new Color(60, 63, 65));

        // Panel Solicitar Correo
        JPanel panelSolicitarCorreo = crearPanelBase();
        JLabel lblTituloCorreo = new JLabel("Recuperar Contraseña");
        lblTituloCorreo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloCorreo.setForeground(Color.WHITE);
        agregarComponenteCentrado(panelSolicitarCorreo, lblTituloCorreo, 0, 0, 2);

        JLabel lblInstruccionCorreo = new JLabel("Ingresa tu correo electrónico para recibir un código de verificación:");
        lblInstruccionCorreo.setForeground(Color.WHITE);
        agregarComponenteCentrado(panelSolicitarCorreo, lblInstruccionCorreo, 0, 1, 2);

        JLabel lblEmail = new JLabel("Correo:");
        lblEmail.setForeground(Color.WHITE);
        agregarComponenteAlineadoIzquierda(panelSolicitarCorreo, lblEmail, 0, 2);
        txtEmailRecuperacion = new CampoTextoRedondeado(25);
        agregarComponenteCentrado(panelSolicitarCorreo, txtEmailRecuperacion, 1, 2, 1);

        BotonRedondeado btnEnviarCodigo = new BotonRedondeado("Enviar Código");
        btnEnviarCodigo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarCodigo();
            }
        });
        agregarComponenteCentrado(panelSolicitarCorreo, btnEnviarCodigo, 0, 3, 2);
        panelCentral.add(panelSolicitarCorreo, "SOLICITAR_CORREO");

        // Panel Ingresar Código
        JPanel panelIngresarCodigo = crearPanelBase();
        JLabel lblTituloCodigo = new JLabel("Verificación de Código");
        lblTituloCodigo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloCodigo.setForeground(Color.WHITE);
        agregarComponenteCentrado(panelIngresarCodigo, lblTituloCodigo, 0, 0, 2);

        JLabel lblInstruccionCodigo = new JLabel("Ingresa el código de 6 dígitos que enviamos a tu correo:");
        lblInstruccionCodigo.setForeground(Color.WHITE);
        agregarComponenteCentrado(panelIngresarCodigo, lblInstruccionCodigo, 0, 1, 2);

        JLabel lblCodigo = new JLabel("Código:");
        lblCodigo.setForeground(Color.WHITE);
        agregarComponenteAlineadoIzquierda(panelIngresarCodigo, lblCodigo, 0, 2);
        txtCodigoVerificacion = new CampoTextoRedondeado(10);
        agregarComponenteCentrado(panelIngresarCodigo, txtCodigoVerificacion, 1, 2, 1);

        BotonRedondeado btnVerificarCodigo = new BotonRedondeado("Verificar Código");
        btnVerificarCodigo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verificarCodigo();
            }
        });
        agregarComponenteCentrado(panelIngresarCodigo, btnVerificarCodigo, 0, 3, 2);
        panelCentral.add(panelIngresarCodigo, "INGRESAR_CODIGO");

        // Panel Cambiar Contraseña
        JPanel panelCambiarContrasena = crearPanelBase();
        JLabel lblTituloCambiar = new JLabel("Cambiar Contraseña");
        lblTituloCambiar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTituloCambiar.setForeground(Color.WHITE);
        agregarComponenteCentrado(panelCambiarContrasena, lblTituloCambiar, 0, 0, 2);

        JLabel lblInstruccionCambiar = new JLabel("Ingresa tu nueva contraseña:");
        lblInstruccionCambiar.setForeground(Color.WHITE);
        agregarComponenteCentrado(panelCambiarContrasena, lblInstruccionCambiar, 0, 1, 2);

        JLabel lblNuevaContrasena = new JLabel("Nueva Contraseña:");
        lblNuevaContrasena.setForeground(Color.WHITE);
        agregarComponenteAlineadoIzquierda(panelCambiarContrasena, lblNuevaContrasena, 0, 2);
        txtNuevaContrasena = new CampoContrasenaRedondeado(20);
        agregarComponenteCentrado(panelCambiarContrasena, txtNuevaContrasena, 1, 2, 1);

        JLabel lblConfirmarNuevaContrasena = new JLabel("Confirmar Contraseña:");
        lblConfirmarNuevaContrasena.setForeground(Color.WHITE);
        agregarComponenteAlineadoIzquierda(panelCambiarContrasena, lblConfirmarNuevaContrasena, 0, 3);
        txtConfirmarNuevaContrasena = new CampoContrasenaRedondeado(20);
        agregarComponenteCentrado(panelCambiarContrasena, txtConfirmarNuevaContrasena, 1, 3, 1);

        BotonRedondeado btnCambiarContrasena = new BotonRedondeado("Cambiar Contraseña");
        btnCambiarContrasena.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarContrasena();
            }
        });
        agregarComponenteCentrado(panelCambiarContrasena, btnCambiarContrasena, 0, 4, 2);
        panelCentral.add(panelCambiarContrasena, "CAMBIAR_CONTRASENA");

        add(panelCentral, BorderLayout.CENTER);
        cardLayout.show(panelCentral, "SOLICITAR_CORREO");
    }

    private JPanel crearPanelBase() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(60, 63, 65));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        return panel;
    }

    private void agregarComponenteCentrado(JPanel panel, JComponent comp, int gridx, int gridy, int gridwidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        panel.add(comp, gbc);
    }

    private void agregarComponenteAlineadoIzquierda(JPanel panel, JComponent comp, int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        panel.add(comp, gbc);
    }

    private void enviarCodigo() {
        emailUsuarioVerificado = txtEmailRecuperacion.getText().trim();

        if (emailUsuarioVerificado.isEmpty() || !emailUsuarioVerificado.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa un correo electrónico válido.", "Correo Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection connection = new conexion().getConnection()) {
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para verificar el correo.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sql = "SELECT COUNT(*) FROM usuarios WHERE correo = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, emailUsuarioVerificado);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        codigoGenerado = GeneradorToken.generarCodigoNumerico();
                        String asunto = "Código de Recuperación de Contraseña";
                        String cuerpo = "Tu código de verificación para restablecer la contraseña es: " + codigoGenerado + "\n\nEste código es válido por un tiempo limitado.";

                        new SwingWorker<Boolean, Void>() {
                            @Override
                            protected Boolean doInBackground() throws Exception {
                                return EnviarCorreoJakarta.enviarEmail(emailUsuarioVerificado, asunto, cuerpo);
                            }

                            @Override
                            protected void done() {
                                try {
                                    if (get()) {
                                        JOptionPane.showMessageDialog(FormularioRecuperarContrasena.this, "Código enviado a tu correo electrónico.", "Código Enviado", JOptionPane.INFORMATION_MESSAGE);
                                        cardLayout.show(panelCentral, "INGRESAR_CODIGO");
                                        txtCodigoVerificacion.setText("");
                                    } else {
                                        JOptionPane.showMessageDialog(FormularioRecuperarContrasena.this, "Error al enviar el código.", "Error de Envío", JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(FormularioRecuperarContrasena.this, "Error inesperado al enviar el código: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    ex.printStackTrace();
                                }
                            }
                        }.execute();

                    } else {
                        JOptionPane.showMessageDialog(this, "El correo electrónico no está registrado.", "Correo No Encontrado", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al verificar correo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void verificarCodigo() {
        String codigoIngresado = txtCodigoVerificacion.getText().trim();

        if (codigoIngresado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa el código de verificación.", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (codigoGenerado != null && codigoIngresado.equals(codigoGenerado)) {
            JOptionPane.showMessageDialog(this, "Código verificado exitosamente. Ahora puedes cambiar tu contraseña.", "Código Correcto", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(panelCentral, "CAMBIAR_CONTRASENA");
            txtNuevaContrasena.setText("");
            txtConfirmarNuevaContrasena.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Código incorrecto. Intenta de nuevo.", "Código Incorrecto", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarContrasena() {
        String nuevaContrasena = new String(txtNuevaContrasena.getPassword());
        String confirmarContrasena = new String(txtConfirmarNuevaContrasena.getPassword());

        if (nuevaContrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa y confirma tu nueva contraseña.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error de Contraseña", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = new conexion().getConnection()) {
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cambiar la contraseña.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "UPDATE usuarios SET contraseña = ? WHERE correo = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nuevaContrasena);
                pstmt.setString(2, emailUsuarioVerificado);
                int filasAfectadas = pstmt.executeUpdate();

                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Contraseña actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    new LoginForm().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar la contraseña. Usuario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al cambiar contraseña: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    //  Clases internas para componentes redondeados 

    class CampoTextoRedondeado extends JTextField {
        private Shape shape;

        public CampoTextoRedondeado(int size) {
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

    class CampoContrasenaRedondeado extends JPasswordField {
        private Shape shape;

        public CampoContrasenaRedondeado(int size) {
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

    class BotonRedondeado extends JButton {
        private Shape shape;

        public BotonRedondeado(String label) {
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
