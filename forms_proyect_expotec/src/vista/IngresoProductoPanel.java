package vista;

import modelo.Producto; // Make sure this Producto class has appropriate constructors or setters if you use it for other purposes.
import controlador.conexion; // Import your connection class
import util.UserSession; // Import the UserSession class to detect the logged-in user

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IngresoProductoPanel extends JPanel { // CHANGED: Now extends JPanel
    private JTextField txtNombre, txtPrecio, txtStock, txtCategoria;
    private JTextArea txtDescripcion;
    private JLabel lblImagenPreview;
    private byte[] imagenBytes;

    // Define the color palette
    private final Color PRIMARY_DARK = new Color(30, 30, 30);   // Dark for headers
    private final Color SECONDARY_DARK = new Color(50, 50, 50); // Darker gray for labels
    private final Color ACCENT_ORANGE = new Color(255, 153, 0); // Primary accent
    private final Color ACCENT_ORANGE_LIGHT = new Color(255, 164, 28); // Lighter accent
    private final Color BACKGROUND_LIGHT = new Color(245, 245, 245); // Light gray background for form
    private final Color TEXT_COLOR = new Color(20, 20, 20); // Very dark gray for general text
    private final Color BORDER_GRAY = new Color(200, 200, 200); // Light gray for borders

    public IngresoProductoPanel() {
        setLayout(new BorderLayout(20, 20)); // Add some padding around the panel
        setBackground(BACKGROUND_LIGHT); // Set overall panel background

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(PRIMARY_DARK); // Dark header background
        JLabel titleLabel = new JLabel("Ingreso de Productos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28)); // Slightly larger font
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding inside the form
        formPanel.setBackground(BACKGROUND_LIGHT); // Light gray background for the form

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Components fill their display area horizontally

        addRow(formPanel, gbc, "Nombre del Producto:", txtNombre = new JTextField(30), 0);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align label to top-left
        formPanel.add(createLabel("Descripción:", new Font("SansSerif", Font.BOLD, 14)), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; // Allow description text area to expand
        gbc.weighty = 0.5; // Allow description to take more vertical space
        txtDescripcion = new JTextArea(5, 30);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtDescripcion.setBorder(BorderFactory.createLineBorder(BORDER_GRAY)); // Add border to text area
        JScrollPane scrollPane = new JScrollPane(txtDescripcion);
        scrollPane.setPreferredSize(new Dimension(300, 80)); // Preferred size for the scroll pane
        formPanel.add(scrollPane, gbc);
        gbc.weightx = 0; // Reset weightx
        gbc.weighty = 0; // Reset weighty
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor for subsequent rows

        addRow(formPanel, gbc, "Precio ($):", txtPrecio = new JTextField(15), 2);
        addRow(formPanel, gbc, "Stock:", txtStock = new JTextField(10), 3);
        addRow(formPanel, gbc, "Categoría:", txtCategoria = new JTextField(20), 4);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(createLabel("Imagen del Producto:", new Font("SansSerif", Font.BOLD, 14)), gbc);

        gbc.gridx = 1;
        JPanel imageUploadPanel = new JPanel(new BorderLayout(10, 0));
        imageUploadPanel.setBackground(formPanel.getBackground());
        JButton btnCargarImagen = new JButton("Cargar Imagen");
        btnCargarImagen.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnCargarImagen.setBackground(ACCENT_ORANGE_LIGHT); // Orange color for button
        btnCargarImagen.setForeground(TEXT_COLOR); // Use dark text for button
        btnCargarImagen.setFocusPainted(false);
        btnCargarImagen.setBorderPainted(false); // No default border
        btnCargarImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCargarImagen.setPreferredSize(new Dimension(120, 30)); // Smaller button

        lblImagenPreview = new JLabel("No hay imagen seleccionada", SwingConstants.CENTER);
        lblImagenPreview.setPreferredSize(new Dimension(150, 100));
        lblImagenPreview.setBorder(BorderFactory.createLineBorder(BORDER_GRAY)); // Lighter border
        lblImagenPreview.setBackground(Color.WHITE); // White background for preview
        lblImagenPreview.setOpaque(true);
        lblImagenPreview.setFont(new Font("SansSerif", Font.ITALIC, 10));
        lblImagenPreview.setForeground(SECONDARY_DARK); // Darker gray for info text

        imageUploadPanel.add(btnCargarImagen, BorderLayout.WEST);
        imageUploadPanel.add(lblImagenPreview, BorderLayout.CENTER);
        formPanel.add(imageUploadPanel, gbc);

        btnCargarImagen.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    imagenBytes = Files.readAllBytes(selectedFile.toPath());
                    ImageIcon originalIcon = new ImageIcon(imagenBytes);
                    // Scale image to fit the preview label while maintaining aspect ratio
                    Image scaledImage = originalIcon.getImage().getScaledInstance(
                            lblImagenPreview.getWidth(), lblImagenPreview.getHeight(), Image.SCALE_SMOOTH);
                    lblImagenPreview.setIcon(new ImageIcon(scaledImage));
                    lblImagenPreview.setText(""); // Clear text when image is loaded
                } catch (IOException ex) {
                    lblImagenPreview.setText("Error al cargar");
                    lblImagenPreview.setIcon(null);
                    imagenBytes = null; // Clear image bytes on error
                    JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20)); // Centered button with vertical padding
        buttonPanel.setBackground(BACKGROUND_LIGHT);
        JButton btnGuardar = new JButton("Guardar Producto");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnGuardar.setBackground(ACCENT_ORANGE); // Primary orange for save button
        btnGuardar.setForeground(Color.WHITE); // White text for save button
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new Dimension(200, 45));
        buttonPanel.add(btnGuardar);

        btnGuardar.addActionListener(e -> {
            guardarProducto(); // Calls the method to save the product directly
        });

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void guardarProducto() {
        // --- Step 1: Check if a user is logged in ---
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para subir productos.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the ID of the logged-in user
        int idUsuarioSubida = UserSession.getCurrentUserId();

        String nombre = txtNombre.getText().trim(); // Use trim to clean spaces
        String descripcion = txtDescripcion.getText().trim();
        String categoria = txtCategoria.getText().trim();

        // Validate that required fields are not empty
        if (nombre.isEmpty() || descripcion.isEmpty() || txtPrecio.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty() || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal precio;
        int stock;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
            stock = Integer.parseInt(txtStock.getText().trim());
            // Validate positive price and non-negative stock
            if (precio.compareTo(BigDecimal.ZERO) <= 0 || stock < 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser mayor que cero y el stock no puede ser negativo.", "Datos Inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio y Stock deben ser números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Logic to save to the database ---
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            // Get a new connection using your 'conexion' class
            connection = new conexion().getConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // SQL: Include id_usuario_subida in the insertion
            // Make sure the column 'id_usuario_subida' exists in your 'Productos' table
            String sql = "INSERT INTO Productos (nombre, descripcion, precio, stock, categoria, imagen, id_usuario_subida) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setBigDecimal(3, precio);
            ps.setInt(4, stock);
            ps.setString(5, categoria);
            if (imagenBytes != null) {
                ps.setBytes(6, imagenBytes); // Handle the image as a byte array
            } else {
                ps.setNull(6, java.sql.Types.BLOB); // Save NULL if no image
            }
            ps.setInt(7, idUsuarioSubida); // Assign the ID of the logged-in user

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Producto guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos(); // Call a method to clear the fields
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error SQL al guardar el producto: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // ALWAYS CLOSE RESOURCES! (In reverse order of their opening)
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar PreparedStatement: " + e.getMessage());
            }
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar Connection: " + e.getMessage());
            }
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        txtCategoria.setText("");
        lblImagenPreview.setIcon(null);
        lblImagenPreview.setText("No hay imagen seleccionada");
        imagenBytes = null; // Reset image bytes
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(SECONDARY_DARK); // Dark gray text
        return label;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST; // Align label to the west
        panel.add(createLabel(labelText, new Font("SansSerif", Font.BOLD, 14)), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0; // The text field expands horizontally
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(BORDER_GRAY)); // Add border to text field
        panel.add(textField, gbc);
        gbc.weightx = 0; // Reset weightx for the next row
    }
}