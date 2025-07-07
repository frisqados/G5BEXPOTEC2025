package vista;

import controlador.conexion;
import util.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import forms_proyect_expotec.PanelRound;

public class PerfilPanel extends JPanel {

    private static final Color PRIMARY_TEXT_COLOR = new Color(30, 30, 30);
    private static final Color SECONDARY_TEXT_COLOR = new Color(90, 90, 90);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color BACKGROUND_COLOR = new Color(248, 248, 248);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color BUTTON_BACKGROUND = new Color(50, 50, 50);
    private static final Color BUTTON_FOREGROUND = Color.WHITE;
    private static final Color ACCENT_GREEN = new Color(46, 179, 79);
    private static final Color ACCENT_ORANGE = new Color(255, 140, 0);

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JLabel lblFechaRegistro;
    private JLabel lblAvatar;

    private JLabel lblInsigniaVentas;
    private JLabel lblInsigniaCompras;
    private JLabel lblTotalProductosVendidos;
    private JLabel lblTotalComprasRealizadas;
    private JLabel lblRachaCompras;
    private JLabel lblDescuentoPotencial;

    private JButton btnGuardarCambios;

    private String currentAvatarPath;

    public PerfilPanel() {
        initComponents();
        loadUserProfileData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 30));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout(20, 0));

        lblAvatar = new JLabel("<html><center><br><br>Cargar Foto</center></html>", SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(120, 120));
        lblAvatar.setMinimumSize(new Dimension(120, 120));
        lblAvatar.setMaximumSize(new Dimension(120, 120));
        lblAvatar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(new Color(230, 230, 230));
        lblAvatar.setForeground(SECONDARY_TEXT_COLOR);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatar.setVerticalAlignment(SwingConstants.CENTER);
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblAvatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectAndSetAvatarImage();
            }
        });

        JPanel avatarContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarContainer.setOpaque(false);
        avatarContainer.add(lblAvatar);

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setOpaque(false);
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel titleLabel = new JLabel("Tu Perfil");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titleLabel.setForeground(PRIMARY_TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerTextPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Gestiona tu información personal y revisa tus logros.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(SECONDARY_TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerTextPanel.add(subtitleLabel);

        headerPanel.add(avatarContainer, BorderLayout.WEST);
        headerPanel.add(headerTextPanel, BorderLayout.CENTER);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel mainContentGrid = new JPanel(new GridBagLayout());
        mainContentGrid.setOpaque(false);
        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.insets = new Insets(15, 15, 15, 15);
        gbcMain.fill = GridBagConstraints.BOTH;

        gbcMain.gridx = 0; gbcMain.gridy = 0; gbcMain.weightx = 1.0; gbcMain.weighty = 1.0;
        mainContentGrid.add(createUserInfoPanel(), gbcMain);

        gbcMain.gridx = 1; gbcMain.gridy = 0; gbcMain.weightx = 1.0; gbcMain.weighty = 1.0;
        mainContentGrid.add(createAchievementsPanel(), gbcMain);

        contentPanel.add(mainContentGrid, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private PanelRound createUserInfoPanel() {
        PanelRound panel = createCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel sectionTitle = createStyledLabel("Datos Personales", PRIMARY_TEXT_COLOR, new Font("Segoe UI", Font.BOLD, 22), SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(sectionTitle, gbc);

        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridwidth = 1;

        panel.add(createStyledLabel("Nombre:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 1, GridBagConstraints.WEST, 0));
        txtNombre = createStyledTextField();
        panel.add(txtNombre, getGBC(1, 1, GridBagConstraints.HORIZONTAL, 1));

        panel.add(createStyledLabel("Apellido:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 2, GridBagConstraints.WEST, 0));
        txtApellido = createStyledTextField();
        panel.add(txtApellido, getGBC(1, 2, GridBagConstraints.HORIZONTAL, 1));

        panel.add(createStyledLabel("Email:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 3, GridBagConstraints.WEST, 0));
        txtEmail = createStyledTextField();
        panel.add(txtEmail, getGBC(1, 3, GridBagConstraints.HORIZONTAL, 1));

        panel.add(createStyledLabel("Teléfono:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 4, GridBagConstraints.WEST, 0));
        txtTelefono = createStyledTextField();
        panel.add(txtTelefono, getGBC(1, 4, GridBagConstraints.HORIZONTAL, 1));

        panel.add(createStyledLabel("Miembro desde:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 5, GridBagConstraints.WEST, 0));
        lblFechaRegistro = createStyledLabel("Cargando...", PRIMARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(lblFechaRegistro, getGBC(1, 5, GridBagConstraints.HORIZONTAL, 1));

        btnGuardarCambios = new JButton("Guardar Cambios");
        btnGuardarCambios.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardarCambios.setBackground(BUTTON_BACKGROUND);
        btnGuardarCambios.setForeground(BUTTON_FOREGROUND);
        btnGuardarCambios.setFocusPainted(false);
        btnGuardarCambios.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnGuardarCambios.putClientProperty("JButton.buttonType", "roundRect");
        btnGuardarCambios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveUserProfileData();
            }
        });
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(25, 0, 0, 0);
        panel.add(btnGuardarCambios, gbc);

        return panel;
    }

    private PanelRound createAchievementsPanel() {
        PanelRound panel = createCardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel sectionTitle = createStyledLabel("Mis Logros", PRIMARY_TEXT_COLOR, new Font("Segoe UI", Font.BOLD, 22), SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(sectionTitle, gbc);

        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridwidth = 1;

        panel.add(createStyledLabel("Insignia Vendedor:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 1, GridBagConstraints.WEST, 0));
        lblInsigniaVentas = createStyledLabel("Novato del Mercado", PRIMARY_TEXT_COLOR, new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lblInsigniaVentas, getGBC(1, 1, GridBagConstraints.EAST, 1));

        panel.add(createStyledLabel("Productos Vendidos:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 2, GridBagConstraints.WEST, 0));
        lblTotalProductosVendidos = createStyledLabel("0", ACCENT_GREEN, new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lblTotalProductosVendidos, getGBC(1, 2, GridBagConstraints.EAST, 1));

        panel.add(createStyledLabel("Insignia Comprador:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 3, GridBagConstraints.WEST, 0));
        lblInsigniaCompras = createStyledLabel("Comprador Ocasional", PRIMARY_TEXT_COLOR, new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lblInsigniaCompras, getGBC(1, 3, GridBagConstraints.EAST, 1));

        panel.add(createStyledLabel("Compras Realizadas:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 4, GridBagConstraints.WEST, 0));
        lblTotalComprasRealizadas = createStyledLabel("0", ACCENT_GREEN, new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lblTotalComprasRealizadas, getGBC(1, 4, GridBagConstraints.EAST, 1));

        panel.add(createStyledLabel("Racha de Compras:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 5, GridBagConstraints.WEST, 0));
        lblRachaCompras = createStyledLabel("0 horas", ACCENT_ORANGE, new Font("Segoe UI", Font.BOLD, 18));
        panel.add(lblRachaCompras, getGBC(1, 5, GridBagConstraints.EAST, 1));

        panel.add(createStyledLabel("Descuento Potencial:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14)), getGBC(0, 6, GridBagConstraints.WEST, 0));
        lblDescuentoPotencial = createStyledLabel("0%", ACCENT_GREEN, new Font("Segoe UI", Font.BOLD, 18));
        panel.add(lblDescuentoPotencial, getGBC(1, 6, GridBagConstraints.EAST, 1));

        return panel;
    }

    private PanelRound createCardPanel() {
        PanelRound panel = new PanelRound();
        panel.setBackground(CARD_BACKGROUND);
        panel.setRoundTopLeft(15);
        panel.setRoundTopRight(15);
        panel.setRoundBottomLeft(15);
        panel.setRoundBottomRight(15);
        panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return panel;
    }

    private JLabel createStyledLabel(String text, Color foreground, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(foreground);
        label.setFont(font);
        return label;
    }

    private JLabel createStyledLabel(String text, Color foreground, Font font, int horizontalAlignment) {
        JLabel label = new JLabel(text, horizontalAlignment);
        label.setForeground(foreground);
        label.setFont(font);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textField.setBackground(BACKGROUND_COLOR);
        textField.setForeground(PRIMARY_TEXT_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setCaretColor(PRIMARY_TEXT_COLOR);
        return textField;
    }

    private GridBagConstraints getGBC(int x, int y, int anchor, double weightx) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(6, 0, 6, 0);
        if (x == 0) {
            gbc.anchor = anchor;
            gbc.weightx = weightx;
            gbc.ipadx = 10;
        } else {
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = weightx;
        }
        return gbc;
    }

    private void setAvatarImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            lblAvatar.setIcon(null);
            lblAvatar.setText("<html><center><br><br>Cargar Foto</center></html>");
            return;
        }
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            if (originalImage != null) {
                Image scaledImage = originalImage.getScaledInstance(
                    lblAvatar.getPreferredSize().width,
                    lblAvatar.getPreferredSize().height,
                    Image.SCALE_SMOOTH
                );
                lblAvatar.setIcon(new ImageIcon(scaledImage));
                lblAvatar.setText("");
            } else {
                lblAvatar.setIcon(null);
                lblAvatar.setText("<html><center><br><br>Error al Cargar</center></html>");
            }
        } catch (IOException e) {
            lblAvatar.setIcon(null);
            lblAvatar.setText("<html><center><br><br>No Encontrada</center></html>");
            System.err.println("Error al cargar imagen: " + e.getMessage());
        }
    }

    private void selectAndSetAvatarImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Foto de Perfil");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Imagen", "jpg", "jpeg", "png", "gif"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                currentAvatarPath = selectedFile.getAbsolutePath();
                setAvatarImage(currentAvatarPath);
            }
        }
    }

    public void loadUserProfileData() {
        int userId = UserSession.getCurrentUserId();
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para ver su perfil.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            updateNoSessionData();
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                updateNoSessionData();
                return;
            }

            loadUserInfo(userId, con);
            loadSalesData(userId, con);
            loadPurchaseData(userId, con);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos del perfil: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            updateNoSessionData();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión en PerfilPanel: " + finalEx.getMessage());
            }
        }
    }

    private void loadUserInfo(int userId, Connection con) throws SQLException {
        String userSql = "SELECT nombre, apellido, correo, telefono, fecha_registro, insignia_ventas, insignia_compras, racha_compras_horas, foto_perfil_ruta FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement psUser = con.prepareStatement(userSql)) {
            psUser.setInt(1, userId);
            try (ResultSet rsUser = psUser.executeQuery()) {
                if (rsUser.next()) {
                    txtNombre.setText(rsUser.getString("nombre"));
                    txtApellido.setText(rsUser.getString("apellido"));
                    txtEmail.setText(rsUser.getString("correo"));
                    txtTelefono.setText(rsUser.getString("telefono") != null && !rsUser.getString("telefono").isEmpty() ? rsUser.getString("telefono") : "");
                    lblFechaRegistro.setText(rsUser.getTimestamp("fecha_registro") != null ? rsUser.getTimestamp("fecha_registro").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");

                    lblInsigniaVentas.setText(rsUser.getString("insignia_ventas") != null ? rsUser.getString("insignia_ventas") : "Novato del Mercado");
                    lblInsigniaCompras.setText(rsUser.getString("insignia_compras") != null ? rsUser.getString("insignia_compras") : "Comprador Ocasional");
                    lblRachaCompras.setText(rsUser.getInt("racha_compras_horas") + " horas");

                    currentAvatarPath = rsUser.getString("foto_perfil_ruta");
                    setAvatarImage(currentAvatarPath);

                    btnGuardarCambios.setEnabled(true);
                } else {
                    updateNoSessionData();
                }
            }
        }
    }

    private void saveUserProfileData() {
        int userId = UserSession.getCurrentUserId();
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para guardar cambios.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String correo = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre, Apellido y Email no pueden estar vacíos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!correo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Formato de correo electrónico inválido.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String updateSql = "UPDATE usuarios SET nombre = ?, apellido = ?, correo = ?, telefono = ?, foto_perfil_ruta = ? WHERE id_usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, nombre);
                ps.setString(2, apellido);
                ps.setString(3, correo);
                ps.setString(4, telefono.isEmpty() ? null : telefono);
                ps.setString(5, currentAvatarPath);
                ps.setInt(6, userId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Perfil actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el perfil.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar el perfil: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión después de guardar: " + finalEx.getMessage());
            }
        }
    }

    private void loadSalesData(int userId, Connection con) throws SQLException {
        String totalVentasSql = "SELECT COALESCE(SUM(det.cantidad), 0) AS total_productos_vendidos " +
                                "FROM detalle_ordenes det " +
                                "JOIN productos p ON det.id_producto = p.id_producto " +
                                "JOIN ordenes o ON det.id_orden = o.id_orden " +
                                "WHERE p.id_usuario_subida = ? AND o.estado = 'completada'";
        try (PreparedStatement psTotalVentas = con.prepareStatement(totalVentasSql)) {
            psTotalVentas.setInt(1, userId);
            try (ResultSet rsTotalVentas = psTotalVentas.executeQuery()) {
                int totalProductosVendidos = 0;
                if (rsTotalVentas.next()) {
                    totalProductosVendidos = rsTotalVentas.getInt("total_productos_vendidos");
                }
                lblTotalProductosVendidos.setText(String.valueOf(totalProductosVendidos));

                String newInsigniaVentas = getInsigniaVentas(totalProductosVendidos);
                if (!newInsigniaVentas.equals(lblInsigniaVentas.getText())) {
                    updateUserAttribute(userId, "insignia_ventas", newInsigniaVentas, con);
                    lblInsigniaVentas.setText(newInsigniaVentas);
                }
            }
        }
    }

    private void loadPurchaseData(int userId, Connection con) throws SQLException {
        String summarySql = "SELECT COALESCE(COUNT(id_orden), 0) AS total_ordenes FROM ordenes WHERE id_usuario = ? AND estado = 'completada'";
        try (PreparedStatement psSummary = con.prepareStatement(summarySql)) {
            psSummary.setInt(1, userId);
            try (ResultSet rsSummary = psSummary.executeQuery()) {
                int totalOrdenesCompletadas = 0;
                if (rsSummary.next()) {
                    totalOrdenesCompletadas = rsSummary.getInt("total_ordenes");
                }
                lblTotalComprasRealizadas.setText(String.valueOf(totalOrdenesCompletadas));

                String newInsigniaCompras = getInsigniaCompras(totalOrdenesCompletadas);
                if (!newInsigniaCompras.equals(lblInsigniaCompras.getText())) {
                    updateUserAttribute(userId, "insignia_compras", newInsigniaCompras, con);
                    lblInsigniaCompras.setText(newInsigniaCompras);
                }

                double descuento = getDescuentoPorInsignia(newInsigniaCompras);
                lblDescuentoPotencial.setText(String.format("%.0f%%", descuento * 100));
            }
        }

        int newRachaHoras = calculatePurchaseStreak(userId, con);
        try {
            int currentRachaDisplay = Integer.parseInt(lblRachaCompras.getText().replace(" horas", ""));
            if (newRachaHoras != currentRachaDisplay) {
                updateUserAttribute(userId, "racha_compras_horas", String.valueOf(newRachaHoras), con);
                lblRachaCompras.setText(newRachaHoras + " horas");
            }
        } catch (NumberFormatException e) {
            updateUserAttribute(userId, "racha_compras_horas", String.valueOf(newRachaHoras), con);
            lblRachaCompras.setText(newRachaHoras + " horas");
        }
    }

    private String getInsigniaVentas(int totalProductosVendidos) {
        if (totalProductosVendidos >= 100) return "Leyenda del Comercio";
        if (totalProductosVendidos >= 50) return "Comerciante de Élite";
        if (totalProductosVendidos >= 25) return "Maestro Minorista";
        if (totalProductosVendidos >= 10) return "Vendedor Ascendente";
        return "Novato del Mercado";
    }

    private String getInsigniaCompras(int totalOrdenesCompletadas) {
        if (totalOrdenesCompletadas >= 100) return "Monarca del Consumo";
        if (totalOrdenesCompletadas >= 50) return "Acaparador de Ofertas";
        if (totalOrdenesCompletadas >= 25) return "Coleccionista Consumado";
        if (totalOrdenesCompletadas >= 10) return "Entusiasta de Compras";
        return "Comprador Ocasional";
    }

    private double getDescuentoPorInsignia(String insigniaCompras) {
        switch (insigniaCompras) {
            case "Monarca del Consumo": return 0.12;
            case "Acaparador de Ofertas": return 0.07;
            case "Coleccionista Consumado": return 0.03;
            case "Entusiasta de Compras": return 0.01;
            default: return 0.00;
        }
    }

    private int calculatePurchaseStreak(int userId, Connection con) throws SQLException {
        String sql = "SELECT o.fecha_orden FROM ordenes o " +
                     "JOIN detalle_ordenes det ON o.id_orden = det.id_orden " +
                     "WHERE o.id_usuario = ? AND o.estado = 'completada' " +
                     "GROUP BY o.id_orden, o.fecha_orden " +
                     "HAVING SUM(det.cantidad) >= 2 " +
                     "ORDER BY o.fecha_orden ASC";
        List<LocalDateTime> validPurchaseTimes = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    validPurchaseTimes.add(rs.getTimestamp("fecha_orden").toLocalDateTime());
                }
            }
        }

        if (validPurchaseTimes.isEmpty()) return 0;
        if (validPurchaseTimes.size() == 1) {
            long hours = Duration.between(validPurchaseTimes.get(0), LocalDateTime.now()).toHours();
            return (int) hours;
        }

        int maxStreakHours = 0;
        int currentStreakStartIdx = 0;

        for (int i = 1; i < validPurchaseTimes.size(); i++) {
            LocalDateTime prevTime = validPurchaseTimes.get(i - 1);
            LocalDateTime currTime = validPurchaseTimes.get(i);
            long hoursBetween = Duration.between(prevTime, currTime).toHours();

            if (hoursBetween <= 24) {
            } else {
                int streakDuration = (int) Duration.between(validPurchaseTimes.get(currentStreakStartIdx), prevTime).toHours();
                maxStreakHours = Math.max(maxStreakHours, streakDuration);
                currentStreakStartIdx = i;
            }
        }
        int lastStreakDuration = (int) Duration.between(validPurchaseTimes.get(currentStreakStartIdx), LocalDateTime.now()).toHours();
        maxStreakHours = Math.max(maxStreakHours, lastStreakDuration);

        return Math.max(0, maxStreakHours);
    }

    private void updateUserAttribute(int userId, String columnName, String value, Connection con) {
        String updateSql = "UPDATE usuarios SET " + columnName + " = ? WHERE id_usuario = ?";
        try (PreparedStatement psUpdate = con.prepareStatement(updateSql)) {
            if (columnName.equals("racha_compras_horas")) {
                psUpdate.setInt(1, Integer.parseInt(value));
            } else {
                psUpdate.setString(1, value);
            }
            psUpdate.setInt(2, userId);
            psUpdate.executeUpdate();
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error al actualizar atributo '" + columnName + "' para el usuario " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateNoSessionData() {
        txtNombre.setText("N/A");
        txtApellido.setText("N/A");
        txtEmail.setText("N/A");
        txtTelefono.setText("N/A");
        lblFechaRegistro.setText("N/A");

        lblTotalProductosVendidos.setText("0");
        lblTotalComprasRealizadas.setText("0");
        lblInsigniaVentas.setText("N/A");
        lblInsigniaCompras.setText("N/A");
        lblRachaCompras.setText("0 horas");
        lblDescuentoPotencial.setText("0%");

        if (lblAvatar != null) lblAvatar.setText("<html><center><br><br>Sin Usuario</center></html>");
        lblAvatar.setIcon(null);
        btnGuardarCambios.setEnabled(false);
    }

    public void refreshData() {
        loadUserProfileData();
    }
}