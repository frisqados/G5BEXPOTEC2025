package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import util.UserSession;
import controlador.conexion;
import modelo.Producto;
import modelo.ItemCarrito;
import util.FacturadorEmail;

public class CarritoPanel extends JPanel {

    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private JButton btnCheckout;
    private JButton btnRemoveSelected;
    private JButton btnClearCart;

    public CarritoPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(230, 235, 240)); // Light Grayish Blue
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String[] columnNames = {"ID Producto", "Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class;
                if (columnIndex == 3 || columnIndex == 4) return BigDecimal.class;
                return super.getColumnClass(columnIndex);
            }
        };

        cartTable = new JTable(tableModel);
        cartTable.setFillsViewportHeight(true);
        cartTable.setRowHeight(35);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        cartTable.getTableHeader().setBackground(new Color(60, 141, 188)); // Dodger Blue
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.setSelectionBackground(new Color(173, 216, 230)); // Light Blue
        cartTable.setSelectionForeground(Color.BLACK);
        cartTable.setGridColor(new Color(200, 200, 200));
        cartTable.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(15, 15));
        bottomPanel.setBackground(getBackground());

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        actionButtonsPanel.setBackground(getBackground());

        btnRemoveSelected = new JButton("Eliminar Producto");
        btnRemoveSelected.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRemoveSelected.setBackground(new Color(105, 105, 105)); // Dim Gray
        btnRemoveSelected.setForeground(Color.BLACK); // White text for contrast
        btnRemoveSelected.setFocusPainted(false);
        btnRemoveSelected.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRemoveSelected.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnRemoveSelected.addActionListener(e -> removeSelectedProduct());
        actionButtonsPanel.add(btnRemoveSelected);

        btnClearCart = new JButton("Vaciar Carrito");
        btnClearCart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClearCart.setBackground(new Color(169, 169, 169)); // Dark Gray
        btnClearCart.setForeground(Color.BLACK); // Black text for contrast
        btnClearCart.setFocusPainted(false);
        btnClearCart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearCart.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnClearCart.addActionListener(e -> clearAllProductsFromCart());
        actionButtonsPanel.add(btnClearCart);

        bottomPanel.add(actionButtonsPanel, BorderLayout.WEST);

        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        checkoutPanel.setBackground(getBackground());

        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotal.setForeground(new Color(169, 169, 169)); // Dark Green (kept this for emphasis on total)
        checkoutPanel.add(lblTotal);

        btnCheckout = new JButton("Finalizar Compra");
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnCheckout.setBackground(new Color(169, 169, 169)); // Olive Drab (a subtle green-gray for "go")
        btnCheckout.setForeground(Color.BLACK); // White text for contrast
        btnCheckout.setFocusPainted(false);
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCheckout.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btnCheckout.addActionListener(e -> finalizePurchase());
        checkoutPanel.add(btnCheckout);

        bottomPanel.add(checkoutPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                    int row = e.getFirstRow();
                    if (row >= 0 && row < tableModel.getRowCount()) {
                        int productId = (int) tableModel.getValueAt(row, 0);
                        Object quantityValue = tableModel.getValueAt(row, 2);
                        int newQuantity;
                        try {
                            newQuantity = Integer.parseInt(quantityValue.toString());
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(CarritoPanel.this, "Cantidad inválida. Ingrese un número entero.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
                            refreshCartDisplay();
                            return;
                        }
                        updateProductQuantityInCart(productId, newQuantity, row);
                    }
                }
            }
        });

        refreshCartDisplay();
    }

    public void refreshCartDisplay() {
        tableModel.setRowCount(0);
        BigDecimal totalCarrito = BigDecimal.ZERO;

        if (!UserSession.isLoggedIn()) {
            lblTotal.setText("Total: $0.00");
            btnCheckout.setEnabled(false);
            btnRemoveSelected.setEnabled(false);
            btnClearCart.setEnabled(false);
            return;
        }

        int userId = UserSession.getCurrentUserId();
        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT p.id_producto, p.nombre, p.precio, ci.cantidad " +
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
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                totalCarrito = totalCarrito.add(subtotal);

                tableModel.addRow(new Object[]{productId, productName, quantity, price, subtotal});
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
    }

    private void updateProductQuantityInCart(int productId, int newQuantity, int row) {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para modificar el carrito.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay();
            return;
        }

        if (newQuantity <= 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "La cantidad es cero. ¿Desea eliminar este producto del carrito?",
                    "Eliminar Producto", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeProductFromCart(productId);
            } else {
                refreshCartDisplay();
            }
            return;
        }

        int userId = UserSession.getCurrentUserId();
        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false);

            String getOldQuantitySql = "SELECT ci.cantidad " +
                                       "FROM carritos c JOIN carrito_items ci ON c.id_carrito = ci.id_carrito " +
                                       "WHERE c.id_usuario = ? AND ci.id_producto = ?";
            PreparedStatement getOldQuantityPs = con.prepareStatement(getOldQuantitySql);
            getOldQuantityPs.setInt(1, userId);
            getOldQuantityPs.setInt(2, productId);
            ResultSet rsOldQty = getOldQuantityPs.executeQuery();
            int oldQuantityInCart = 0;
            if (rsOldQty.next()) {
                oldQuantityInCart = rsOldQty.getInt("cantidad");
            } else {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Producto no encontrado en el carrito.", "Error", JOptionPane.ERROR_MESSAGE);
                refreshCartDisplay();
                return;
            }

            int currentStockInDB = getProductStockFromDB(productId, con);

            if (newQuantity > (currentStockInDB + oldQuantityInCart)) {
                JOptionPane.showMessageDialog(this,
                        "Stock insuficiente. Solo hay " + (currentStockInDB + oldQuantityInCart) + " unidades disponibles para " + tableModel.getValueAt(row, 1) + ".",
                        "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                con.rollback();
                refreshCartDisplay();
                return;
            }

            int stockChange = oldQuantityInCart - newQuantity;

            String updateCartItemSql = "UPDATE carrito_items SET cantidad = ? " +
                                       "FROM carritos c " +
                                       "WHERE carrito_items.id_carrito = c.id_carrito AND c.id_usuario = ? AND carrito_items.id_producto = ?";
            PreparedStatement updateCartItemPs = con.prepareStatement(updateCartItemSql);
            updateCartItemPs.setInt(1, newQuantity);
            updateCartItemPs.setInt(2, userId);
            updateCartItemPs.setInt(3, productId);
            updateCartItemPs.executeUpdate();

            String updateProductStockSql = "UPDATE Productos SET stock = stock + ? WHERE id_producto = ?";
            PreparedStatement updateProductStockPs = con.prepareStatement(updateProductStockSql);
            updateProductStockPs.setInt(1, stockChange);
            updateProductStockPs.setInt(2, productId);
            updateProductStockPs.executeUpdate();

            con.commit();
            refreshCartDisplay();

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error al realizar rollback: " + rollbackEx.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error al actualizar la cantidad en el carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            refreshCartDisplay();
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

    private void removeSelectedProduct() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto para eliminar.", "Ningún Producto Seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar '" + productName + "' del carrito?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            removeProductFromCart(productId);
        }
    }

    private void clearAllProductsFromCart() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para vaciar el carrito.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito ya está vacío.", "Carrito Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea vaciar todo el carrito?",
                "Confirmar Vaciado", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int userId = UserSession.getCurrentUserId();
            Connection con = null;
            try {
                con = new conexion().getConnection();
                if (con == null) {
                    JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                con.setAutoCommit(false);

                String getItemsSql = "SELECT ci.id_producto, ci.cantidad " +
                                     "FROM carritos c JOIN carrito_items ci ON c.id_carrito = ci.id_carrito " +
                                     "WHERE c.id_usuario = ?";
                PreparedStatement getItemsPs = con.prepareStatement(getItemsSql);
                getItemsPs.setInt(1, userId);
                ResultSet rs = getItemsPs.executeQuery();

                while (rs.next()) {
                    int productId = rs.getInt("id_producto");
                    int quantity = rs.getInt("cantidad");
                    String updateStockSql = "UPDATE Productos SET stock = stock + ? WHERE id_producto = ?";
                    PreparedStatement updateStockPs = con.prepareStatement(updateStockSql);
                    updateStockPs.setInt(1, quantity);
                    updateStockPs.setInt(2, productId);
                    updateStockPs.executeUpdate();
                }

                String deleteItemsSql = "DELETE FROM carrito_items " +
                                        "FROM carritos c " +
                                        "WHERE carrito_items.id_carrito = c.id_carrito AND c.id_usuario = ?";
                PreparedStatement deleteItemsPs = con.prepareStatement(deleteItemsSql);
                deleteItemsPs.setInt(1, userId);
                deleteItemsPs.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this, "El carrito ha sido vaciado.", "Carrito Vaciado", JOptionPane.INFORMATION_MESSAGE);
                refreshCartDisplay();

            } catch (SQLException ex) {
                try {
                    if (con != null) con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al realizar rollback al vaciar carrito: " + rollbackEx.getMessage());
                }
                JOptionPane.showMessageDialog(this, "Error al vaciar el carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
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
    }

    private void removeProductFromCart(int productId) {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para modificar el carrito.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int userId = UserSession.getCurrentUserId();
        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            con.setAutoCommit(false);

            String getQuantitySql = "SELECT ci.cantidad " +
                                    "FROM carritos c JOIN carrito_items ci ON c.id_carrito = ci.id_carrito " +
                                    "WHERE c.id_usuario = ? AND ci.id_producto = ?";
            PreparedStatement getQuantityPs = con.prepareStatement(getQuantitySql);
            getQuantityPs.setInt(1, userId);
            getQuantityPs.setInt(2, productId);
            ResultSet rs = getQuantityPs.executeQuery();
            int quantityToRemove = 0;
            if (rs.next()) {
                quantityToRemove = rs.getInt("cantidad");
            } else {
                con.rollback();
                JOptionPane.showMessageDialog(this, "Producto no encontrado en el carrito para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
                refreshCartDisplay();
                return;
            }

            String deleteItemSql = "DELETE FROM carrito_items " +
                                   "FROM carritos c " +
                                   "WHERE carrito_items.id_carrito = c.id_carrito AND c.id_usuario = ? AND carrito_items.id_producto = ?";
            PreparedStatement deleteItemPs = con.prepareStatement(deleteItemSql);
            deleteItemPs.setInt(1, userId);
            deleteItemPs.setInt(2, productId);
            deleteItemPs.executeUpdate();

            String updateStockSql = "UPDATE Productos SET stock = stock + ? WHERE id_producto = ?";
            PreparedStatement updateStockPs = con.prepareStatement(updateStockSql);
            updateStockPs.setInt(1, quantityToRemove);
            updateStockPs.setInt(2, productId);
            updateStockPs.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Producto eliminado del carrito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            refreshCartDisplay();

        } catch (SQLException ex) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error al realizar rollback: " + rollbackEx.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error al eliminar producto del carrito: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            refreshCartDisplay();
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

    private void finalizePurchase() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para finalizar la compra.", "No Autenticado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío. Añada productos antes de finalizar la compra.", "Carrito Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirmar la compra por un total de " + lblTotal.getText().replace("Total: ", "") + "?",
                "Confirmar Compra", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int userId = UserSession.getCurrentUserId();
            String userName = UserSession.getCurrentUserName();
            String userEmail = UserSession.getCurrentUserEmail();

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
                getCarritoPs.setInt(1, userId);
                ResultSet rsCarrito = getCarritoPs.executeQuery();
                if (rsCarrito.next()) {
                    idCarrito = rsCarrito.getInt("id_carrito");
                } else {
                    throw new SQLException("No se encontró el carrito para el usuario.");
                }

                String insertOrderSql = "INSERT INTO Ordenes (id_usuario, fecha_orden, total_orden, estado) VALUES (?, NOW(), ?, 'pendiente')";
                PreparedStatement insertOrderPs = con.prepareStatement(insertOrderSql, PreparedStatement.RETURN_GENERATED_KEYS);
                insertOrderPs.setInt(1, userId);
                BigDecimal totalCompra = new BigDecimal(lblTotal.getText().replace("Total: $", ""));
                insertOrderPs.setBigDecimal(2, totalCompra);
                insertOrderPs.executeUpdate();

                ResultSet generatedKeys = insertOrderPs.getGeneratedKeys();
                int idPedido = -1;
                if (generatedKeys.next()) {
                    idPedido = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Error al obtener ID del pedido generado.");
                }

                String getCartItemsSql = "SELECT id_producto, cantidad FROM carrito_items WHERE id_carrito = ?";
                PreparedStatement getCartItemsPs = con.prepareStatement(getCartItemsSql);
                getCartItemsPs.setInt(1, idCarrito);
                ResultSet cartItemsRs = getCartItemsPs.executeQuery();

                String insertOrderItemSql = "INSERT INTO Detalle_Ordenes (id_orden, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                PreparedStatement insertOrderItemPs = con.prepareStatement(insertOrderItemSql);

                List<ItemCarrito> itemsCompradosParaFactura = new ArrayList<>();

                while (cartItemsRs.next()) {
                    int productId = cartItemsRs.getInt("id_producto");
                    int quantity = cartItemsRs.getInt("cantidad");

                    String getProductPriceSql = "SELECT nombre, precio, stock FROM Productos WHERE id_producto = ?";
                    PreparedStatement getProductPricePs = con.prepareStatement(getProductPriceSql);
                    getProductPricePs.setInt(1, productId);
                    ResultSet productRs = getProductPricePs.executeQuery();
                    if (productRs.next()) {
                        String productName = productRs.getString("nombre");
                        BigDecimal unitPrice = productRs.getBigDecimal("precio");
                        int currentStock = productRs.getInt("stock");

                        if (quantity > currentStock) {
                            con.rollback();
                            JOptionPane.showMessageDialog(this,
                                    "Stock insuficiente para " + productName + ". Solo quedan " + currentStock + " unidades. Compra cancelada.",
                                    "Error de Stock", JOptionPane.ERROR_MESSAGE);
                            refreshCartDisplay();
                            return;
                        }

                        insertOrderItemPs.setInt(1, idPedido);
                        insertOrderItemPs.setInt(2, productId);
                        insertOrderItemPs.setInt(3, quantity);
                        insertOrderItemPs.setBigDecimal(4, unitPrice);
                        insertOrderItemPs.executeUpdate();

                        itemsCompradosParaFactura.add(new ItemCarrito(new Producto(productId, productName, unitPrice, currentStock), quantity));

                    } else {
                        con.rollback();
                        JOptionPane.showMessageDialog(this, "Producto " + productId + " no encontrado durante la finalización de compra.", "Error Interno", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                String deleteCartItemsSql = "DELETE FROM carrito_items WHERE id_carrito = ?";
                PreparedStatement deleteCartItemsPs = con.prepareStatement(deleteCartItemsSql);
                deleteCartItemsPs.setInt(1, idCarrito);
                deleteCartItemsPs.executeUpdate();

                con.commit();
                JOptionPane.showMessageDialog(this, "¡Compra finalizada con éxito! Su número de pedido es: " + idPedido, "Compra Exitosa", JOptionPane.INFORMATION_MESSAGE);
                refreshCartDisplay();

                if (userEmail != null && !userEmail.isEmpty() && !itemsCompradosParaFactura.isEmpty()) {
                    boolean emailSent = FacturadorEmail.enviarFacturaPorCorreo(
                        userEmail,
                        userName,
                        String.valueOf(idPedido),
                        itemsCompradosParaFactura,
                        totalCompra.doubleValue()
                    );

                    if (emailSent) {
                        JOptionPane.showMessageDialog(this, "Factura enviada a " + userEmail + ".", "Correo Enviado", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo enviar la factura por correo electrónico. Verifique la configuración.", "Error de Correo", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo enviar la factura por correo electrónico. Correo del usuario no disponible o carrito vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }

            } catch (SQLException ex) {
                try {
                    if (con != null) con.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al realizar rollback en finalizar compra: " + rollbackEx.getMessage());
                }
                JOptionPane.showMessageDialog(this, "Error al finalizar la compra: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
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
    }

    private int getProductStockFromDB(int productId, Connection con) {
        int stock = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT stock FROM Productos WHERE id_producto = ?";
            ps = con.prepareStatement(sql);
            ps.setInt(1, productId);
            rs = ps.executeQuery();
            if (rs.next()) {
                stock = rs.getInt("stock");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener stock del producto " + productId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
        }
        return stock;
    }

    private int getRowForProductId(int productId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((int) tableModel.getValueAt(i, 0) == productId) {
                return i;
            }
        }
        return -1;
    }
}