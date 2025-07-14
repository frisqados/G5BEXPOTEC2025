package vista;

import controlador.conexion;
import util.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IngresoProductoPanel extends JPanel {

    private final Color PRIMARY_TEXT_COLOR = UIManager.getColor("Label.foreground");
    private final Color SECONDARY_TEXT_COLOR = UIManager.getColor("Label.disabledForeground");
    private final Color BORDER_COLOR = UIManager.getColor("Component.borderColor");
    private final Color BACKGROUND_COLOR = UIManager.getColor("Panel.background");
    private final Color CARD_BACKGROUND = UIManager.getColor("List.background");
    private final Color BUTTON_PRIMARY_BACKGROUND = new Color(70, 130, 180);
    private final Color BUTTON_FOREGROUND = Color.WHITE;
    private final Color DANGER_COLOR = new Color(220, 20, 60);

    private JTextField txtNombreProducto;
    private JTextArea txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JComboBox<String> cmbCategorias;
    private JLabel lblImagenProducto;
    private JButton btnSeleccionarImagen;
    private JButton btnGuardarProducto;

    private byte[] selectedImageData = null;

    public IngresoProductoPanel() {
        initComponents();
        loadCategories();
        clearFields();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 30));
        contentPanel.setOpaque(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Ingreso de Nuevo Producto");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titleLabel.setForeground(PRIMARY_TEXT_COLOR);
        headerPanel.add(titleLabel);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        forms_proyect_expotec.PanelRound formPanel = new forms_proyect_expotec.PanelRound();
        formPanel.setBackground(CARD_BACKGROUND);
        formPanel.setRoundTopLeft(15);
        formPanel.setRoundTopRight(15);
        formPanel.setRoundBottomLeft(15);
        formPanel.setRoundBottomRight(15);
        formPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblNombreProducto = createStyledLabel("Nombre del Producto:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(lblNombreProducto, gbc);

        txtNombreProducto = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(txtNombreProducto, gbc);

        JLabel lblDescripcion = createStyledLabel("Descripción:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(lblDescripcion, gbc);

        txtDescripcion = new JTextArea(5, 20);
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtDescripcion.setBackground(UIManager.getColor("TextField.background"));
        txtDescripcion.setForeground(PRIMARY_TEXT_COLOR);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 30;
        formPanel.add(scrollDescripcion, gbc);
        gbc.ipady = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPrecio = createStyledLabel("Precio:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(lblPrecio, gbc);

        txtPrecio = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        formPanel.add(txtPrecio, gbc);

        JLabel lblStock = createStyledLabel("Stock:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(lblStock, gbc);

        txtStock = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        formPanel.add(txtStock, gbc);

        JLabel lblCategoria = createStyledLabel("Categoría:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(lblCategoria, gbc);

        cmbCategorias = new JComboBox<>();
        cmbCategorias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCategorias.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        formPanel.add(cmbCategorias, gbc);

        JLabel lblImagen = createStyledLabel("Imagen del Producto:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        formPanel.add(lblImagen, gbc);

        lblImagenProducto = new JLabel("Haz clic para seleccionar una imagen", SwingConstants.CENTER);
        lblImagenProducto.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagenProducto.setVerticalAlignment(SwingConstants.CENTER);
        lblImagenProducto.setPreferredSize(new Dimension(400, 400));
        lblImagenProducto.setBorder(BorderFactory.createDashedBorder(BORDER_COLOR, 2, 2));
        lblImagenProducto.setBackground(UIManager.getColor("Panel.background"));
        lblImagenProducto.setOpaque(true);
        lblImagenProducto.setForeground(SECONDARY_TEXT_COLOR);
        lblImagenProducto.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblImagenProducto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblImagenProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSeleccionarImagen.doClick();
            }
        });
        
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.insets = new Insets(10, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(lblImagenProducto, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        btnSeleccionarImagen = createStyledButton("Seleccionar Imagen", BUTTON_PRIMARY_BACKGROUND, BUTTON_FOREGROUND);
        btnSeleccionarImagen.addActionListener(e -> selectImage());
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0; gbc.insets = new Insets(5, 5, 10, 5);
        formPanel.add(btnSeleccionarImagen, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        btnGuardarProducto = createStyledButton("Guardar Nuevo Producto", BUTTON_PRIMARY_BACKGROUND, BUTTON_FOREGROUND);
        btnGuardarProducto.addActionListener(e -> saveNewProduct());
        buttonPanel.add(btnGuardarProducto);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        JScrollPane mainScrollPane = new JScrollPane(formPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        contentPanel.add(mainScrollPane, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createStyledLabel(String text, Color foreground, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(foreground);
        label.setFont(font);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        textField.setBackground(UIManager.getColor("TextField.background"));
        textField.setForeground(PRIMARY_TEXT_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setCaretColor(PRIMARY_TEXT_COLOR);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 35));
        return textField;
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    private void loadCategories() {
        cmbCategorias.removeAllItems();
        Connection con = null;
        try {
            con = new conexion().getConnection();
            String sql = "SELECT DISTINCT categoria FROM productos WHERE categoria IS NOT NULL AND categoria <> '' ORDER BY categoria";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cmbCategorias.addItem(rs.getString("categoria"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías disponibles: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        // --- INICIO: Nuevas categorías agregadas ---
        // Puedes agregar categorías aquí que no estén en la base de datos o que quieras asegurar que siempre aparezcan.
        // Se añadirán al final de las categorías ya cargadas de la DB.
        if (cmbCategorias.getItemCount() == 0) { // Si no hay categorías de la DB, añade algunas por defecto
            cmbCategorias.addItem("Electrónica");
            cmbCategorias.addItem("Ropa y Accesorios");
            cmbCategorias.addItem("Hogar y Cocina");
            cmbCategorias.addItem("Libros");
            cmbCategorias.addItem("Deportes");
            cmbCategorias.addItem("Juguetes y Juegos");
            cmbCategorias.addItem("Belleza y Cuidado Personal");
            cmbCategorias.addItem("Salud");
            cmbCategorias.addItem("Automotriz");
            cmbCategorias.addItem("Herramientas y Mejoras para el Hogar");
            cmbCategorias.addItem("Alimentos y Bebidas");
            cmbCategorias.addItem("Mascotas");
            cmbCategorias.addItem("Arte y Manualidades");
            cmbCategorias.addItem("Oficina y Papelería");
            cmbCategorias.addItem("Jardín y Exterior");
            cmbCategorias.addItem("Música, Películas y TV");
            cmbCategorias.addItem("Videojuegos y Consolas");
            cmbCategorias.addItem("Software");
            cmbCategorias.addItem("Viajes");
            cmbCategorias.addItem("Servicios");
            cmbCategorias.addItem("Otros");
        } else { // Si ya hay categorías de la DB, puedes añadir estas si no existen
             String[] defaultCategories = {
                "Electrónica", "Ropa y Accesorios", "Hogar y Cocina", "Libros", "Deportes",
                "Juguetes y Juegos", "Belleza y Cuidado Personal", "Salud", "Automotriz",
                "Herramientas y Mejoras para el Hogar", "Alimentos y Bebidas", "Mascotas",
                "Arte y Manualidades", "Oficina y Papelería", "Jardín y Exterior",
                "Música, Películas y TV", "Videojuegos y Consolas", "Software", "Viajes",
                "Servicios", "Otros"
            };
            for (String cat : defaultCategories) {
                boolean found = false;
                for (int i = 0; i < cmbCategorias.getItemCount(); i++) {
                    if (cmbCategorias.getItemAt(i).equals(cat)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    cmbCategorias.addItem(cat);
                }
            }
        }
        // --- FIN: Nuevas categorías agregadas ---

        cmbCategorias.insertItemAt("Seleccionar o Escribir Nueva...", 0);
        cmbCategorias.setSelectedIndex(0);
    }

    private void clearFields() {
        txtNombreProducto.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        if (cmbCategorias.getItemCount() > 0) {
            cmbCategorias.setSelectedIndex(0);
        } else {
            cmbCategorias.addItem("Seleccionar o Escribir Nueva...");
            cmbCategorias.setSelectedIndex(0);
        }
        
        lblImagenProducto.setIcon(null);
        lblImagenProducto.setText("Haz clic para seleccionar una imagen");
        lblImagenProducto.setForeground(SECONDARY_TEXT_COLOR);
        lblImagenProducto.setPreferredSize(new Dimension(400, 400));
        selectedImageData = null;
        
        revalidate(); 
        repaint();
    }

    private void loadImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            lblImagenProducto.setIcon(null);
            lblImagenProducto.setText("Haz clic para seleccionar una imagen");
            lblImagenProducto.setForeground(SECONDARY_TEXT_COLOR);
            lblImagenProducto.setPreferredSize(new Dimension(400, 400));
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(imageData);
            Image image = icon.getImage();

            if (image.getWidth(null) == -1 || image.getHeight(null) == -1) {
                throw new IOException("Los datos de la imagen son inválidos o están corruptos.");
            }

            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);

            lblImagenProducto.setPreferredSize(new Dimension(imageWidth, imageHeight));
            
            lblImagenProducto.setIcon(icon); 
            lblImagenProducto.setText("");
            lblImagenProducto.setForeground(PRIMARY_TEXT_COLOR);

            revalidate();
            repaint();

        } catch (Exception e) {
            lblImagenProducto.setIcon(null);
            lblImagenProducto.setText("Error al cargar imagen. Los datos podrían estar corruptos.");
            lblImagenProducto.setForeground(DANGER_COLOR);
            lblImagenProducto.setPreferredSize(new Dimension(400, 400));
            System.err.println("Error al cargar imagen desde bytes: " + e.getMessage());
            e.printStackTrace();
            revalidate(); 
            repaint();
        }
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen para el Producto");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Imagen", "jpg", "jpeg", "png", "gif"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                try {
                    selectedImageData = Files.readAllBytes(selectedFile.toPath());
                    loadImage(selectedImageData);
                    lblImagenProducto.setForeground(PRIMARY_TEXT_COLOR);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "No se pudo leer la imagen seleccionada: " + e.getMessage(), "Error de Archivo", JOptionPane.ERROR_MESSAGE);
                    selectedImageData = null;
                    lblImagenProducto.setIcon(null);
                    lblImagenProducto.setText("Error al leer la imagen seleccionada.");
                    lblImagenProducto.setForeground(DANGER_COLOR);
                    lblImagenProducto.setPreferredSize(new Dimension(400, 400));
                    e.printStackTrace();
                    revalidate();
                    repaint();
                }
            }
        }
    }

    private void saveNewProduct() {
        String nombre = txtNombreProducto.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String stockStr = txtStock.getText().trim();
        String categoria = (String) cmbCategorias.getSelectedItem();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || categoria == null || categoria.isEmpty() || categoria.equals("Seleccionar o Escribir Nueva...")) {
            JOptionPane.showMessageDialog(this, "Todos los campos (excepto la imagen) son obligatorios. Por favor, rellénelos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double precio;
        int stock;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio <= 0) {
                JOptionPane.showMessageDialog(this, "El precio debe ser un número positivo mayor que cero.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio no es válido. Por favor, ingrese un número (ej. 19.99).", "Error de Formato", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser un número negativo.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El stock no es válido. Por favor, ingrese un número entero.", "Error de Formato", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = UserSession.getCurrentUserId();
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para poder subir productos.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            con.setAutoCommit(false);

            String sqlInsert = "INSERT INTO productos (nombre, descripcion, precio, stock, categoria, imagen, id_usuario_subida) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setString(1, nombre);
                ps.setString(2, descripcion);
                ps.setDouble(3, precio);
                ps.setInt(4, stock);
                ps.setString(5, categoria);
                if (selectedImageData != null && selectedImageData.length > 0) {
                    ps.setBytes(6, selectedImageData);
                } else {
                    ps.setNull(6, java.sql.Types.VARBINARY);
                }
                ps.setInt(7, userId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    con.commit();
                    JOptionPane.showMessageDialog(this, "¡Producto ingresado con éxito! Ya está disponible.", "Ingreso Exitoso", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                } else {
                    con.rollback();
                    JOptionPane.showMessageDialog(this, "No se pudo ingresar el producto. Inténtelo de nuevo.", "Error de Ingreso", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Ocurrió un error en la base de datos al intentar guardar el producto: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void refreshPanel() {
        clearFields();
        loadCategories();
    }
}