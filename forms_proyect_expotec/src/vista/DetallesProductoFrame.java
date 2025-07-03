package vista;

import modelo.Producto;
import controlador.conexion;
import util.UserSession;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.nio.file.Files;

import forms_proyect_expotec.CarritoForm; // Asegúrate de que esta importación sea correcta si CarritoForm está en otro paquete

public class DetallesProductoFrame extends JFrame {

    private Producto producto;
    private JSpinner cantidadSpinner;
    private JLabel lblSubtotal, lblEnvio, lblTotal;
    private JLabel stockLabel;
    private JPanel detallesPanel;
    private JButton buyNowButton;
    private JButton addToCartButton;
    private JButton addToWishlistButton; // Nuevo botón para lista de deseos

    private CarritoForm carritoFormInstance;

    public DetallesProductoFrame(Producto producto, CarritoForm carritoFormInstance) {
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        this.producto = producto;
        this.carritoFormInstance = carritoFormInstance;

        setTitle("Detalles del Producto: " + producto.getNombre());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(25, 25));

        Font labelFont = new Font("SansSerif", Font.BOLD, 16);
        Font valueFont = new Font("SansSerif", Font.PLAIN, 16);
        Font priceFont = new Font("SansSerif", Font.BOLD, 24);
        Font descriptionFont = new Font("SansSerif", Font.PLAIN, 14);
        Font summaryFont = new Font("SansSerif", Font.BOLD, 16);

