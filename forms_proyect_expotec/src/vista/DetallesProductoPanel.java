package vista;

import modelo.ItemCarrito;
import modelo.Producto;
import controlador.conexion;
import util.UserSession;
import util.ProductoSeleccionadoListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import util.FacturadorEmail;
import forms_proyect_expotec.LoginForm; // Asegúrate de que esta ruta sea correcta
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetallesProductoPanel extends JPanel {

    private Producto producto;
    private JSpinner cantidadSpinner;
    private JLabel lblSubtotal, lblEnvio, lblTotal;
    private JLabel stockLabel;
    private JPanel contentPanel; // Panel que contendrá toda la interfaz del producto
    private JButton buyNowButton;
    private JButton addToCartButton;
    private JButton addToWishlistButton;

    private JTextField direccionEnvioField;
    private JTextField correoElectronicoField;

    private CarritoPanel carritoPanelInstance; // Puede ser null si no se usa el carrito
    private ProductoSeleccionadoListener navegadorPrincipal; // Puede ser null

    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.00");

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s,\\.#/\\\\-]{5,100}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public DetallesProductoPanel(Producto producto, CarritoPanel carritoPanelInstance, ProductoSeleccionadoListener navegadorPrincipal) {
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.producto = producto;
        this.carritoPanelInstance = carritoPanelInstance;
        this.navegadorPrincipal = navegadorPrincipal;

        // **Panel principal que contendrá todos los subpaneles**
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(25, 25)); // Espacio entre NORTH y CENTER
        contentPanel.setBackground(new Color(240, 240, 240));
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- PANEL DE INFORMACIÓN DEL PRODUCTO (Imagen, detalles, botones) ---
        JPanel detallesProductoInfoPanel = new JPanel(new GridBagLayout());
        detallesProductoInfoPanel.setBackground(Color.WHITE);
        detallesProductoInfoPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(300, 300));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(producto.getImagen()));
                if (originalImage != null) {
                    Image scaledImage = originalImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(scaledImage));
                } else {
                    lblImagen.setText("Error de formato de imagen");
                    lblImagen.setFont(new Font("SansSerif", Font.ITALIC, 10));
                    lblImagen.setForeground(Color.RED);
                }
            } catch (IOException e) {
                lblImagen.setText("Error al cargar imagen");
                lblImagen.setFont(new Font("SansSerif", Font.ITALIC, 10));
                lblImagen.setForeground(Color.RED);
                e.printStackTrace();
            }
        } else {
            lblImagen.setText("Sin imagen");
            lblImagen.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lblImagen.setForeground(Color.GRAY);
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        detallesProductoInfoPanel.add(lblImagen, gbc);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints infoGbc = new GridBagConstraints();
        infoGbc.insets = new Insets(5, 5, 5, 5);
        infoGbc.fill = GridBagConstraints.HORIZONTAL;
        infoGbc.anchor = GridBagConstraints.WEST;

        int infoRow = 0;

        JLabel lblNombre = new JLabel("<html><b style='font-size:24px;'>" + producto.getNombre() + "</b></html>");
        infoGbc.gridx = 0; infoGbc.gridy = infoRow++; infoGbc.gridwidth = 2; infoGbc.weightx = 1.0;
        infoPanel.add(lblNombre, infoGbc);

        JLabel lblCategoria = new JLabel("Categoría: " + producto.getCategoria());
        lblCategoria.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblCategoria.setForeground(Color.GRAY);
        infoGbc.gridy = infoRow++;
        infoPanel.add(lblCategoria, infoGbc);

        JLabel lblDescripcion = new JLabel("<html><p style='font-size:12px;'>" + producto.getDescripcion() + "</p></html>");
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoGbc.gridy = infoRow++;
        infoGbc.insets = new Insets(15, 5, 15, 5);
        infoPanel.add(lblDescripcion, infoGbc);
        infoGbc.insets = new Insets(5, 5, 5, 5);

        JLabel lblPrecio = new JLabel("<html><b style='font-size:28px; color:#006400;'>$" + producto.getPrecio().setScale(2, RoundingMode.HALF_UP) + "</b></html>");
        infoGbc.gridy = infoRow++;
        infoPanel.add(lblPrecio, infoGbc);

        stockLabel = new JLabel("Stock disponible: " + producto.getStock());
        stockLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        stockLabel.setForeground(producto.getStock() > 0 ? new Color(0, 128, 0) : new Color(178, 34, 34));
        infoGbc.gridy = infoRow++;
        infoPanel.add(stockLabel, infoGbc);

        JLabel lblPublicador = new JLabel("Publicado por: " + (producto.getPublisherName() != null ? producto.getPublisherName() : "Desconocido"));
        lblPublicador.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblPublicador.setForeground(new Color(100, 100, 100));
        infoGbc.gridy = infoRow++;
        infoPanel.add(lblPublicador, infoGbc);

        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoGbc.gridx = 0; infoGbc.gridy = infoRow; infoGbc.gridwidth = 1;
        infoPanel.add(lblCantidad, infoGbc);

        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, producto.getStock() > 0 ? producto.getStock() : 1, 1);
        cantidadSpinner = new JSpinner(spinnerModel);
        cantidadSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cantidadSpinner.setPreferredSize(new Dimension(80, 30));
        cantidadSpinner.setEnabled(producto.getStock() > 0);
        infoGbc.gridx = 1; infoGbc.gridy = infoRow++;
        infoPanel.add(cantidadSpinner, infoGbc);
        infoGbc.gridwidth = 2;

        JLabel lblDireccionEnvio = new JLabel("Dirección de Envío:");
        lblDireccionEnvio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoGbc.gridx = 0; infoGbc.gridy = infoRow; infoGbc.gridwidth = 1;
        infoPanel.add(lblDireccionEnvio, infoGbc);

        direccionEnvioField = new JTextField();
        direccionEnvioField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoGbc.gridx = 1; infoGbc.gridy = infoRow++;
        infoPanel.add(direccionEnvioField, infoGbc);
        infoGbc.gridwidth = 2;

        JLabel lblCorreoElectronico = new JLabel("Correo Electrónico (Factura):");
        lblCorreoElectronico.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoGbc.gridx = 0; infoGbc.gridy = infoRow; infoGbc.gridwidth = 1;
        infoPanel.add(lblCorreoElectronico, infoGbc);

        correoElectronicoField = new JTextField(UserSession.isLoggedIn() ? UserSession.getCurrentUserEmail() : "");
        correoElectronicoField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        correoElectronicoField.setEditable(UserSession.isLoggedIn());
        infoGbc.gridx = 1; infoGbc.gridy = infoRow++;
        infoPanel.add(correoElectronicoField, infoGbc);
        infoGbc.gridwidth = 2;

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionButtonPanel.setBackground(Color.WHITE);

        addToCartButton = new JButton("Añadir al Carrito");
        addToCartButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addToCartButton.setBackground(new Color(0, 128, 0));
        addToCartButton.setForeground(Color.WHITE);
        addToCartButton.setFocusPainted(false);
        addToCartButton.setBorderPainted(false);
        addToCartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToCartButton.setEnabled(producto.getStock() > 0);
        addToCartButton.addActionListener(e -> {
            if (!UserSession.isLoggedIn()) {
                showLoginRequiredDialog();
            } else {
                agregarAlCarrito(producto, (int) cantidadSpinner.getValue());
            }
        });
        actionButtonPanel.add(addToCartButton);

        buyNowButton = new JButton("Comprar Ahora");
        buyNowButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        buyNowButton.setBackground(new Color(255, 140, 0));
        buyNowButton.setForeground(Color.WHITE);
        buyNowButton.setFocusPainted(false);
        buyNowButton.setBorderPainted(false);
        buyNowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyNowButton.setEnabled(producto.getStock() > 0);
        buyNowButton.addActionListener(e -> {
            if (!UserSession.isLoggedIn()) {
                showLoginRequiredDialog();
            } else {
                handleBuyNowAction();
            }
        });
        actionButtonPanel.add(buyNowButton);

        addToWishlistButton = new JButton("Añadir a Deseos");
        addToWishlistButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addToWishlistButton.setBackground(new Color(178, 34, 34));
        addToWishlistButton.setForeground(Color.WHITE);
        addToWishlistButton.setFocusPainted(false);
        addToWishlistButton.setBorderPainted(false);
        addToWishlistButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToWishlistButton.addActionListener(e -> {
            if (!UserSession.isLoggedIn()) {
                showLoginRequiredDialog();
            } else {
                addProductoToWishlist(producto.getId());
            }
        });
        actionButtonPanel.add(addToWishlistButton);

        JButton backButton = new JButton("Volver al Catálogo");
        backButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        backButton.setBackground(new Color(60, 179, 113));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> {
            if (navegadorPrincipal != null) {
                navegadorPrincipal.volverAlCatalogo();
            }
        });
        actionButtonPanel.add(backButton);

        infoGbc.gridx = 0; infoGbc.gridy = infoRow++; infoGbc.gridwidth = 2;
        infoGbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(actionButtonPanel, infoGbc);

        JPanel resumenPanel = new JPanel(new GridBagLayout());
        resumenPanel.setBackground(new Color(230, 230, 250));
        resumenPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(150, 150, 200)), "Resumen de Compra"));
        GridBagConstraints resGbc = new GridBagConstraints();
        resGbc.insets = new Insets(5, 10, 5, 10);
        resGbc.fill = GridBagConstraints.HORIZONTAL;
        resGbc.anchor = GridBagConstraints.WEST;
        resGbc.weightx = 1.0;

        JLabel lblResumen = new JLabel("Subtotal:");
        lblResumen.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resGbc.gridx = 0; resGbc.gridy = 0;
        resumenPanel.add(lblResumen, resGbc);
        lblSubtotal = new JLabel("$0.00");
        lblSubtotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        resGbc.gridx = 1; resGbc.gridy = 0;
        resumenPanel.add(lblSubtotal, resGbc);

        JLabel lblResumenEnvio = new JLabel("Envío:");
        lblResumenEnvio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resGbc.gridx = 0; resGbc.gridy = 1;
        resumenPanel.add(lblResumenEnvio, resGbc);
        lblEnvio = new JLabel("$" + SHIPPING_COST.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lblEnvio.setFont(new Font("SansSerif", Font.BOLD, 14));
        resGbc.gridx = 1; resGbc.gridy = 1;
        resumenPanel.add(lblEnvio, resGbc);

        resGbc.gridx = 0; resGbc.gridy = 2; resGbc.gridwidth = 2;
        resumenPanel.add(new JSeparator(), resGbc);

        JLabel lblResumenTotal = new JLabel("Total:");
        lblResumenTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        resGbc.gridx = 0; resGbc.gridy = 3; resGbc.gridwidth = 1;
        resumenPanel.add(lblResumenTotal, resGbc);
        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotal.setForeground(new Color(0, 100, 0));
        resGbc.gridx = 1; resGbc.gridy = 3;
        resumenPanel.add(lblTotal, resGbc);

        cantidadSpinner.addChangeListener(e -> actualizarResumenCompra());
        actualizarResumenCompra();

        infoGbc.gridx = 0; infoGbc.gridy = infoRow++; infoGbc.gridwidth = 2;
        infoGbc.fill = GridBagConstraints.BOTH;
        infoGbc.weighty = 1.0;
        infoGbc.insets = new Insets(20, 5, 5, 5);
        infoPanel.add(resumenPanel, infoGbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        detallesProductoInfoPanel.add(infoPanel, gbc);

        // Añade el panel de información del producto al NORTH del contentPanel
        contentPanel.add(detallesProductoInfoPanel, BorderLayout.NORTH);

        // --- PANEL DE RESEÑAS (Cargado desde la nueva clase ReseñasPanel) ---
        // Aquí es donde se integra el ReseñasPanel en el BorderLayout.CENTER
        ReseñasPanel reseñasPanel = new ReseñasPanel(producto);
        contentPanel.add(reseñasPanel, BorderLayout.CENTER);

        // --- SCROLL PRINCIPAL PARA TODO EL DETALLES DEL PRODUCTO ---
        // Este JScrollPane envuelve todo el contentPanel, permitiendo el scroll
        // de toda la página de detalles si el contenido es demasiado grande.
        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Velocidad del scroll

        // Establece el layout de este panel (DetallesProductoPanel) a BorderLayout
        // y añade el mainScrollPane para que ocupe todo su espacio.
        this.setLayout(new BorderLayout());
        this.add(mainScrollPane, BorderLayout.CENTER);

        // Nota: La carga inicial de reseñas se hace dentro de ReseñasPanel
    }

    public DetallesProductoPanel(Producto producto, CarritoPanel carritoPanelInstance) {
        this(producto, carritoPanelInstance, null);
    }

    private void actualizarResumenCompra() {
        int cantidad = (int) cantidadSpinner.getValue();
        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        
        BigDecimal total = subtotal.add(cantidad > 0 ? SHIPPING_COST : BigDecimal.ZERO);

        lblSubtotal.setText("$" + subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lblEnvio.setText("$" + SHIPPING_COST.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lblTotal.setText("$" + total.setScale(2, RoundingMode.HALF_UP).toPlainString());

        boolean hayStock = producto.getStock() > 0;
        boolean cantidadValida = cantidad > 0;
        buyNowButton.setEnabled(hayStock && cantidadValida);
        addToCartButton.setEnabled(hayStock && cantidadValida);
        cantidadSpinner.setEnabled(hayStock);
        
        stockLabel.setForeground(producto.getStock() > 0 ? new Color(0, 128, 0) : new Color(178, 34, 34));
    }

    private void showLoginRequiredDialog() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Debes iniciar sesión para realizar esta acción. ¿Deseas ir al login ahora?",
            "Inicio de Sesión Requerido",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        if (response == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                ((JFrame) window).dispose();
            }
            new LoginForm().setVisible(true);
        }
    }
    
    private void handleBuyNowAction() {
        String direccionEnvio = direccionEnvioField.getText().trim();
        String correoFactura = correoElectronicoField.getText().trim();

        if (!UserSession.isLoggedIn()) {
             showLoginRequiredDialog();
             return;
        }

        if (direccionEnvio.isEmpty() || !isValidAddress(direccionEnvio)) {
            String inputDireccion = JOptionPane.showInputDialog(this,
                "Por favor, ingresa tu dirección de envío (ej. 123 Calle Principal, Ciudad, País):",
                "Dirección de Envío Requerida",
                JOptionPane.QUESTION_MESSAGE);
            
            if (inputDireccion == null || inputDireccion.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "La dirección de envío es obligatoria para la compra.", "Entrada Requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            direccionEnvio = inputDireccion.trim();
            if (!isValidAddress(direccionEnvio)) {
                JOptionPane.showMessageDialog(this, "La dirección ingresada no es válida. Por favor, usa un formato más completo (ej. 123 Calle Ficticia, Ciudad, País).", "Dirección Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            direccionEnvioField.setText(direccionEnvio);
        }

        if (correoFactura.isEmpty() || !isValidEmail(correoFactura)) {
            String inputCorreo = JOptionPane.showInputDialog(this,
                "Por favor, ingresa tu correo electrónico para enviar la factura:",
                "Correo Electrónico Requerido",
                JOptionPane.QUESTION_MESSAGE);
            
            if (inputCorreo == null || inputCorreo.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El correo electrónico es obligatorio para enviar la factura.", "Entrada Requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }

            correoFactura = inputCorreo.trim();
            if (!isValidEmail(correoFactura)) {
                JOptionPane.showMessageDialog(this, "El correo electrónico ingresado no es válido. Por favor, verifica el formato.", "Correo Inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            correoElectronicoField.setText(correoFactura);
        }

        realizarCompraDirecta(producto, (int) cantidadSpinner.getValue(), direccionEnvio, correoFactura);
    }

    private void agregarAlCarrito(Producto prod, int cantidad) {
        if (!UserSession.isLoggedIn()) {
             showLoginRequiredDialog();
             return;
        }

        String direccionEnvio = direccionEnvioField.getText().trim();

        if (prod.getStock() < cantidad) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock para la cantidad seleccionada.", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero.", "Cantidad Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (direccionEnvio.isEmpty() || !isValidAddress(direccionEnvio)) {
            String inputDireccion = JOptionPane.showInputDialog(this,
                "Para añadir al carrito, por favor, ingresa tu dirección de envío:",
                "Dirección de Envío Requerida",
                JOptionPane.QUESTION_MESSAGE);
            
            if (inputDireccion == null || inputDireccion.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "La dirección de envío es obligatoria para añadir productos al carrito.", "Entrada Requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            direccionEnvio = inputDireccion.trim();
            if (!isValidAddress(direccionEnvio)) {
                JOptionPane.showMessageDialog(this, "La dirección ingresada no es válida. Por favor, usa un formato más completo (ej. 123 Calle Ficticia, Ciudad, País).", "Dirección Inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            direccionEnvioField.setText(direccionEnvio);
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            con.setAutoCommit(false);

            int idCarrito = -1;
            String getCarritoSql = "SELECT id_carrito FROM carritos WHERE id_usuario = ?";
            PreparedStatement getCarritoPs = con.prepareStatement(getCarritoSql);
            getCarritoPs.setInt(1, UserSession.getCurrentUserId());
            ResultSet rsCarrito = getCarritoPs.executeQuery();

            if (rsCarrito.next()) {
                idCarrito = rsCarrito.getInt("id_carrito");
            } else {
                String insertCarritoSql = "INSERT INTO carritos (id_usuario, fecha_agregado) VALUES (?, NOW())";
                PreparedStatement insertCarritoPs = con.prepareStatement(insertCarritoSql, Statement.RETURN_GENERATED_KEYS);
                insertCarritoPs.setInt(1, UserSession.getCurrentUserId());
                insertCarritoPs.executeUpdate();
                ResultSet generatedKeys = insertCarritoPs.getGeneratedKeys();
                if (generatedKeys.next()) {
                    idCarrito = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del carrito generado.");
                }
            }

            String checkItemSql = "SELECT id_item, cantidad FROM carrito_items WHERE id_carrito = ? AND id_producto = ?";
            PreparedStatement checkItemPs = con.prepareStatement(checkItemSql);
            checkItemPs.setInt(1, idCarrito);
            checkItemPs.setInt(2, prod.getId());
            ResultSet rsItem = checkItemPs.executeQuery();

            if (rsItem.next()) {
                int currentCantidad = rsItem.getInt("cantidad");
                String updateItemSql = "UPDATE carrito_items SET cantidad = ? WHERE id_item = ?";
                PreparedStatement updateItemPs = con.prepareStatement(updateItemSql);
                updateItemPs.setInt(1, currentCantidad + cantidad);
                updateItemPs.setInt(2, rsItem.getInt("id_item"));
                updateItemPs.executeUpdate();
            } else {
                String insertItemSql = "INSERT INTO carrito_items (id_carrito, id_producto, cantidad) VALUES (?, ?, ?)";
                PreparedStatement insertItemPs = con.prepareStatement(insertItemSql);
                insertItemPs.setInt(1, idCarrito);
                insertItemPs.setInt(2, prod.getId());
                insertItemPs.setInt(3, cantidad);
                insertItemPs.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(this, cantidad + " unidades de " + prod.getNombre() + " añadidas al carrito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            if (carritoPanelInstance != null) {
                carritoPanelInstance.refreshCartDisplay();
            }

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error al realizar rollback al añadir al carrito: " + rollbackEx.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error al añadir al carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
    }

    private void realizarCompraDirecta(Producto prod, int cantidad, String direccionEnvio, String correoFactura) {
        if (prod.getStock() < cantidad) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock para la cantidad seleccionada.", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero para comprar.", "Cantidad Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal totalOrderPrice = producto.getPrecio().multiply(new BigDecimal(cantidad)).add(SHIPPING_COST).setScale(2, RoundingMode.HALF_UP);
        
        String idCompra = "COMPRA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        List<ItemCarrito> itemsParaFactura = new ArrayList<>();
        itemsParaFactura.add(new ItemCarrito(prod, cantidad));

        String nombreUsuario = UserSession.getCurrentUserName() != null ? UserSession.getCurrentUserName() : "Cliente";

        boolean facturaEnviada = FacturadorEmail.enviarFacturaPorCorreo(
            correoFactura,
            nombreUsuario,
            idCompra,
            itemsParaFactura,
            totalOrderPrice.doubleValue()
        );

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirma tu compra:\n\n" +
            "Producto: " + prod.getNombre() + "\n" +
            "Cantidad: " + cantidad + "\n" +
            "Total a Pagar: $" + totalOrderPrice.toPlainString() + "\n\n" +
            "Dirección de Envío: " + direccionEnvio + "\n" +
            "Factura " + (facturaEnviada ? "enviada" : "¡ERROR AL ENVIAR!") + " a: " + correoFactura + "\n\n" +
            "¿Deseas finalizar la compra?",
            "Confirmar Compra Directa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection con = null;
            try {
                con = new conexion().getConnection();
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                con.setAutoCommit(false);

                String updateStockSql = "UPDATE Productos SET stock = stock - ? WHERE id_producto = ?";
                PreparedStatement updateStockPs = con.prepareStatement(updateStockSql);
                updateStockPs.setInt(1, cantidad);
                updateStockPs.setInt(2, prod.getId());
                updateStockPs.executeUpdate();

                String insertOrderSql = "INSERT INTO ordenes (id_usuario, fecha_orden, direccion_envio, estado) VALUES (?, NOW(), ?, 'completada')";
                PreparedStatement insertOrderPs = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                insertOrderPs.setInt(1, UserSession.getCurrentUserId());
                insertOrderPs.setString(2, direccionEnvio);
                insertOrderPs.executeUpdate();

                ResultSet generatedOrderKeys = insertOrderPs.getGeneratedKeys();
                int idOrdenGenerada = -1;
                if (generatedOrderKeys.next()) {
                    idOrdenGenerada = generatedOrderKeys.getInt(1);
                } else {
                    throw new SQLException("Fallo al crear la orden, no se obtuvo ID generado.");
                }
                generatedOrderKeys.close();
                insertOrderPs.close();
                System.out.println("Orden creada con ID: " + idOrdenGenerada);

                String insertDetailSql = "INSERT INTO detalle_ordenes (id_orden, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                PreparedStatement insertDetailPs = con.prepareStatement(insertDetailSql);
                insertDetailPs.setInt(1, idOrdenGenerada);
                insertDetailPs.setInt(2, prod.getId());
                insertDetailPs.setInt(3, cantidad);
                insertDetailPs.setBigDecimal(4, prod.getPrecio());
                insertDetailPs.executeUpdate();
                insertDetailPs.close();
                System.out.println("Detalle de orden guardado para producto: " + prod.getNombre());

                con.commit();
                JOptionPane.showMessageDialog(this, "¡Compra directa procesada con éxito!\nRevisa tu correo (" + correoFactura + ") para la factura.", "Éxito de Compra", JOptionPane.INFORMATION_MESSAGE);

                producto.setStock(producto.getStock() - cantidad);
                stockLabel.setText("Stock disponible: " + producto.getStock());
                ((SpinnerNumberModel)cantidadSpinner.getModel()).setMaximum(producto.getStock() > 0 ? producto.getStock() : 1);
                
                if (producto.getStock() == 0) {
                    cantidadSpinner.setValue(0);
                    cantidadSpinner.setEnabled(false);
                    buyNowButton.setEnabled(false);
                    addToCartButton.setEnabled(false);
                    JOptionPane.showMessageDialog(this, "Producto agotado. ¡Vuelve pronto!", "Sin Stock", JOptionPane.INFORMATION_MESSAGE);
                }
                actualizarResumenCompra();

            } catch (SQLException ex) {
                try {
                    if (con != null) con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al realizar rollback: " + rollbackEx.getMessage());
                }
                JOptionPane.showMessageDialog(this, "Error al procesar la compra: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                try {
                    if (con != null) {
                        con.setAutoCommit(true);
                        con.close();
                    }
                } catch (SQLException finalEx) {
                    System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Compra cancelada por el usuario.", "Compra Cancelada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addProductoToWishlist(int productId) {
        if (!UserSession.isLoggedIn()) {
            showLoginRequiredDialog();
            return;
        }
        try (Connection con = new conexion().getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String checkSql = "SELECT id FROM lista_deseos WHERE id_usuario = ? AND id_producto = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setInt(1, UserSession.getCurrentUserId());
            checkPs.setInt(2, productId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Este producto ya está en tu lista de deseos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String insertSql = "INSERT INTO lista_deseos (id_usuario, id_producto, fecha_agregado) VALUES (?, ?, NOW())";
                PreparedStatement insertPs = con.prepareStatement(insertSql);
                insertPs.setInt(1, UserSession.getCurrentUserId());
                insertPs.setInt(2, productId);
                insertPs.executeUpdate();
                JOptionPane.showMessageDialog(this, "Producto añadido a tu lista de deseos.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al añadir a la lista de deseos: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean isValidAddress(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}