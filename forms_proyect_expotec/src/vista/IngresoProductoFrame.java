package vista;

import modelo.Producto;
import controlador.conexion; // Importa tu clase de conexión
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection; // Para Connection
import java.sql.PreparedStatement; // Para PreparedStatement
import java.sql.SQLException; // Para SQLException

public class IngresoProductoFrame extends JFrame {
    private JTextField txtNombre, txtPrecio, txtStock, txtCategoria;
    private JTextArea txtDescripcion;
    private JLabel lblImagenPreview;
    private byte[] imagenBytes;

    public IngresoProductoFrame() {
        setTitle("Ingresar Nuevo Producto - Panel Administrativo");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(30, 30, 30));
        JLabel titleLabel = new JLabel("Ingreso de Productos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(formPanel, gbc, "Nombre del Producto:", txtNombre = new JTextField(30), 0);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Descripción:", new Font("SansSerif", Font.BOLD, 14)), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDescripcion = new JTextArea(5, 30);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtDescripcion);
        scrollPane.setPreferredSize(new Dimension(300, 80));
        formPanel.add(scrollPane, gbc);
        gbc.weightx = 0;

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
        btnCargarImagen.setBackground(new Color(255, 164, 28));
        btnCargarImagen.setForeground(Color.BLACK);
        btnCargarImagen.setFocusPainted(false);
        btnCargarImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblImagenPreview = new JLabel("No hay imagen seleccionada", SwingConstants.CENTER);
        lblImagenPreview.setPreferredSize(new Dimension(150, 100));
        lblImagenPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblImagenPreview.setBackground(Color.WHITE);
        lblImagenPreview.setOpaque(true);
        lblImagenPreview.setFont(new Font("SansSerif", Font.ITALIC, 10));
        
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
                    Image scaledImage = originalIcon.getImage().getScaledInstance(
                        lblImagenPreview.getWidth(), lblImagenPreview.getHeight(), Image.SCALE_SMOOTH);
                    lblImagenPreview.setIcon(new ImageIcon(scaledImage));
                    lblImagenPreview.setText("");
                } catch (IOException ex) {
                    lblImagenPreview.setText("Error al cargar");
                    lblImagenPreview.setIcon(null);
                    imagenBytes = null;
                    JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        buttonPanel.setBackground(formPanel.getBackground());
        JButton btnGuardar = new JButton("Guardar Producto");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnGuardar.setBackground(new Color(255, 153, 0));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.setPreferredSize(new Dimension(200, 45));
        buttonPanel.add(btnGuardar);

        btnGuardar.addActionListener(e -> {
            guardarProducto(); // Llama al método para guardar el producto directamente
        });
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void guardarProducto() {
        String nombre = txtNombre.getText();
        String descripcion = txtDescripcion.getText();
        String categoria = txtCategoria.getText();
        
        if (nombre.isEmpty() || descripcion.isEmpty() || txtPrecio.getText().isEmpty() || txtStock.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal precio;
        int stock;
        try {
            precio = new BigDecimal(txtPrecio.getText());
            stock = Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio y Stock deben ser números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Lógica de guardado directamente en la clase de la vista ---
        conexion con = new conexion(); // Instancia de tu clase de conexión
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = con.getConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO Productos (nombre, descripcion, precio, stock, categoria, imagen) VALUES (?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setBigDecimal(3, precio);
            ps.setInt(4, stock);
            ps.setString(5, categoria);
            ps.setBytes(6, imagenBytes); // Manejo de imagen como byte array

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Producto guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                // Limpiar campos después de guardar
                txtNombre.setText("");
                txtDescripcion.setText("");
                txtPrecio.setText("");
                txtStock.setText("");
                txtCategoria.setText("");
                lblImagenPreview.setIcon(null);
                lblImagenPreview.setText("No hay imagen seleccionada");
                imagenBytes = null;
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error SQL al guardar el producto: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (ps != null) ps.close();
                if (connection != null) connection.close(); // Cerrar conexión
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
        // --- Fin de la lógica de guardado directo ---
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(createLabel(labelText, new Font("SansSerif", Font.BOLD, 14)), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(textField, gbc);
        gbc.weightx = 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(IngresoProductoFrame::new);
    }
}