        JPanel imagenPanel = new JPanel(new BorderLayout());
        imagenPanel.setBackground(Color.WHITE);
        imagenPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel imagenLabel = new JLabel();
        imagenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagenLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagenLabel.setOpaque(true);
        imagenLabel.setBackground(new Color(240, 240, 240));

        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(producto.getImagen()));
                if (originalImage != null) {
                    imagenLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
                        @Override
                        public void componentResized(java.awt.event.ComponentEvent e) {
                            int labelWidth = imagenLabel.getWidth();
                            int labelHeight = imagenLabel.getHeight();
                            if (labelWidth > 0 && labelHeight > 0) {
                                double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                                int newWidth = labelWidth;
                                int newHeight = (int) (newWidth / aspectRatio);
                                if (newHeight > labelHeight) {
                                    newHeight = labelHeight;
                                    newWidth = (int) (newHeight * aspectRatio);
                                }
                                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                                imagenLabel.setIcon(new ImageIcon(scaledImage));
                            }
                        }
                    });
                } else {
                    imagenLabel.setText("Error al leer la imagen");
                    imagenLabel.setForeground(Color.RED);
                    imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
                }
            } catch (IOException ex) {
                System.err.println("Error al cargar imagen para detalles: " + ex.getMessage());
                imagenLabel.setText("No hay imagen disponible");
                imagenLabel.setForeground(Color.DARK_GRAY);
                imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            }
        } else {
            imagenLabel.setText("No hay imagen disponible");
            imagenLabel.setForeground(Color.DARK_GRAY);
            imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
        }
        imagenPanel.add(imagenLabel, BorderLayout.CENTER);
        add(imagenPanel, BorderLayout.WEST);

        detallesPanel = new JPanel();
        detallesPanel.setLayout(new GridBagLayout());
        detallesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        detallesPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel nombreLabel = new JLabel("<html><b>" + producto.getNombre() + "</b></html>");
        nombreLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        nombreLabel.setForeground(new Color(20, 20, 20));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        detallesPanel.add(nombreLabel, gbc);
        row++;

        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel precioLabel = new JLabel("<html><b>$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "</b></html>");
        precioLabel.setFont(priceFont);
        precioLabel.setForeground(new Color(178, 34, 34));
        gbc.gridy = row;
        detallesPanel.add(precioLabel, gbc);
        row++;

        gbc.insets = new Insets(15, 0, 5, 0);

        JLabel descripcionTitle = new JLabel("Descripción:");
        descripcionTitle.setFont(labelFont);
        gbc.gridy = row;
        detallesPanel.add(descripcionTitle, gbc);
        row++;

        JTextArea descripcionArea = new JTextArea(producto.getDescripcion());
        descripcionArea.setFont(descriptionFont);
        descripcionArea.setWrapStyleWord(true);
        descripcionArea.setLineWrap(true);
        descripcionArea.setEditable(false);
        descripcionArea.setBackground(detallesPanel.getBackground());
        descripcionArea.setBorder(null);
        descripcionArea.setSelectionColor(new Color(255, 204, 153));

        JScrollPane scrollDesc = new JScrollPane(descripcionArea);
        scrollDesc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDesc.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        scrollDesc.setPreferredSize(new Dimension(500, 150));

        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        detallesPanel.add(scrollDesc, gbc);
        gbc.weighty = 0;
        row++;

        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel categoriaLabel = new JLabel("<html><b>Categoría:</b> " + producto.getCategoria() + "</html>");
        categoriaLabel.setFont(valueFont);
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        detallesPanel.add(categoriaLabel, gbc);
        row++;

        stockLabel = new JLabel("<html><b>Stock Disponible:</b> " + producto.getStock() + " unidades</html>");
        stockLabel.setFont(valueFont);
        stockLabel.setForeground(producto.getStock() > 0 ? Color.BLACK : Color.RED);
        gbc.gridy = row;
        detallesPanel.add(stockLabel, gbc);
        row++;

        gbc.insets = new Insets(20, 0, 10, 0);
        JLabel cantidadLabel = new JLabel("Cantidad:");
        cantidadLabel.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        detallesPanel.add(cantidadLabel, gbc);

        SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, producto.getStock() > 0 ? producto.getStock() : 1, 1);
        cantidadSpinner = new JSpinner(spinnerModel);
        cantidadSpinner.setFont(valueFont);
        cantidadSpinner.setPreferredSize(new Dimension(80, 30));
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        detallesPanel.add(cantidadSpinner, gbc);
        row++;

        if (producto.getStock() <= 0) {
            cantidadSpinner.setEnabled(false);
            cantidadSpinner.setValue(0);
        }

        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        detallesPanel.add(new JSeparator(), gbc);
        row++;

        lblSubtotal = new JLabel("Subtotal: $0.00");
        lblSubtotal.setFont(summaryFont);
        gbc.gridy = row;
        detallesPanel.add(lblSubtotal, gbc);
        row++;

        lblEnvio = new JLabel("Costo de Envío (15%): $0.00");
        lblEnvio.setFont(summaryFont);
        gbc.gridy = row;
        detallesPanel.add(lblEnvio, gbc);
        row++;

        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTotal.setForeground(new Color(0, 128, 0));
        gbc.gridy = row;
        detallesPanel.add(lblTotal, gbc);
        row++;

        cantidadSpinner.addChangeListener(e -> actualizarResumenCompra());
        actualizarResumenCompra();

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionButtonPanel.setBackground(Color.WHITE);

        addToCartButton = new JButton("Añadir al Carrito");
        addToCartButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addToCartButton.setBackground(new Color(255, 164, 28));
        addToCartButton.setForeground(Color.BLACK);
        addToCartButton.setFocusPainted(false);
        addToCartButton.setBorderPainted(false);
        addToCartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boolean hasStock = producto.getStock() > 0;
        addToCartButton.setEnabled(hasStock && UserSession.isLoggedIn());
        actionButtonPanel.add(addToCartButton);
        addToCartButton.addActionListener(e -> agregarAlCarrito());

        buyNowButton = new JButton("Comprar Ahora");
        buyNowButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        buyNowButton.setBackground(new Color(255, 153, 0));
        buyNowButton.setForeground(Color.WHITE);
        buyNowButton.setFocusPainted(false);
        buyNowButton.setBorderPainted(false);
        buyNowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buyNowButton.setEnabled(hasStock && UserSession.isLoggedIn());
        buyNowButton.addActionListener(e -> realizarCompra());
        actionButtonPanel.add(buyNowButton);

        // --- Nuevo: Botón de "Agregar a Lista de Deseos" ---
        addToWishlistButton = new JButton("Agregar a Lista de Deseos");
        addToWishlistButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addToWishlistButton.setBackground(new Color(173, 216, 230)); // Azul claro
        addToWishlistButton.setForeground(Color.BLACK);
        addToWishlistButton.setFocusPainted(false);
        addToWishlistButton.setBorderPainted(false);
        addToWishlistButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToWishlistButton.setEnabled(UserSession.isLoggedIn()); // Solo habilitado si hay sesión
        addToWishlistButton.addActionListener(e -> addProductoToWishlist(producto.getId()));
        actionButtonPanel.add(addToWishlistButton);
        // --- Fin Nuevo ---


        if (!hasStock) {
            JLabel noStockMsg = new JLabel("Producto sin stock.", SwingConstants.CENTER);
            noStockMsg.setFont(new Font("SansSerif", Font.BOLD, 16));
            noStockMsg.setForeground(Color.RED);
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(10, 0, 0, 0);
            detallesPanel.add(noStockMsg, gbc);
            row++;
        } else if (!UserSession.isLoggedIn()) {
            JLabel loginMsg = new JLabel("Inicia sesión para añadir al carrito, comprar o agregar a deseos.", SwingConstants.CENTER); // Mensaje actualizado
            loginMsg.setFont(new Font("SansSerif", Font.BOLD, 16));
            loginMsg.setForeground(new Color(0, 100, 0));
            gbc.gridy = row;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(10, 0, 0, 0);
            detallesPanel.add(loginMsg, gbc);
            row++;
        }

        gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        detallesPanel.add(actionButtonPanel, gbc);

        add(detallesPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public DetallesProductoFrame(Producto producto) {
        this(producto, null);
    }

    private void actualizarResumenCompra() {
        int cantidad = (int) cantidadSpinner.getValue();
        BigDecimal precioUnitario = producto.getPrecio();

        BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        BigDecimal costoEnvio = subtotal.multiply(new BigDecimal("0.15"));
        BigDecimal total = subtotal.add(costoEnvio);

        lblSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        lblEnvio.setText(String.format("Costo de Envío (15%%): $%.2f", costoEnvio));
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    private void agregarAlCarrito() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para añadir productos al carrito.", "Error de Sesión", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidadAAnadir = (int) cantidadSpinner.getValue();
        if (cantidadAAnadir <= 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una cantidad válida para añadir al carrito.", "Error de Cantidad", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidadAAnadir > producto.getStock()) {
            JOptionPane.showMessageDialog(this, "La cantidad solicitada excede el stock disponible (" + producto.getStock() + ").", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int currentUserId = UserSession.getCurrentUserId();
        conexion con = new conexion();
        Connection connection = null;
        PreparedStatement psGetCarritoId = null;
        PreparedStatement psInsertCarrito = null;
        PreparedStatement psCheckExistingItem = null;
        PreparedStatement psUpdateItem = null;
        PreparedStatement psInsertNewItem = null;
        ResultSet rs = null;
        ResultSet rsItem = null;

        try {
            connection = con.getConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            connection.setAutoCommit(false);

            int idCarrito = -1;

            String sqlGetCarritoId = "SELECT id_carrito FROM Carritos WHERE id_usuario = ?";
            psGetCarritoId = connection.prepareStatement(sqlGetCarritoId);
            psGetCarritoId.setInt(1, currentUserId);
            rs = psGetCarritoId.executeQuery();

            if (rs.next()) {
                idCarrito = rs.getInt("id_carrito");
            } else {
                String sqlInsertCarrito = "INSERT INTO Carritos (id_usuario, fecha_agregado) VALUES (?, CURRENT_TIMESTAMP)";
                psInsertCarrito = connection.prepareStatement(sqlInsertCarrito, Statement.RETURN_GENERATED_KEYS);
                psInsertCarrito.setInt(1, currentUserId);
                psInsertCarrito.executeUpdate();

                ResultSet generatedKeys = psInsertCarrito.getGeneratedKeys();
                if (generatedKeys.next()) {
                    idCarrito = generatedKeys.getInt(1);
                    System.out.println("Nuevo carrito creado con ID: " + idCarrito + " para el usuario " + currentUserId);
                } else {
                    throw new SQLException("No se pudo obtener el ID del carrito generado.");
                }
                generatedKeys.close();
            }

            String sqlCheckExistingItem = "SELECT id_item, cantidad FROM Carrito_Items WHERE id_carrito = ? AND id_producto = ?";
            psCheckExistingItem = connection.prepareStatement(sqlCheckExistingItem);
            psCheckExistingItem.setInt(1, idCarrito);
            psCheckExistingItem.setInt(2, producto.getId());
            rsItem = psCheckExistingItem.executeQuery();

            if (rsItem.next()) {
                int currentQuantityInCart = rsItem.getInt("cantidad");
                int idItemToUpdate = rsItem.getInt("id_item");

                String sqlUpdateItem = "UPDATE Carrito_Items SET cantidad = ? WHERE id_item = ?";
                psUpdateItem = connection.prepareStatement(sqlUpdateItem);
                psUpdateItem.setInt(1, currentQuantityInCart + cantidadAAnadir);
                psUpdateItem.setInt(2, idItemToUpdate);
                psUpdateItem.executeUpdate();
                System.out.println("Producto " + producto.getNombre() + " actualizado en el carrito. Nueva cantidad: " + (currentQuantityInCart + cantidadAAnadir));
            } else {
                String sqlInsertNewItem = "INSERT INTO Carrito_Items (id_carrito, id_producto, cantidad) VALUES (?, ?, ?)";
                psInsertNewItem = connection.prepareStatement(sqlInsertNewItem);
                psInsertNewItem.setInt(1, idCarrito);
                psInsertNewItem.setInt(2, producto.getId());
                psInsertNewItem.setInt(3, cantidadAAnadir);
                psInsertNewItem.executeUpdate();
                System.out.println("Producto " + producto.getNombre() + " añadido al carrito. Cantidad: " + cantidadAAnadir);
            }

            connection.commit();
            JOptionPane.showMessageDialog(this, "¡Producto(s) añadido(s) al carrito con éxito!", "Añadido al Carrito", JOptionPane.INFORMATION_MESSAGE);

            if (carritoFormInstance != null && carritoFormInstance.isVisible()) {
                carritoFormInstance.loadCarritoItems();
            } else {
                int openCart = JOptionPane.showConfirmDialog(this, "¿Deseas ver tu carrito de compras ahora?", "Ver Carrito", JOptionPane.YES_NO_OPTION);
                if (openCart == JOptionPane.YES_OPTION) {
                    if (carritoFormInstance == null) {
                        carritoFormInstance = new CarritoForm();
                    }
                    carritoFormInstance.setVisible(true);
                    carritoFormInstance.toFront();
                    carritoFormInstance.loadCarritoItems();
                }
            }

        } catch (SQLException e) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error al revertir la transacción de carrito: " + ex.getMessage());
            }
            System.err.println("Error SQL al añadir al carrito: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al añadir al carrito: " + e.getMessage(), "Error de Carrito", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (rsItem != null) {
                    rsItem.close();
                }
                if (psGetCarritoId != null) {
                    psGetCarritoId.close();
                }
                if (psInsertCarrito != null) {
                    psInsertCarrito.close();
                }
                if (psCheckExistingItem != null) {
                    psCheckExistingItem.close();
                }
                if (psUpdateItem != null) {
                    psUpdateItem.close();
                }
                if (psInsertNewItem != null) {
                    psInsertNewItem.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.setAutoCommit(true);
                    con.desconectar();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de DB en añadir al carrito: " + e.getMessage());
            }
        }
    }

    /**
     * Procesa la compra del producto seleccionado. Crea una nueva orden, sus
     * detalles y actualiza el stock del producto. Incluye una verificación del
     * límite de crédito del usuario y debita el monto. Utiliza transacciones
     * para asegurar la integridad de los datos.
     */
    private void realizarCompra() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para poder realizar una compra.", "Error de Sesión", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad = (int) cantidadSpinner.getValue();
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una cantidad válida.", "Error de Cantidad", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantidad > producto.getStock()) {
            JOptionPane.showMessageDialog(this, "La cantidad solicitada excede el stock disponible (" + producto.getStock() + ").", "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal precioUnitario = producto.getPrecio();
        BigDecimal subtotalCalculado = precioUnitario.multiply(new BigDecimal(cantidad));
        BigDecimal costoEnvioCalculado = subtotalCalculado.multiply(new BigDecimal("0.15"));
        BigDecimal totalCompra = subtotalCalculado.add(costoEnvioCalculado);

        int currentUserId = UserSession.getCurrentUserId();

        // --- INICIO DE LA TRANSACCIÓN COMPLETA DE COMPRA Y DÉBITO ---
        conexion conPurchase = new conexion();
        Connection connectionPurchase = null;
        PreparedStatement psCheckCredit = null;
        PreparedStatement psUpdateCredit = null;
        PreparedStatement psOrden = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psUpdateStock = null;
        ResultSet rsCredit = null;
        ResultSet rsOrder = null;

        try {
            connectionPurchase = conPurchase.getConnection();
            if (connectionPurchase == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para realizar la compra.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            connectionPurchase.setAutoCommit(false); // Iniciar transacción

            // 1. Verificar el límite de crédito
            String sqlCheckCredit = "SELECT limite_credito FROM tarjeta_credito WHERE usuario_id = ? FOR UPDATE"; // Bloquear la fila para evitar concurrencia
            psCheckCredit = connectionPurchase.prepareStatement(sqlCheckCredit);
            psCheckCredit.setInt(1, currentUserId);
            rsCredit = psCheckCredit.executeQuery();

            BigDecimal limiteCredito = BigDecimal.ZERO;
            if (rsCredit.next()) {
                limiteCredito = rsCredit.getBigDecimal("limite_credito");
                System.out.println("Límite de crédito del usuario " + currentUserId + ": $" + limiteCredito);
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró una tarjeta de crédito asociada a tu cuenta. No se puede realizar la compra.", "Error de Pago", JOptionPane.ERROR_MESSAGE);
                connectionPurchase.rollback(); // Rollback antes de salir
                return;
            }

            if (totalCompra.compareTo(limiteCredito) > 0) {
                JOptionPane.showMessageDialog(this,
                        String.format("Saldo insuficiente. El total de la compra es $%.2f, pero tu límite de crédito es $%.2f.",
                                totalCompra, limiteCredito),
                        "Saldo Insuficiente", JOptionPane.WARNING_MESSAGE);
                connectionPurchase.rollback(); // Rollback antes de salir
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("¿Desea confirmar la compra de %d unidades de %s por un total de $%.2f?",
                            cantidad, producto.getNombre(), totalCompra),
                    "Confirmar Compra", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                connectionPurchase.rollback(); // Rollback si el usuario cancela
                return;
            }

            // 2. Debitar el límite de crédito
            String sqlUpdateCredit = "UPDATE tarjeta_credito SET limite_credito = limite_credito - ? WHERE usuario_id = ?";
            psUpdateCredit = connectionPurchase.prepareStatement(sqlUpdateCredit);
            psUpdateCredit.setBigDecimal(1, totalCompra);
            psUpdateCredit.setInt(2, currentUserId);
            int rowsAffected = psUpdateCredit.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo debitar el límite de crédito. Es posible que el usuario_id no exista en tarjeta_credito.");
            }
            System.out.println("Límite de crédito actualizado para usuario " + currentUserId + ": debitado $" + totalCompra);

            // 3. Insertar en la tabla de órdenes
            String sqlOrden = "INSERT INTO ordenes (id_usuario, estado, fecha_orden) VALUES (?, ?, CURRENT_TIMESTAMP)";
            psOrden = connectionPurchase.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS);
            psOrden.setInt(1, currentUserId);
            psOrden.setString(2, "pendiente");
            psOrden.executeUpdate();

            rsOrder = psOrden.getGeneratedKeys();
            int idOrden = -1;
            if (rsOrder.next()) {
                idOrden = rsOrder.getInt(1);
            } else {
                throw new SQLException("No se pudo obtener el ID de la orden generada.");
            }
            System.out.println("Orden creada con ID: " + idOrden);

            // 4. Insertar en la tabla de detalle_ordenes
            String sqlDetalle = "INSERT INTO detalle_ordenes (id_orden, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
            psDetalle = connectionPurchase.prepareStatement(sqlDetalle);
            psDetalle.setInt(1, idOrden);
            psDetalle.setInt(2, producto.getId());
            psDetalle.setInt(3, cantidad);
            psDetalle.setBigDecimal(4, producto.getPrecio());
            psDetalle.executeUpdate();
            System.out.println("Detalle de orden añadido para producto " + producto.getId());

            // 5. Actualizar el stock del producto
            String sqlUpdateStock = "UPDATE Productos SET stock = stock - ? WHERE id_producto = ?";
            psUpdateStock = connectionPurchase.prepareStatement(sqlUpdateStock);
            psUpdateStock.setInt(1, cantidad);
            psUpdateStock.setInt(2, producto.getId());
            psUpdateStock.executeUpdate();
            System.out.println("Stock actualizado para producto " + producto.getId() + ". Nuevo stock: " + (producto.getStock() - cantidad));

            connectionPurchase.commit(); // Confirmar la transacción completa
            JOptionPane.showMessageDialog(this, "¡Compra realizada con éxito! Orden #" + idOrden, "Compra Exitosa", JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la UI después de una compra exitosa
            producto.setStock(producto.getStock() - cantidad);
            stockLabel.setText(String.format("<html><b>Stock Disponible:</b> %d unidades</html>", producto.getStock()));
            stockLabel.setForeground(producto.getStock() > 0 ? Color.BLACK : Color.RED);

            SpinnerNumberModel model = (SpinnerNumberModel) cantidadSpinner.getModel();
            model.setMaximum(producto.getStock() > 0 ? producto.getStock() : 1);
            if (producto.getStock() == 0) {
                cantidadSpinner.setValue(0);
                cantidadSpinner.setEnabled(false);
                buyNowButton.setEnabled(false);
                addToCartButton.setEnabled(false);
            } else {
                cantidadSpinner.setValue(1);
            }
            actualizarResumenCompra();

            detallesPanel.revalidate();
            detallesPanel.repaint();

            if (carritoFormInstance != null && carritoFormInstance.isVisible()) {
                carritoFormInstance.loadCarritoItems();
            }

        } catch (SQLException e) {
            try {
                if (connectionPurchase != null && !connectionPurchase.isClosed()) {
                    connectionPurchase.rollback(); // Revertir toda la transacción en caso de error
                }
            } catch (SQLException ex) {
                System.err.println("Error al revertir la transacción de compra: " + ex.getMessage());
            }
            System.err.println("Error SQL al procesar la compra: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al procesar la compra: " + e.getMessage(), "Error de Compra", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rsCredit != null) {
                    rsCredit.close();
                }
                if (rsOrder != null) {
                    rsOrder.close();
                }
                if (psCheckCredit != null) {
                    psCheckCredit.close();
                }
                if (psUpdateCredit != null) {
                    psUpdateCredit.close();
                }
                if (psOrden != null) {
                    psOrden.close();
                }
                if (psDetalle != null) {
                    psDetalle.close();
                }
                if (psUpdateStock != null) {
                    psUpdateStock.close();
                }
                if (connectionPurchase != null && !connectionPurchase.isClosed()) {
                    connectionPurchase.setAutoCommit(true); // Restaurar autocommit
                    conPurchase.desconectar(); // Cerrar la conexión
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de DB (compra): " + e.getMessage());
            }
        }
    }

  
    /**
     * Método para agregar un producto a la lista de deseos del usuario logueado.
     * Verifica si el usuario ha iniciado sesión y si el producto ya existe en su lista de deseos
     * antes de realizar la inserción en la base de datos.
     * @param idProducto El ID del producto a agregar a la lista de deseos.
     */
    private void addProductoToWishlist(int idProducto) {
        // 1. Verificar si el usuario ha iniciado sesión
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para agregar productos a tu lista de deseos.", "No Autenticado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idUsuario = UserSession.getCurrentUserId();
        conexion con = new conexion();
        Connection connection = null;
        PreparedStatement psCheck = null;
        PreparedStatement psInsert = null;
        ResultSet rs = null;

        try {
            connection = con.getConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para agregar a la lista de deseos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Verificar si el producto ya está en la lista de deseos del usuario
            String checkSql = "SELECT COUNT(*) FROM lista_deseos WHERE id_usuario = ? AND id_producto = ?";
            psCheck = connection.prepareStatement(checkSql);
            psCheck.setInt(1, idUsuario);
            psCheck.setInt(2, idProducto);
            rs = psCheck.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // Producto ya existe en la lista de deseos
                JOptionPane.showMessageDialog(this, "Este producto ya está en tu lista de deseos.", "Producto Existente", JOptionPane.INFORMATION_MESSAGE);
                return; // Salir sin insertar
            }

            // 3. Si no existe, proceder a insertar el producto en la lista de deseos
            String insertSql = "INSERT INTO lista_deseos (id_usuario, id_producto) VALUES (?, ?)";
            psInsert = connection.prepareStatement(insertSql);
            psInsert.setInt(1, idUsuario);
            psInsert.setInt(2, idProducto);
            int rowsAffected = psInsert.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Producto agregado a tu lista de deseos exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar el producto a la lista de deseos.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Error SQL al agregar a lista de deseos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al agregar a lista de deseos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // 4. Cerrar recursos de la base de datos
            try {
                if (rs != null) rs.close();
                if (psCheck != null) psCheck.close();
                if (psInsert != null) psInsert.close();
                if (connection != null) con.desconectar(); // Usar el método desconectar de tu clase de conexión
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de DB (lista de deseos): " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Para probar DetallesProductoFrame, necesitas cargar un objeto Producto real desde tu base de datos. \n"
                    + "El código de ejemplo para cargar un un producto (o varios) desde la DB está en MostrarProductosFrame.",
                    "Información de Prueba", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}