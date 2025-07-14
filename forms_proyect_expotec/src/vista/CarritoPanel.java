package vista;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Importar Statement para obtener claves generadas
import java.math.BigDecimal;
import java.time.LocalDateTime; // Para la fecha y hora de la orden
import java.time.format.DateTimeFormatter; // Para formatear la fecha
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.UserSession;
import controlador.conexion;
import modelo.Producto;
import modelo.ItemCarrito;
import util.CartCardActionListener;
import util.FacturadorEmail; // Asegúrate de tener esta clase

public class CarritoPanel extends JPanel implements CartCardActionListener {

    private JPanel cartItemsContainerPanel;
    private JScrollPane scrollPane;
    private JLabel lblTotal;
    private JButton btnCheckout;
    private JButton btnRemoveSelected;
    private JButton btnClearCart;

    private Map<Integer, ProductoCardPanel> cardPanelsMap;

    public CarritoPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        cardPanelsMap = new HashMap<>();

        cartItemsContainerPanel = new JPanel();
        cartItemsContainerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        cartItemsContainerPanel.setBackground(UIManager.getColor("Panel.background"));
        
        scrollPane = new JScrollPane(cartItemsContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(15, 15));

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));

        btnRemoveSelected = new JButton("Eliminar Producto (Clic en 'X')");
        btnRemoveSelected.setFocusPainted(false);
        btnRemoveSelected.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemoveSelected.addActionListener(e -> showIndividualRemoveMessage());
        actionButtonsPanel.add(btnRemoveSelected);

        btnClearCart = new JButton("Vaciar Carrito");
        btnClearCart.setFocusPainted(false);
        btnClearCart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearCart.addActionListener(e -> clearAllProductsFromCart());
        actionButtonsPanel.add(btnClearCart);

        bottomPanel.add(actionButtonsPanel, BorderLayout.WEST);

        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));

        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        checkoutPanel.add(lblTotal);

        btnCheckout = new JButton("Finalizar Compra");
        btnCheckout.setFocusPainted(false);
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckout.addActionListener(e -> finalizePurchase());
        checkoutPanel.add(btnCheckout);

        bottomPanel.add(checkoutPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        refreshCartDisplay();
    }

    // --- Implementación de CartCardActionListener ---

    @Override
    public void onProductoSeleccionado(Producto producto) {
        removeProductFromCart(producto.getId());
    }

    @Override
    public void onQuantityChanged(int productId, int newQuantity) {
        updateProductQuantityInCart(productId, newQuantity);
    }

    // --- Métodos de Gestión del Carrito ---

    /**
     * Refresca la visualización completa del carrito obteniendo los datos de la base de datos.
     */
    public void refreshCartDisplay() {
        cartItemsContainerPanel.removeAll();
        cardPanelsMap.clear();
        BigDecimal totalCarrito = BigDecimal.ZERO;

        if (!UserSession.isLoggedIn()) {
            lblTotal.setText("Total: $0.00");
            btnCheckout.setEnabled(false);
            btnRemoveSelected.setEnabled(false);
            btnClearCart.setEnabled(false);
            cartItemsContainerPanel.revalidate();
            cartItemsContainerPanel.repaint();
            return;
        }

        int userId = UserSession.getCurrentUserId();
        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT p.id_producto, p.nombre, p.precio, p.stock, ci.cantidad, p.imagen " +
                         "FROM carritos c " +
                         "JOIN carrito_items ci ON c.id_carrito = ci.id_carrito " +
                         "JOIN Productos p ON ci.id_producto = p.id_producto " +
                         "WHERE c.id_usuario = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            boolean hasItems = false;
            while (rs.next()) {
                hasItems = true;
                int productId = rs.getInt("id_producto");
                String productName = rs.getString("nombre");
                BigDecimal price = rs.getBigDecimal("precio");
                int quantity = rs.getInt("cantidad");
                int stock = rs.getInt("stock");
                byte[] imagen = rs.getBytes("imagen");
                
                totalCarrito = totalCarrito.add(price.multiply(BigDecimal.valueOf(quantity)));

                // Usamos el constructor Producto(int id, String nombre, BigDecimal precio, int stock, byte[] imagen)
                Producto producto = new Producto(productId, productName, price, stock, imagen);
                ProductoCardPanel card = new ProductoCardPanel(producto, quantity, this);
                cartItemsContainerPanel.add(card);
                cardPanelsMap.put(productId, card);
            }

            lblTotal.setText("Total: $" + totalCarrito.setScale(2, BigDecimal.ROUND_HALF_UP));

            btnCheckout.setEnabled(hasItems);
            btnRemoveSelected.setEnabled(hasItems);
            btnClearCart.setEnabled(hasItems);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
        cartItemsContainerPanel.revalidate();
        cartItemsContainerPanel.repaint();
    }

    /**
     * Muestra un mensaje al usuario para indicar cómo eliminar productos.
     * Este método se asocia al botón "Eliminar Producto (Clic en 'X')".
     */
    private void showIndividualRemoveMessage() {
        JOptionPane.showMessageDialog(this,
            "Para eliminar un producto específico, haz clic en el botón 'X' dentro de la tarjeta del producto.",
            "Eliminar Producto",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Actualiza la cantidad de un producto en el carrito y en la base de datos.
     * Gestiona el stock y los subtotales.
     * @param productId El ID del producto a actualizar.
     * @param newQuantity La nueva cantidad deseada.
     */
    private void updateProductQuantityInCart(int productId, int newQuantity) {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para modificar el carrito.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay();
            return;
        }

        // Si la nueva cantidad es 0 o menos, preguntar si desea eliminar el producto.
        if (newQuantity <= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "La cantidad es cero. ¿Desea eliminar este producto del carrito?",
                    "Eliminar Producto", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeProductFromCart(productId);
            } else {
                // Si el usuario cancela, restablecer la cantidad a la cantidad actual en el carrito
                ProductoCardPanel card = cardPanelsMap.get(productId);
                if (card != null) {
                    // Primero, necesitamos obtener la cantidad actual de la BD para restablecer correctamente
                    Connection con = null;
                    try {
                        con = new conexion().getConnection();
                        String selectQtySql = "SELECT ci.cantidad FROM Carrito_Items ci JOIN Carritos c ON ci.id_carrito = c.id_carrito WHERE ci.id_producto = ? AND c.id_usuario = ?";
                        PreparedStatement ps = con.prepareStatement(selectQtySql);
                        ps.setInt(1, productId);
                        ps.setInt(2, UserSession.getCurrentUserId());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            card.updateQuantityDisplay(rs.getInt("cantidad"));
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } finally {
                        try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); }
                    }
                }
                refreshCartDisplay(); // Para asegurar que el total se recalcule correctamente
            }
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Obtener el stock actual del producto y la cantidad actual en el carrito
            String selectSql = "SELECT P.stock, CI.cantidad FROM Productos P " +
                               "JOIN Carrito_Items CI ON P.id_producto = CI.id_producto " +
                               "JOIN Carritos C ON CI.id_carrito = C.id_carrito " +
                               "WHERE P.id_producto = ? AND C.id_usuario = ?";
            PreparedStatement selectPs = con.prepareStatement(selectSql);
            selectPs.setInt(1, productId);
            selectPs.setInt(2, UserSession.getCurrentUserId());
            ResultSet rs = selectPs.executeQuery();

            int currentStock = 0;
            int currentCartQuantity = 0;
            if (rs.next()) {
                currentStock = rs.getInt("stock");
                currentCartQuantity = rs.getInt("cantidad");
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado en el carrito o stock no disponible.", "Error", JOptionPane.ERROR_MESSAGE);
                con.rollback(); // Deshacer si no se encuentra
                return;
            }
            rs.close();
            selectPs.close();

            int stockChange = newQuantity - currentCartQuantity; // Cantidad añadida o eliminada
            int updatedStock = currentStock - stockChange;

            if (updatedStock < 0) {
                JOptionPane.showMessageDialog(this, "No hay suficiente stock para la cantidad solicitada. Stock disponible: " + currentStock, "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                con.rollback(); // Deshacer
                // Restablecer la cantidad en el spinner a la cantidad actual en el carrito
                ProductoCardPanel card = cardPanelsMap.get(productId);
                if (card != null) {
                    card.updateQuantityDisplay(currentCartQuantity);
                }
                return;
            }

            // 2. Actualizar la cantidad en carrito_items
            String updateCartSql = "UPDATE Carrito_Items SET cantidad = ? WHERE id_carrito = (SELECT id_carrito FROM Carritos WHERE id_usuario = ?) AND id_producto = ?";
            PreparedStatement updateCartPs = con.prepareStatement(updateCartSql);
            updateCartPs.setInt(1, newQuantity);
            updateCartPs.setInt(2, UserSession.getCurrentUserId());
            updateCartPs.setInt(3, productId);
            updateCartPs.executeUpdate();
            updateCartPs.close();

            // 3. Actualizar el stock en Productos
            String updateProductStockSql = "UPDATE Productos SET stock = ? WHERE id_producto = ?";
            PreparedStatement updateProductStockPs = con.prepareStatement(updateProductStockSql);
            updateProductStockPs.setInt(1, updatedStock);
            updateProductStockPs.setInt(2, productId);
            updateProductStockPs.executeUpdate();
            updateProductStockPs.close();

            con.commit(); // Confirmar la transacción
            refreshCartDisplay(); // Refrescar para reflejar los cambios y el total

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback(); // Deshacer si hay error
            } catch (SQLException rbEx) {
                rbEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error al actualizar la cantidad: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.setAutoCommit(true); // Restaurar auto-commit
                if (con != null) con.close();
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
    }

    /**
     * Elimina un producto del carrito y actualiza el stock en la base de datos.
     * @param productId El ID del producto a eliminar.
     */
    private void removeProductFromCart(int productId) {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para modificar el carrito.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar este producto del carrito?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return; // El usuario canceló la eliminación
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Obtener la cantidad de producto en el carrito y el stock actual
            String selectSql = "SELECT P.stock, CI.cantidad FROM Productos P " +
                               "JOIN Carrito_Items CI ON P.id_producto = CI.id_producto " +
                               "JOIN Carritos C ON CI.id_carrito = C.id_carrito " +
                               "WHERE P.id_producto = ? AND C.id_usuario = ?";
            PreparedStatement selectPs = con.prepareStatement(selectSql);
            selectPs.setInt(1, productId);
            selectPs.setInt(2, UserSession.getCurrentUserId());
            ResultSet rs = selectPs.executeQuery();

            int currentStock = 0;
            int quantityToRemove = 0;
            if (rs.next()) {
                currentStock = rs.getInt("stock");
                quantityToRemove = rs.getInt("cantidad");
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado en el carrito.", "Error", JOptionPane.ERROR_MESSAGE);
                con.rollback();
                return;
            }
            rs.close();
            selectPs.close();

            // 2. Eliminar el item del carrito
            String deleteSql = "DELETE FROM Carrito_Items WHERE id_carrito = (SELECT id_carrito FROM Carritos WHERE id_usuario = ?) AND id_producto = ?";
            PreparedStatement deletePs = con.prepareStatement(deleteSql);
            deletePs.setInt(1, UserSession.getCurrentUserId());
            deletePs.setInt(2, productId);
            deletePs.executeUpdate();
            deletePs.close();

            // 3. Devolver el stock a la tabla Productos
            String updateStockSql = "UPDATE Productos SET stock = ? WHERE id_producto = ?";
            PreparedStatement updateStockPs = con.prepareStatement(updateStockSql);
            updateStockPs.setInt(1, currentStock + quantityToRemove);
            updateStockPs.setInt(2, productId);
            updateStockPs.executeUpdate();
            updateStockPs.close();

            con.commit(); // Confirmar la transacción
            JOptionPane.showMessageDialog(this, "Producto eliminado del carrito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay(); // Refrescar la visualización del carrito

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rbEx) {
                rbEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error al eliminar el producto: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
    }

    /**
     * Vacía completamente el carrito del usuario y devuelve el stock a los productos.
     */
    private void clearAllProductsFromCart() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para vaciar el carrito.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea vaciar todo el carrito?",
                "Confirmar Vaciado", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return; // El usuario canceló
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false); // Iniciar transacción

            // 1. Obtener todos los ítems del carrito para devolver el stock
            String selectItemsSql = "SELECT ci.id_producto, ci.cantidad, p.stock FROM carrito_items ci " +
                                    "JOIN carritos c ON ci.id_carrito = c.id_carrito " +
                                    "JOIN Productos p ON ci.id_producto = p.id_producto " +
                                    "WHERE c.id_usuario = ?";
            PreparedStatement selectItemsPs = con.prepareStatement(selectItemsSql);
            selectItemsPs.setInt(1, UserSession.getCurrentUserId());
            ResultSet rs = selectItemsPs.executeQuery();

            List<ItemCarrito> itemsToReturnStock = new ArrayList<>();
            while (rs.next()) {
                // Producto en este contexto solo necesita id, nombre y precio para el ItemCarrito.
                // El stock lo obtenemos de la tabla Productos al momento.
                Producto p = new Producto(rs.getInt("id_producto"), "", BigDecimal.ZERO); // Constructor simple
                itemsToReturnStock.add(new ItemCarrito(p, rs.getInt("cantidad")));
            }
            rs.close();
            selectItemsPs.close();

            // 2. Eliminar todos los ítems del carrito
            String deleteItemsSql = "DELETE FROM carrito_items WHERE id_carrito = (SELECT id_carrito FROM Carritos WHERE id_usuario = ?)";
            PreparedStatement deleteItemsPs = con.prepareStatement(deleteItemsSql);
            deleteItemsPs.setInt(1, UserSession.getCurrentUserId());
            deleteItemsPs.executeUpdate();
            deleteItemsPs.close();

            // 3. Actualizar el stock de cada producto
            String updateStockSql = "UPDATE Productos SET stock = stock + ? WHERE id_producto = ?";
            PreparedStatement updateStockPs = con.prepareStatement(updateStockSql);
            for (ItemCarrito item : itemsToReturnStock) {
                updateStockPs.setInt(1, item.getCantidad());
                updateStockPs.setInt(2, item.getProducto().getId());
                updateStockPs.addBatch(); // Añadir a un batch para ejecución más eficiente
            }
            updateStockPs.executeBatch(); // Ejecutar todas las actualizaciones de stock
            updateStockPs.close();

            con.commit(); // Confirmar la transacción
            JOptionPane.showMessageDialog(this, "El carrito ha sido vaciado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay(); // Refrescar la visualización del carrito

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rbEx) {
                rbEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Error al vaciar el carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
                if (con != null) con.close();
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
    }

    /**
     * Procesa la finalización de la compra, registra la orden y envía un correo.
     */
   private void finalizePurchase() {
    if (!UserSession.isLoggedIn()) {
        JOptionPane.showMessageDialog(this, "Debe iniciar sesión para finalizar la compra.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    BigDecimal currentTotal = new BigDecimal(lblTotal.getText().replace("Total: $", ""));
    if (currentTotal.compareTo(BigDecimal.ZERO) <= 0) {
        JOptionPane.showMessageDialog(this, "Su carrito está vacío. Agregue productos antes de finalizar la compra.", "Carrito Vacío", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // --- PASO 1: CONFIRMAR COMPRA ---
    int confirm = JOptionPane.showConfirmDialog(this,
            "El total de su compra es: $" + currentTotal.setScale(2, BigDecimal.ROUND_HALF_UP) + "\n¿Desea finalizar la compra?",
            "Confirmar Compra", JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) {
        return; // El usuario canceló la compra
    }

    // --- PASO 2: SOLICITAR LA DIRECCIÓN DE ENVÍO AL USUARIO ---
    String userAddress = JOptionPane.showInputDialog(this,
            "Por favor, ingrese la dirección de envío:",
            "Dirección de Envío",
            JOptionPane.QUESTION_MESSAGE);

    if (userAddress == null || userAddress.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "La dirección de envío es obligatoria para finalizar la compra.", "Dirección Requerida", JOptionPane.WARNING_MESSAGE);
        return; // El usuario canceló o no ingresó dirección
    }

    Connection con = null;
    try {
        con = new conexion().getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }
        con.setAutoCommit(false); // Iniciar transacción

        int userId = UserSession.getCurrentUserId();
        String userEmail = UserSession.getCurrentUserEmail();
        String userName = UserSession.getCurrentUserName();

        // 1. Obtener los detalles de los productos del carrito para la orden y el correo
        List<ItemCarrito> itemsComprados = new ArrayList<>();
        String selectItemsSql = "SELECT p.id_producto, p.nombre, p.precio, ci.cantidad " +
                                "FROM carritos c JOIN carrito_items ci ON c.id_carrito = ci.id_carrito " +
                                "JOIN Productos p ON ci.id_producto = p.id_producto " +
                                "WHERE c.id_usuario = ?";
        PreparedStatement selectItemsPs = con.prepareStatement(selectItemsSql);
        selectItemsPs.setInt(1, userId);
        ResultSet rs = selectItemsPs.executeQuery();

        if (!rs.isBeforeFirst()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío. No se puede finalizar la compra.", "Carrito Vacío", JOptionPane.WARNING_MESSAGE);
            con.rollback();
            return;
        }

        while (rs.next()) {
            Producto p = new Producto(rs.getInt("id_producto"), rs.getString("nombre"), rs.getBigDecimal("precio"));
            itemsComprados.add(new ItemCarrito(p, rs.getInt("cantidad")));
        }
        rs.close();
        selectItemsPs.close();

        // 2. Insertar la orden en la tabla 'ordenes'
        String insertOrderSql = "INSERT INTO Ordenes (id_usuario, estado, fecha_orden, total_orden, direccion_envio) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertOrderPs = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
        insertOrderPs.setInt(1, userId);
        insertOrderPs.setString(2, "pendiente");
        insertOrderPs.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
        insertOrderPs.setBigDecimal(4, currentTotal);
        insertOrderPs.setString(5, userAddress);
        insertOrderPs.executeUpdate();

        ResultSet generatedKeys = insertOrderPs.getGeneratedKeys();
        int idOrden = -1;
        if (generatedKeys.next()) {
            idOrden = generatedKeys.getInt(1);
        } else {
            throw new SQLException("Error al obtener el ID de la orden generada.");
        }
        generatedKeys.close();
        insertOrderPs.close();

        // 3. Insertar los detalles de la orden en 'detalle_ordenes'
        String insertDetalleSql = "INSERT INTO Detalle_Ordenes (id_orden, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        PreparedStatement insertDetallePs = con.prepareStatement(insertDetalleSql);
        for (ItemCarrito item : itemsComprados) {
            insertDetallePs.setInt(1, idOrden);
            insertDetallePs.setInt(2, item.getProducto().getId());
            insertDetallePs.setInt(3, item.getCantidad());
            insertDetallePs.setBigDecimal(4, item.getProducto().getPrecio());
            insertDetallePs.addBatch();
        }
        insertDetallePs.executeBatch();
        insertDetallePs.close();

        // --- INICIO DEL CÓDIGO AÑADIDO PARA EL HISTORIAL DE ENVÍO ---
        String insertHistorialEnvioSql = "INSERT INTO historial_envio_orden (id_orden, estado_orden, ubicacion_actual, fecha_hora_evento, notas) VALUES (?, ?, ?, NOW(), ?)";
        PreparedStatement insertHistorialEnvioPs = con.prepareStatement(insertHistorialEnvioSql);
        insertHistorialEnvioPs.setInt(1, idOrden);
        insertHistorialEnvioPs.setString(2, "Orden Confirmada"); // Estado inicial
        insertHistorialEnvioPs.setString(3, "Almacén Principal, Guatemala"); // Ubicación inicial
        insertHistorialEnvioPs.setString(4, "Su pedido ha sido recibido y está siendo procesado."); // Nota inicial
        insertHistorialEnvioPs.executeUpdate();
        insertHistorialEnvioPs.close();
        System.out.println("Primer registro de historial de envío guardado para la orden ID: " + idOrden);
        // --- FIN DEL CÓDIGO AÑADIDO ---

        // 4. Vaciar el carrito
        String deleteCartItemsSql = "DELETE FROM carrito_items WHERE id_carrito = (SELECT id_carrito FROM Carritos WHERE id_usuario = ?)";
        PreparedStatement deleteCartItemsPs = con.prepareStatement(deleteCartItemsSql);
        deleteCartItemsPs.setInt(1, userId);
        deleteCartItemsPs.executeUpdate();
        deleteCartItemsPs.close();

        con.commit();

        JOptionPane.showMessageDialog(this, "¡Compra finalizada con éxito! Su orden #" + idOrden + " ha sido registrada.", "Compra Exitosa", JOptionPane.INFORMATION_MESSAGE);

        // 5. Enviar el recibo/factura por correo electrónico
        if (userEmail != null && !userEmail.isEmpty()) {
            boolean emailSent = FacturadorEmail.enviarFacturaPorCorreo(
                                userEmail,
                                userName,
                                String.valueOf(idOrden),
                                itemsComprados,
                                currentTotal.doubleValue()
                            );
            if (emailSent) {
                JOptionPane.showMessageDialog(this, "Se ha enviado un recibo/factura de su orden a " + userEmail, "Recibo Enviado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al enviar el correo con el recibo. Por favor, revise la consola para más detalles.", "Error de Envío de Correo", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró un correo electrónico para enviar el recibo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }

        refreshCartDisplay();

    } catch (SQLException ex) {
        try {
            if (con != null) con.rollback();
        } catch (SQLException rbEx) {
            rbEx.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, "Error al finalizar la compra: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al procesar la compra: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    } finally {
        try {
            if (con != null) con.setAutoCommit(true);
            if (con != null) con.close();
        } catch (SQLException finalEx) {
            System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
        }
    }
}

    @Override
    public void onProductoRemovidoDeListaDeseos(Producto producto) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void volverAlCatalogo() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}