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

public class EditarProductoPanel extends JPanel {

    // Definición de colores para un estilo consistente y vistoso
    private final Color PRIMARY_TEXT_COLOR = UIManager.getColor("Label.foreground");
    private final Color SECONDARY_TEXT_COLOR = UIManager.getColor("Label.disabledForeground");
    private final Color BORDER_COLOR = UIManager.getColor("Component.borderColor");
    private final Color BACKGROUND_COLOR = UIManager.getColor("Panel.background");
    private final Color CARD_BACKGROUND = UIManager.getColor("List.background");
    private final Color BUTTON_PRIMARY_BACKGROUND = new Color(70, 130, 180); // Azul acero, más vibrante
    private final Color BUTTON_FOREGROUND = Color.WHITE;
    private final Color DANGER_COLOR = new Color(220, 20, 60); // Rojo más brillante

    private JComboBox<String> cmbProductos;
    private JTextField txtNombreProducto;
    private JTextArea txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JComboBox<String> cmbCategorias;
    private JLabel lblImagenProducto;
    private JButton btnSeleccionarImagen;
    private JButton btnActualizarProducto;
    private JButton btnEliminarProducto;

    private int selectedProductId = -1;
    private byte[] currentImageData = null; // Datos binarios de la imagen actual del producto
    private byte[] newImageData = null;     // Datos binarios de la nueva imagen seleccionada


    public EditarProductoPanel() {
        initComponents();
        loadProductsForUser();
        loadCategories();
        setFieldsEnabled(false);
    }

