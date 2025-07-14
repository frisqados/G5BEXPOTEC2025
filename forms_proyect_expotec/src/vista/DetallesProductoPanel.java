package vista;

import modelo.ItemCarrito;
import modelo.Producto;
import controlador.conexion;
import util.UserSession;
import util.ProductoSeleccionadoListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import forms_proyect_expotec.LoginForm;
import java.util.UUID;

public class DetallesProductoPanel extends JPanel {

    private Producto producto;
    private JSpinner cantidadSpinner;
    private JLabel lblSubtotal, lblEnvio, lblTotal;
    private JLabel stockLabel;
    private JPanel contentPanel;
    private JButton buyNowButton;
    private JButton addToCartButton;
    private JButton addToWishlistButton;
    private JButton backButton;

    private JTextField direccionEnvioField;
    private JTextField correoElectronicoField;

    private CarritoPanel carritoPanelInstance;
    private ProductoSeleccionadoListener navegadorPrincipal;

    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.00");

    // Patrones de validación para dirección y correo electrónico
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

        // Configuración del panel principal
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(30, 30));
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Panel de información del producto (imagen, detalles, botones de acción)
        JPanel detallesProductoInfoPanel = new JPanel(new GridBagLayout());
        detallesProductoInfoPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;

        // Componente: Imagen del Producto
        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(300, 300));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        cargarImagenProducto(lblImagen, producto.getImagen());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        detallesProductoInfoPanel.add(lblImagen, gbc);

        // Panel con los detalles textuales del producto
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints infoGbc = new GridBagConstraints();
        infoGbc.insets = new Insets(8, 5, 8, 5);
        infoGbc.fill = GridBagConstraints.HORIZONTAL;
        infoGbc.anchor = GridBagConstraints.WEST;

        int infoRow = 0;

        JLabel lblNombre = new JLabel("<html><b style='font-size:26px;'>" + producto.getNombre() + "</b></html>");
        infoGbc.gridx = 0; infoGbc.gridy = infoRow++; infoGbc.gridwidth = 2; infoGbc.weightx = 1.0;
        infoPanel.add(lblNombre, infoGbc);

        JLabel lblCategoria = new JLabel("Categoría: " + producto.getCategoria());
        lblCategoria.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lblCategoria.setForeground(new Color(100, 100, 100));
        infoGbc.gridy = infoRow++;
        infoPanel.add(lblCategoria, infoGbc);

        JLabel lblDescripcion = new JLabel("<html><p style='font-size:13px;'>" + producto.getDescripcion() + "</p></html>");
        lblDescripcion.setFont(new Font("SansSerif", Font.PLAIN, 15));
        infoGbc.gridy = infoRow++;
        infoGbc.insets = new Insets(15, 5, 15, 5);
        infoPanel.add(lblDescripcion, infoGbc);
        infoGbc.insets = new Insets(8, 5, 8, 5); // Restablecer insets

        JLabel lblPrecio = new JLabel("<html><b style='font-size:32px; color:#006400;'>$" + producto.getPrecio().setScale(2, RoundingMode.HALF_UP) + "</b></html>");
        infoGbc.gridy = infoRow++;
        infoPanel.add(lblPrecio, infoGbc);

        stockLabel = new JLabel("Stock disponible: " + producto.getStock());
        stockLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        stockLabel.setForeground(producto.getStock() > 0 ? new Color(0, 150, 0) : new Color(200, 50, 50));
        infoGbc.gridy = infoRow++;
        infoPanel.add(stockLabel, infoGbc);

        JLabel lblPublicador = new JLabel("Publicado por: " + (producto.getPublisherName() != null ? producto.getPublisherName() : "Desconocido"));
        lblPublicador.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblPublicador.setForeground(new Color(120, 120, 120));
        infoGbc.gridy = infoRow++;
        infoPanel.add(lblPublicador, infoGbc);

        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("SansSerif", Font.PLAIN, 15));
        infoGbc.gridx = 0; infoGbc.gridy = infoRow; infoGbc.gridwidth = 1;
        infoPanel.add(lblCantidad, infoGbc);

        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, producto.getStock() > 0 ? producto.getStock() : 1, 1);
        cantidadSpinner = new JSpinner(spinnerModel);
        cantidadSpinner.setFont(new Font("SansSerif", Font.PLAIN, 15));
        cantidadSpinner.setPreferredSize(new Dimension(90, 35));
        cantidadSpinner.setEnabled(producto.getStock() > 0);
        infoGbc.gridx = 1; infoGbc.gridy = infoRow++;
        infoPanel.add(cantidadSpinner, infoGbc);
        infoGbc.gridwidth = 2; // Restablecer gridwidth para los siguientes componentes

        JLabel lblDireccionEnvio = new JLabel("Dirección de Envío:");
        lblDireccionEnvio.setFont(new Font("SansSerif", Font.PLAIN, 15));
        infoGbc.gridx = 0; infoGbc.gridy = infoRow; infoGbc.gridwidth = 1;
        infoPanel.add(lblDireccionEnvio, infoGbc);

        direccionEnvioField = new JTextField();
        direccionEnvioField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        infoGbc.gridx = 1; infoGbc.gridy = infoRow++;
        infoPanel.add(direccionEnvioField, infoGbc);
        infoGbc.gridwidth = 2;

        JLabel lblCorreoElectronico = new JLabel("Correo Electrónico (Factura):");
        lblCorreoElectronico.setFont(new Font("SansSerif", Font.PLAIN, 15));
        infoGbc.gridx = 0; infoGbc.gridy = infoRow; infoGbc.gridwidth = 1;
        infoPanel.add(lblCorreoElectronico, infoGbc);

        correoElectronicoField = new JTextField(UserSession.isLoggedIn() ? UserSession.getCurrentUserEmail() : "");
        correoElectronicoField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        correoElectronicoField.setEditable(UserSession.isLoggedIn()); // Si está logueado, su correo no es editable
        infoGbc.gridx = 1; infoGbc.gridy = infoRow++;
        infoPanel.add(correoElectronicoField, infoGbc);
        infoGbc.gridwidth = 2;

        // Panel para los botones de acción
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));

        addToCartButton = new JButton("Añadir al Carrito");
        configurarBoton(addToCartButton, new Color(40, 167, 69), Color.WHITE, new Color(50, 180, 80));
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
        configurarBoton(buyNowButton, new Color(255, 193, 7), Color.BLACK, new Color(255, 205, 50));
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
        configurarBoton(addToWishlistButton, new Color(220, 53, 69), Color.WHITE, new Color(230, 65, 80));
        addToWishlistButton.addActionListener(e -> {
            if (!UserSession.isLoggedIn()) {
                showLoginRequiredDialog();
            } else {
                addProductoToWishlist(producto.getId());
            }
        });
        actionButtonPanel.add(addToWishlistButton);

        backButton = new JButton("Volver al Catálogo");
        configurarBoton(backButton, new Color(108, 117, 125), Color.WHITE, new Color(120, 130, 140));
        backButton.addActionListener(e -> {
            if (navegadorPrincipal != null) {
                navegadorPrincipal.volverAlCatalogo();
            }
        });
        actionButtonPanel.add(backButton);

        infoGbc.gridx = 0; infoGbc.gridy = infoRow++; infoGbc.gridwidth = 2;
        infoGbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(actionButtonPanel, infoGbc);

        // Panel de resumen de compra
        JPanel resumenPanel = new JPanel(new GridBagLayout());
        resumenPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 255), 1, true),
            "Resumen de Compra",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 14),
            new Color(90, 90, 90)
        ));
        GridBagConstraints resGbc = new GridBagConstraints();
        resGbc.insets = new Insets(8, 15, 8, 15);
        resGbc.fill = GridBagConstraints.HORIZONTAL;
        resGbc.anchor = GridBagConstraints.WEST;
        resGbc.weightx = 1.0;

        JLabel lblResumen = new JLabel("Subtotal:");
        lblResumen.setFont(new Font("SansSerif", Font.PLAIN, 15));
        resGbc.gridx = 0; resGbc.gridy = 0;
        resumenPanel.add(lblResumen, resGbc);
        lblSubtotal = new JLabel("$0.00");
        lblSubtotal.setFont(new Font("SansSerif", Font.BOLD, 15));
        resGbc.gridx = 1; resGbc.gridy = 0;
        resumenPanel.add(lblSubtotal, resGbc);

        JLabel lblResumenEnvio = new JLabel("Envío:");
        lblResumenEnvio.setFont(new Font("SansSerif", Font.PLAIN, 15));
        resGbc.gridx = 0; resGbc.gridy = 1;
        resumenPanel.add(lblResumenEnvio, resGbc);
        lblEnvio = new JLabel("$" + SHIPPING_COST.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lblEnvio.setFont(new Font("SansSerif", Font.BOLD, 15));
        resGbc.gridx = 1; resGbc.gridy = 1;
        resumenPanel.add(lblEnvio, resGbc);

        resGbc.gridx = 0; resGbc.gridy = 2; resGbc.gridwidth = 2;
        resGbc.insets = new Insets(5, 15, 5, 15);
        resumenPanel.add(new JSeparator(), resGbc);
        resGbc.insets = new Insets(8, 15, 8, 15);

        JLabel lblResumenTotal = new JLabel("Total:");
        lblResumenTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        resGbc.gridx = 0; resGbc.gridy = 3; resGbc.gridwidth = 1;
        resumenPanel.add(lblResumenTotal, resGbc);
        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotal.setForeground(new Color(0, 120, 0));
        resGbc.gridx = 1; resGbc.gridy = 3;
        resumenPanel.add(lblTotal, resGbc);

        cantidadSpinner.addChangeListener(e -> actualizarResumenCompra());
        actualizarResumenCompra();

        infoGbc.gridx = 0; infoGbc.gridy = infoRow++; infoGbc.gridwidth = 2;
        infoGbc.fill = GridBagConstraints.BOTH;
        infoGbc.weighty = 1.0;
        infoGbc.insets = new Insets(25, 5, 5, 5);
        infoPanel.add(resumenPanel, infoGbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        detallesProductoInfoPanel.add(infoPanel, gbc);

        contentPanel.add(detallesProductoInfoPanel, BorderLayout.NORTH);

        ReseñasPanel reseñasPanel = new ReseñasPanel(producto);
        contentPanel.add(reseñasPanel, BorderLayout.CENTER);

        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.setLayout(new BorderLayout());
        this.add(mainScrollPane, BorderLayout.CENTER);
    }

    // Constructor secundario para compatibilidad
    public DetallesProductoPanel(Producto producto, CarritoPanel carritoPanelInstance) {
        this(producto, carritoPanelInstance, null);
    }

    /**
     * Carga y escala la imagen del producto para mostrarla en un JLabel.
     * @param lblImagen El JLabel donde se mostrará la imagen.
     * @param imagenBytes Los bytes de la imagen del producto.
     */
    private void cargarImagenProducto(JLabel lblImagen, byte[] imagenBytes) {
        if (imagenBytes != null && imagenBytes.length > 0) {
            try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imagenBytes));
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
    }

    /**
     * Configura un botón con colores iniciales, de texto y de hover, además de estilo.
     * @param button El JButton a configurar.
     * @param initialColor Color de fondo inicial.
     * @param textColor Color del texto.
     * @param hoverColor Color de fondo al pasar el ratón.
     */
    private void configurarBoton(JButton button, Color initialColor, Color textColor, Color hoverColor) {
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setBackground(initialColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addHoverAnimation(button, initialColor, hoverColor);
    }

    /**
     * Añade una animación de "hover" a un botón, cambiando gradualmente su color de fondo.
     * @param button El JButton al que se le añade la animación.
     * @param initialColor El color de fondo original del botón.
     * @param hoverColor El color de fondo cuando el ratón está sobre el botón.
     */
    private void addHoverAnimation(JButton button, Color initialColor, Color hoverColor) {
        button.addMouseListener(new MouseAdapter() {
            private Timer timer;
            private Color currentColor;

            @Override
            public void mouseEntered(MouseEvent e) {
                currentColor = button.getBackground();
                if (timer != null && timer.isRunning()) {
                    timer.stop();
                }
                timer = new Timer(10, new AbstractAction() {
                    float progress = 0;
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent ae) {
                        progress += 0.1f;
                        if (progress >= 1.0f) {
                            progress = 1.0f;
                            timer.stop();
                        }
                        int r = (int) (currentColor.getRed() + (hoverColor.getRed() - currentColor.getRed()) * progress);
                        int g = (int) (currentColor.getGreen() + (hoverColor.getGreen() - currentColor.getGreen()) * progress);
                        int b = (int) (currentColor.getBlue() + (hoverColor.getBlue() - currentColor.getBlue()) * progress);
                        button.setBackground(new Color(r, g, b));
                    }
                });
                timer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                currentColor = button.getBackground();
                if (timer != null && timer.isRunning()) {
                    timer.stop();
                }
                timer = new Timer(10, new AbstractAction() {
                    float progress = 0;
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent ae) {
                        progress += 0.1f;
                        if (progress >= 1.0f) {
                            progress = 1.0f;
                            timer.stop();
                        }
                        int r = (int) (currentColor.getRed() + (initialColor.getRed() - currentColor.getRed()) * progress);
                        int g = (int) (currentColor.getGreen() + (initialColor.getGreen() - currentColor.getGreen()) * progress);
                        int b = (int) (currentColor.getBlue() + (initialColor.getBlue() - currentColor.getBlue()) * progress);
                        button.setBackground(new Color(r, g, b));
                    }
                });
                timer.start();
            }
        });
    }

    /**
     * Actualiza los labels de subtotal, envío y total en el panel de resumen de compra.
     */
    private void actualizarResumenCompra() {
        int cantidad = (int) cantidadSpinner.getValue();
        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        
        // El costo de envío se aplica solo si la cantidad es mayor que 0
        BigDecimal total = subtotal.add(cantidad > 0 ? SHIPPING_COST : BigDecimal.ZERO);

        lblSubtotal.setText("$" + subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lblEnvio.setText("$" + SHIPPING_COST.setScale(2, RoundingMode.HALF_UP).toPlainString());
        lblTotal.setText("$" + total.setScale(2, RoundingMode.HALF_UP).toPlainString());

        boolean hayStock = producto.getStock() > 0;
        boolean cantidadValida = cantidad > 0;
        buyNowButton.setEnabled(hayStock && cantidadValida);
        addToCartButton.setEnabled(hayStock && cantidadValida);
        cantidadSpinner.setEnabled(hayStock);
        
        stockLabel.setForeground(producto.getStock() > 0 ? new Color(0, 150, 0) : new Color(200, 50, 50));
    }

    /**
     * Muestra un diálogo solicitando al usuario que inicie sesión si no lo está.
     */
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
    
    /**
     * Maneja la acción de "Comprar Ahora", incluyendo validaciones y llamada a `realizarCompraDirecta`.
     */
    private void handleBuyNowAction() {
        String direccionEnvio = direccionEnvioField.getText().trim();
        String correoFactura = correoElectronicoField.getText().trim();

        if (!UserSession.isLoggedIn()) {
            showLoginRequiredDialog();
            return;
        }

        // Validar y solicitar dirección de envío si es necesario
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

        // Validar y solicitar correo electrónico si es necesario
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

    /**
     * Agrega el producto al carrito del usuario, manejando la creación del carrito si no existe
     * y la actualización de la cantidad si el producto ya está en el carrito.
     * @param prod El producto a añadir.
     * @param cantidad La cantidad del producto a añadir.
     */
    private void agregarAlCarrito(Producto prod, int cantidad) {
        if (!UserSession.isLoggedIn()) {
            showLoginRequiredDialog();
            return;
        }

        String direccionEnvio = direccionEnvioField.getText().trim(); // Se obtiene para la validación, aunque no se guarda directamente aquí

        if (prod.getStock() < cantidad) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock para la cantidad seleccionada.", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero.", "Cantidad Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Se solicita la dirección de envío si no está presente, aunque se guarda a nivel de orden,
        // se pide para asegurar que el usuario la ha proporcionado antes de añadir al carrito.
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
            direccionEnvioField.setText(direccionEnvio); // Actualiza el campo de dirección
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            con.setAutoCommit(false); // Iniciar transacción

            int idCarrito = -1;
            String getCarritoSql = "SELECT id_carrito FROM carritos WHERE id_usuario = ?";
            PreparedStatement getCarritoPs = con.prepareStatement(getCarritoSql);
            getCarritoPs.setInt(1, UserSession.getCurrentUserId());
            ResultSet rsCarrito = getCarritoPs.executeQuery();

            if (rsCarrito.next()) {
                idCarrito = rsCarrito.getInt("id_carrito");
            } else {
                // Si no hay carrito, se crea uno nuevo para el usuario
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
                generatedKeys.close();
                insertCarritoPs.close();
            }
            rsCarrito.close();
            getCarritoPs.close();

            // Verificar si el producto ya existe en el carrito
            String checkItemSql = "SELECT id_item, cantidad FROM carrito_items WHERE id_carrito = ? AND id_producto = ?";
            PreparedStatement checkItemPs = con.prepareStatement(checkItemSql);
            checkItemPs.setInt(1, idCarrito);
            checkItemPs.setInt(2, prod.getId());
            ResultSet rsItem = checkItemPs.executeQuery();

            if (rsItem.next()) {
                // Si el producto ya está, se actualiza la cantidad
                int currentCantidad = rsItem.getInt("cantidad");
                String updateItemSql = "UPDATE carrito_items SET cantidad = ? WHERE id_item = ?";
                PreparedStatement updateItemPs = con.prepareStatement(updateItemSql);
                updateItemPs.setInt(1, currentCantidad + cantidad);
                updateItemPs.setInt(2, rsItem.getInt("id_item"));
                updateItemPs.executeUpdate();
                updateItemPs.close();
            } else {
                // Si el producto no está, se inserta como nuevo item
                String insertItemSql = "INSERT INTO carrito_items (id_carrito, id_producto, cantidad) VALUES (?, ?, ?)";
                PreparedStatement insertItemPs = con.prepareStatement(insertItemSql);
                insertItemPs.setInt(1, idCarrito);
                insertItemPs.setInt(2, prod.getId());
                insertItemPs.setInt(3, cantidad);
                insertItemPs.executeUpdate();
                insertItemPs.close();
            }
            rsItem.close();
            checkItemPs.close();

            con.commit(); // Confirmar la transacción
            JOptionPane.showMessageDialog(this, cantidad + " unidades de " + prod.getNombre() + " añadidas al carrito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la visualización del carrito en el panel principal
            if (carritoPanelInstance != null) {
                carritoPanelInstance.refreshCartDisplay();
            }

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback(); // Revertir transacción en caso de error
            } catch (SQLException rollbackEx) {
                System.err.println("Error al realizar rollback al añadir al carrito: " + rollbackEx.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error al añadir al carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true); // Restaurar auto-commit
                    con.close(); // Cerrar conexión
                }
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
    }

    /**
     * Procesa la compra directa de un producto, actualiza el stock, guarda la orden,
     * los detalles de la orden y el historial de envío, y envía una factura por correo.
     * @param prod El producto a comprar.
     * @param cantidad La cantidad del producto.
     * @param direccionEnvio La dirección a la que se enviará la orden.
     * @param correoFactura El correo electrónico para enviar la factura.
     */
    private void realizarCompraDirecta(Producto prod, int cantidad, String direccionEnvio, String correoFactura) {
        if (prod.getStock() < cantidad) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock para la cantidad seleccionada.", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero para comprar.", "Cantidad Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Calcular el precio total de la orden, incluyendo el costo de envío.
        BigDecimal totalOrderPrice = producto.getPrecio().multiply(new BigDecimal(cantidad)).add(SHIPPING_COST).setScale(2, RoundingMode.HALF_UP);

        // Generar un ID de compra único para la factura y referencia interna
        String idCompra = "COMPRA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Preparar la lista de ítems para la factura (en este caso, un solo producto)
        List<ItemCarrito> itemsParaFactura = new ArrayList<>();
        itemsParaFactura.add(new ItemCarrito(prod, cantidad));

        String nombreUsuario = UserSession.getCurrentUserName() != null ? UserSession.getCurrentUserName() : "Cliente";

        // Intenta enviar la factura por correo electrónico
        // (Nota: Los errores de conexión de correo como MailConnectException se manejan dentro de FacturadorEmail,
        // pero la bandera `facturaEnviada` indicará el éxito o fracaso aquí).
        boolean facturaEnviada = FacturadorEmail.enviarFacturaPorCorreo(
            correoFactura,
            nombreUsuario,
            idCompra,
            itemsParaFactura,
            totalOrderPrice.doubleValue()
        );

        // Mostrar confirmación al usuario antes de finalizar la compra en la BD
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
                con.setAutoCommit(false); // Iniciar transacción

                // 1. Actualizar stock del producto
                String updateStockSql = "UPDATE Productos SET stock = stock - ? WHERE id_producto = ?";
                PreparedStatement updateStockPs = con.prepareStatement(updateStockSql);
                updateStockPs.setInt(1, cantidad);
                updateStockPs.setInt(2, prod.getId());
                updateStockPs.executeUpdate();
                updateStockPs.close();


                // 2. Insertar la orden principal en la tabla 'ordenes'
                // NO se incluye 'id_factura_externa' aquí, ya que no existe en tu esquema de DB.
                // Los parámetros son: id_usuario, fecha_orden (NOW()), direccion_envio, estado, total_orden
                String insertOrderSql = "INSERT INTO ordenes (id_usuario, fecha_orden, direccion_envio, estado, total_orden) VALUES (?, NOW(), ?, 'pagado', ?)";
                PreparedStatement insertOrderPs = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                insertOrderPs.setInt(1, UserSession.getCurrentUserId());
                insertOrderPs.setString(2, direccionEnvio);
                insertOrderPs.setBigDecimal(3, totalOrderPrice);
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

                // 3. Insertar los detalles de la orden (productos comprados en esta orden)
                String insertDetailSql = "INSERT INTO detalle_ordenes (id_orden, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                PreparedStatement insertDetailPs = con.prepareStatement(insertDetailSql);
                insertDetailPs.setInt(1, idOrdenGenerada);
                insertDetailPs.setInt(2, prod.getId());
                insertDetailPs.setInt(3, cantidad);
                insertDetailPs.setBigDecimal(4, prod.getPrecio());
                insertDetailPs.executeUpdate();
                insertDetailPs.close();
                System.out.println("Detalle de orden guardado para producto: " + prod.getNombre());


                // 4. Insertar el primer registro en el historial de envío
                String insertHistorialEnvioSql = "INSERT INTO historial_envio_orden (id_orden, estado_orden, ubicacion_actual, fecha_hora_evento, notas) VALUES (?, ?, ?, NOW(), ?)";
                PreparedStatement insertHistorialEnvioPs = con.prepareStatement(insertHistorialEnvioSql);
                insertHistorialEnvioPs.setInt(1, idOrdenGenerada);
                insertHistorialEnvioPs.setString(2, "Orden Confirmada"); // Estado inicial
                insertHistorialEnvioPs.setString(3, "Almacén Principal, Guatemala"); // Ubicación inicial
                insertHistorialEnvioPs.setString(4, "Su pedido ha sido recibido y está siendo procesado."); // Nota inicial
                insertHistorialEnvioPs.executeUpdate();
                insertHistorialEnvioPs.close();
                System.out.println("Primer registro de historial de envío guardado para la orden ID: " + idOrdenGenerada);


                con.commit(); // Confirmar la transacción
                JOptionPane.showMessageDialog(this, "¡Compra directa procesada con éxito!\nRevisa tu correo (" + correoFactura + ") para la factura.", "Éxito de Compra", JOptionPane.INFORMATION_MESSAGE);

                // Actualizar la UI del producto en tiempo real
                producto.setStock(producto.getStock() - cantidad); // Restar la cantidad comprada del stock local
                stockLabel.setText("Stock disponible: " + producto.getStock()); // Actualizar etiqueta de stock
                // Ajustar el valor máximo del spinner para no permitir comprar más del stock restante
                ((SpinnerNumberModel)cantidadSpinner.getModel()).setMaximum(producto.getStock() > 0 ? producto.getStock() : 1);
                // Si el stock llega a cero, deshabilitar botones y spinner
                if (producto.getStock() == 0) {
                    cantidadSpinner.setValue(0);
                    cantidadSpinner.setEnabled(false);
                    buyNowButton.setEnabled(false);
                    addToCartButton.setEnabled(false);
                    JOptionPane.showMessageDialog(this, "Producto agotado. ¡Vuelve pronto!", "Sin Stock", JOptionPane.INFORMATION_MESSAGE);
                }
                actualizarResumenCompra(); // Volver a calcular y mostrar los totales

            } catch (SQLException ex) {
                try {
                    if (con != null) con.rollback(); // Revertir transacción en caso de error
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al realizar rollback: " + rollbackEx.getMessage());
                }
                JOptionPane.showMessageDialog(this, "Error al procesar la compra: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                try {
                    if (con != null) {
                        con.setAutoCommit(true); // Restaurar auto-commit
                        con.close(); // Cerrar conexión
                    }
                } catch (SQLException finalEx) {
                    System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Compra cancelada por el usuario.", "Compra Cancelada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Añade un producto a la lista de deseos del usuario si no está ya presente.
     * @param productId El ID del producto a añadir.
     */
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

            // Comprobar si el producto ya está en la lista de deseos
            String checkSql = "SELECT id FROM lista_deseos WHERE id_usuario = ? AND id_producto = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setInt(1, UserSession.getCurrentUserId());
            checkPs.setInt(2, productId);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Este producto ya está en tu lista de deseos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Si no está, insertarlo
                String insertSql = "INSERT INTO lista_deseos (id_usuario, id_producto, fecha_agregado) VALUES (?, ?, NOW())";
                PreparedStatement insertPs = con.prepareStatement(insertSql);
                insertPs.setInt(1, UserSession.getCurrentUserId());
                insertPs.setInt(2, productId);
                insertPs.executeUpdate();
                JOptionPane.showMessageDialog(this, "Producto añadido a tu lista de deseos.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            rs.close();
            checkPs.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al añadir a la lista de deseos: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Valida una dirección de envío usando un patrón de expresión regular.
     * @param address La dirección a validar.
     * @return true si la dirección es válida, false en caso contrario.
     */
    private boolean isValidAddress(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }

    /**
     * Valida un correo electrónico usando un patrón de expresión regular.
     * @param email El correo electrónico a validar.
     * @return true si el correo electrónico es válido, false en caso contrario.
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}