    private void initComponents() {
        // --- CAMBIO CLAVE: Layout de este panel para permitir el JScrollPane ---
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 30));
        contentPanel.setOpaque(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Gestión de Productos");
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
        formPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // -- Componente: Selector de Producto --
        JLabel lblSeleccionarProducto = createStyledLabel("Seleccionar Producto:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(lblSeleccionarProducto, gbc);

        cmbProductos = new JComboBox<>();
        cmbProductos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbProductos.addActionListener(e -> displaySelectedProductData());
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(cmbProductos, gbc);

        // -- Componente: Nombre del Producto --
        JLabel lblNombreProducto = createStyledLabel("Nombre del Producto:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(lblNombreProducto, gbc);

        txtNombreProducto = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtNombreProducto, gbc);

        // -- Componente: Descripción --
        JLabel lblDescripcion = createStyledLabel("Descripción:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
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
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 30;
        formPanel.add(scrollDescripcion, gbc);
        gbc.ipady = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -- Componente: Precio --
        JLabel lblPrecio = createStyledLabel("Precio:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(lblPrecio, gbc);

        txtPrecio = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        formPanel.add(txtPrecio, gbc);

        // -- Componente: Stock --
        JLabel lblStock = createStyledLabel("Stock:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(lblStock, gbc);

        txtStock = createStyledTextField();
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        formPanel.add(txtStock, gbc);

        // -- Componente: Categoría --
        JLabel lblCategoria = createStyledLabel("Categoría:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        formPanel.add(lblCategoria, gbc);

        cmbCategorias = new JComboBox<>();
        cmbCategorias.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        formPanel.add(cmbCategorias, gbc);

        // -- Componente: Imagen del Producto (más grande, sin scroll propio) --
        JLabel lblImagen = createStyledLabel("Imagen del Producto:", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        formPanel.add(lblImagen, gbc);

        lblImagenProducto = new JLabel("Haz clic para seleccionar una imagen", SwingConstants.CENTER);
        // --- CAMBIO CLAVE: Tamaño inicial grande para la imagen ---
        lblImagenProducto.setPreferredSize(new Dimension(400, 400)); // Un tamaño generoso por defecto
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
        
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0; gbc.insets = new Insets(10, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH; // Permitir que el JLabel de imagen se expanda si hay espacio
        formPanel.add(lblImagenProducto, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Resetear fill para el resto

        btnSeleccionarImagen = createStyledButton("Seleccionar Nueva Imagen", BUTTON_PRIMARY_BACKGROUND, BUTTON_FOREGROUND);
        btnSeleccionarImagen.addActionListener(e -> selectImage());
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 1.0; gbc.insets = new Insets(5, 5, 10, 5);
        formPanel.add(btnSeleccionarImagen, gbc);

        // -- Panel de Botones de Acción --
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        btnActualizarProducto = createStyledButton("Actualizar Producto", BUTTON_PRIMARY_BACKGROUND, BUTTON_FOREGROUND);
        btnActualizarProducto.addActionListener(e -> updateProduct());
        buttonPanel.add(btnActualizarProducto);

        btnEliminarProducto = createStyledButton("Eliminar Producto", DANGER_COLOR, Color.WHITE);
        btnEliminarProducto.addActionListener(e -> deleteProduct());
        buttonPanel.add(btnEliminarProducto);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(buttonPanel, gbc);

        // --- CAMBIO CLAVE: Envolver el formPanel en un JScrollPane ---
        JScrollPane mainScrollPane = new JScrollPane(formPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Eliminar el borde predeterminado del JScrollPane para que se vea el borde del PanelRound
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        // Añadir el JScrollPane al contentPanel (o directamente a este panel si no usas contentPanel)
        contentPanel.add(mainScrollPane, BorderLayout.CENTER);
        
        // Añadir el contentPanel (que ahora contiene el JScrollPane) al panel principal
        add(contentPanel, BorderLayout.CENTER);
    }

    // Métodos utilitarios para crear componentes con estilo consistente
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

    // Método para habilitar/deshabilitar campos del formulario
    private void setFieldsEnabled(boolean enabled) {
        txtNombreProducto.setEnabled(enabled);
        txtDescripcion.setEnabled(enabled);
        txtPrecio.setEnabled(enabled);
        txtStock.setEnabled(enabled);
        cmbCategorias.setEnabled(enabled);
        btnSeleccionarImagen.setEnabled(enabled);
        btnActualizarProducto.setEnabled(enabled);
        btnEliminarProducto.setEnabled(enabled);
    }

    // --- LÓGICA DE CARGA Y ACTUALIZACIÓN DE DATOS ---

    // Carga los productos del usuario actual en el JComboBox
    private void loadProductsForUser() {
        int userId = UserSession.getCurrentUserId();
        if (userId == -1) {
            cmbProductos.removeAllItems();
            cmbProductos.addItem("Debe iniciar sesión para ver productos.");
            cmbProductos.setEnabled(false);
            setFieldsEnabled(false);
            return;
        }

        cmbProductos.removeAllItems();
        cmbProductos.addItem("Seleccione un producto para editar...");
        cmbProductos.setEnabled(true);

        Connection con = null;
        boolean hasProducts = false;
        try {
            con = new conexion().getConnection();
            String sql = "SELECT id_producto, nombre FROM productos WHERE id_usuario_subida = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cmbProductos.addItem(rs.getInt("id_producto") + " - " + rs.getString("nombre"));
                        hasProducts = true;
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar sus productos: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        if (!hasProducts) {
            cmbProductos.addItem("No tiene productos subidos. ¡Suba uno!");
            cmbProductos.setEnabled(false);
            setFieldsEnabled(false);
        }
    }

    // Carga las categorías existentes en el JComboBox
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
    }

    // Muestra los datos del producto seleccionado en los campos del formulario
    private void displaySelectedProductData() {
        String selectedItem = (String) cmbProductos.getSelectedItem();
        if (selectedItem == null || selectedItem.startsWith("Seleccione un producto") || selectedItem.startsWith("No tiene productos")) {
            clearFields();
            setFieldsEnabled(false);
            selectedProductId = -1;
            return;
        }

        try {
            selectedProductId = Integer.parseInt(selectedItem.split(" - ")[0]);
        } catch (NumberFormatException e) {
            selectedProductId = -1;
            clearFields();
            setFieldsEnabled(false);
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            String sql = "SELECT p.nombre, p.descripcion, p.precio, p.stock, p.imagen, p.categoria " +
                         "FROM productos p " +
                         "WHERE p.id_producto = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, selectedProductId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        txtNombreProducto.setText(rs.getString("nombre"));
                        txtDescripcion.setText(rs.getString("descripcion"));
                        txtPrecio.setText(String.format("%.2f", rs.getDouble("precio")));
                        txtStock.setText(String.valueOf(rs.getInt("stock")));
                        cmbCategorias.setSelectedItem(rs.getString("categoria"));

                        currentImageData = rs.getBytes("imagen");
                        newImageData = null;
                        loadImage(currentImageData);

                        setFieldsEnabled(true);
                    } else {
                        clearFields();
                        setFieldsEnabled(false);
                        selectedProductId = -1;
                        JOptionPane.showMessageDialog(this, "El producto seleccionado no fue encontrado.", "Producto no encontrado", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar los detalles del producto: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            clearFields();
            setFieldsEnabled(false);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Limpia todos los campos del formulario
    private void clearFields() {
        txtNombreProducto.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cmbCategorias.setSelectedIndex(-1);
        lblImagenProducto.setIcon(null);
        lblImagenProducto.setText("Haz clic para seleccionar una imagen");
        lblImagenProducto.setForeground(SECONDARY_TEXT_COLOR);
        // --- CAMBIO CLAVE: Volver al tamaño por defecto si no hay imagen ---
        lblImagenProducto.setPreferredSize(new Dimension(400, 400)); 
        currentImageData = null;
        newImageData = null;
        // Revalidar y repintar el padre para asegurar que los cambios de tamaño se reflejen
        revalidate(); 
        repaint();
    }

    // Carga la imagen en el JLabel. Ajusta el tamaño del JLabel a la imagen original.
    private void loadImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            lblImagenProducto.setIcon(null);
            lblImagenProducto.setText("No hay imagen para mostrar.");
            lblImagenProducto.setForeground(SECONDARY_TEXT_COLOR);
            lblImagenProducto.setPreferredSize(new Dimension(400, 400)); // Tamaño por defecto si no hay imagen
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

            // --- CAMBIO CLAVE: Establecer el tamaño preferido del JLabel al tamaño original de la imagen ---
            lblImagenProducto.setPreferredSize(new Dimension(imageWidth, imageHeight));
            
            // Establecer el icono original sin escalar. El JScrollPane padre se encargará del scroll si es necesario.
            lblImagenProducto.setIcon(icon); 
            lblImagenProducto.setText("");
            lblImagenProducto.setForeground(PRIMARY_TEXT_COLOR);

            // Revalidar y repintar el panel completo para que el JScrollPane padre se ajuste
            revalidate();
            repaint();

        } catch (Exception e) {
            lblImagenProducto.setIcon(null);
            lblImagenProducto.setText("Error al cargar imagen. Los datos podrían estar corruptos.");
            lblImagenProducto.setForeground(DANGER_COLOR);
            lblImagenProducto.setPreferredSize(new Dimension(400, 400)); // Tamaño por defecto en caso de error
            System.err.println("Error al cargar imagen desde bytes: " + e.getMessage());
            e.printStackTrace();
            // Revalidar y repintar en caso de error
            revalidate(); 
            repaint();
        }
    }

    // Permite al usuario seleccionar una nueva imagen
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Nueva Imagen para el Producto");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de Imagen", "jpg", "jpeg", "png", "gif"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                try {
                    newImageData = Files.readAllBytes(selectedFile.toPath());
                    loadImage(newImageData); // Mostrar la nueva imagen seleccionada
                    lblImagenProducto.setForeground(PRIMARY_TEXT_COLOR);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "No se pudo leer la imagen seleccionada: " + e.getMessage(), "Error de Archivo", JOptionPane.ERROR_MESSAGE);
                    newImageData = null;
                    lblImagenProducto.setIcon(null);
                    lblImagenProducto.setText("Error al leer la imagen seleccionada.");
                    lblImagenProducto.setForeground(DANGER_COLOR);
                    lblImagenProducto.setPreferredSize(new Dimension(400, 400)); // Restablecer tamaño por defecto
                    e.printStackTrace();
                    revalidate();
                    repaint();
                }
            }
        }
    }

    // Actualiza el producto en la base de datos
    private void updateProduct() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la lista para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = txtNombreProducto.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String stockStr = txtStock.getText().trim();
        String categoria = (String) cmbCategorias.getSelectedItem();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || categoria == null || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos de producto son obligatorios. Por favor, rellénelos.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
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

        Connection con = null;
        try {
            con = new conexion().getConnection();
            con.setAutoCommit(false); // Iniciar transacción

            byte[] imageDataToSave = (newImageData != null) ? newImageData : currentImageData;

            String sqlUpdate = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, stock = ?, categoria = ?, imagen = ? WHERE id_producto = ?";
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                psUpdate.setString(1, nombre);
                psUpdate.setString(2, descripcion);
                psUpdate.setDouble(3, precio);
                psUpdate.setInt(4, stock);
                psUpdate.setString(5, categoria);
                if (imageDataToSave != null && imageDataToSave.length > 0) {
                    psUpdate.setBytes(6, imageDataToSave);
                } else {
                    psUpdate.setNull(6, java.sql.Types.VARBINARY);
                }
                psUpdate.setInt(7, selectedProductId);

                int rowsAffected = psUpdate.executeUpdate();
                if (rowsAffected > 0) {
                    con.commit();
                    JOptionPane.showMessageDialog(this, "¡Producto actualizado con éxito! Los cambios ya están guardados.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
                    loadProductsForUser();
                    clearFields();
                    setFieldsEnabled(false);
                } else {
                    con.rollback();
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el producto. El ID podría no ser válido o no tiene permisos.", "Error de Actualización", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Ocurrió un error en la base de datos al intentar actualizar el producto: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteProduct() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la lista para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está completamente seguro de que desea eliminar el producto \"" + txtNombreProducto.getText() + "\"?\nEsta acción es irreversible.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            try {
                con = new conexion().getConnection();
                con.setAutoCommit(false);

                String sqlDelete = "DELETE FROM productos WHERE id_producto = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                    ps.setInt(1, selectedProductId);
                    int rowsAffected = ps.executeUpdate();

                    if (rowsAffected > 0) {
                        con.commit();
                        JOptionPane.showMessageDialog(this, "¡Producto eliminado con éxito! Ya no aparecerá en sus listados.", "Eliminación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                        loadProductsForUser();
                        clearFields();
                        setFieldsEnabled(false);
                    } else {
                        con.rollback();
                        JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto. El ID podría no existir o no tiene permisos.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                try {
                    if (con != null) con.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Ocurrió un error al intentar eliminar el producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                try {
                    if (con != null) con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void refreshData() {
        loadProductsForUser();
        loadCategories();
        clearFields();
        setFieldsEnabled(false);
    }
}