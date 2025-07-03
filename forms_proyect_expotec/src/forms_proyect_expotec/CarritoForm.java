package forms_proyect_expotec;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.math.BigDecimal;

import controlador.conexion;
import util.UserSession;

public class CarritoForm extends JFrame {

    private JTable carritoTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    public CarritoForm() {
        // Inicializa los componentes de la interfaz de usuario
        initComponents();
        // Carga los ítems del carrito desde la base de datos
        loadCarritoItems();
    }

    private void initComponents() {
        // Configura el título de la ventana
        setTitle("Mi Carrito de Compras");
        // Configura el tamaño de la ventana
        setSize(1200, 700);
        // Define la operación por defecto al cerrar la ventana
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Centra la ventana en la pantalla
        setLocationRelativeTo(null);

        // Define colores para la interfaz de usuario
        Color darkBlue = new Color(34, 70, 113);
        Color lightGrey = new Color(240, 240, 240);
        Color mediumGrey = new Color(220, 220, 220);
        Color darkText = Color.BLACK;
        Color headerText = Color.WHITE;

        // Panel principal que contendrá todos los demás paneles
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightGrey);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de encabezado para el título del carrito
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(darkBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Etiqueta del título del carrito
        JLabel titleLabel = new JLabel("Mi Carrito de Compras");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(headerText);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Añade el panel de encabezado al panel principal
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Nombres de las columnas de la tabla del carrito
        String[] columnNames = {"ID Producto", "Nombre Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        // Modelo de tabla para el carrito, no editable
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas de la tabla no son editables
            }
        };
        // Crea la tabla del carrito con el modelo definido
        carritoTable = new JTable(tableModel);

        // Configuración de la fuente y apariencia de la tabla
        carritoTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        carritoTable.setRowHeight(35);
        carritoTable.setGridColor(mediumGrey);
        carritoTable.setShowVerticalLines(false);
        carritoTable.setFillsViewportHeight(true);

        // Configuración de la cabecera de la tabla
        carritoTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        carritoTable.getTableHeader().setBackground(darkBlue);
        carritoTable.getTableHeader().setForeground(headerText);
        carritoTable.getTableHeader().setReorderingAllowed(false);
        carritoTable.getTableHeader().setResizingAllowed(true);

        // Centra el texto en todas las celdas de la tabla
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        for (int i = 0; i < carritoTable.getColumnCount(); i++) {
            carritoTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Crea un panel de desplazamiento para la tabla del carrito
        JScrollPane scrollPane = new JScrollPane(carritoTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(mediumGrey, 1));

        // Añade el panel de desplazamiento al centro del panel principal
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior para el resumen del total y los botones de acción
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(lightGrey);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Panel para mostrar el total del carrito
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mediumGrey, 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Etiqueta de texto para "Total del Carrito"
        JLabel totalTextLabel = new JLabel("Total del Carrito: ");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTextLabel.setForeground(darkText);

        // Etiqueta para mostrar el valor total del carrito
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(darkBlue);

        // Añade las etiquetas al panel de resumen
        summaryPanel.add(totalTextLabel);
        summaryPanel.add(totalLabel);

        // Añade el panel de resumen a la parte superior del panel inferior
        bottomPanel.add(summaryPanel, BorderLayout.NORTH);

        // Panel para los botones de acción
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actionButtonPanel.setBackground(lightGrey);

        // Botón "Continuar Comprando"
        JButton continueShoppingButton = new JButton("Continuar Comprando");
        styleButton(continueShoppingButton, darkBlue, headerText);
        continueShoppingButton.addActionListener(e -> {
            this.dispose(); // Cierra la ventana del carrito
        });

        // Botón "Proceder al Pago"
        JButton checkoutButton = new JButton("Proceder al Pago");
        styleButton(checkoutButton, darkBlue, headerText);
        checkoutButton.addActionListener(e -> {
            // Verifica si el carrito está vacío antes de proceder al pago
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Tu carrito está vacío. No puedes proceder al pago.", "Carrito Vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Procesa el pago del carrito
            processCartCheckout();
        });

        // Botón "Vaciar Carrito"
        JButton emptyCartButton = new JButton("Vaciar Carrito");
        styleButton(emptyCartButton, Color.RED, headerText);
        emptyCartButton.addActionListener(e -> {
            // Pide confirmación al usuario antes de vaciar el carrito
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres vaciar el carrito?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                emptyUserCart(); // Vacía el carrito del usuario
            }
        });

        // Añade los botones al panel de botones de acción
        actionButtonPanel.add(continueShoppingButton);
        actionButtonPanel.add(emptyCartButton);
        actionButtonPanel.add(checkoutButton);

        // Añade el panel de botones de acción a la parte inferior del panel inferior
        bottomPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        // Añade el panel inferior al panel principal
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Añade el panel principal a la ventana
        add(mainPanel);
    }

    // Método para aplicar estilos a los botones
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Quita el borde de foco
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setOpaque(true); // Asegura que el fondo se pinte
    }

    // Método para cargar los ítems del carrito desde la base de datos
    public void loadCarritoItems() {
        // Verifica si hay un usuario logueado
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "No hay un usuario logueado. Por favor, inicie sesión.", "Error de Sesión", JOptionPane.WARNING_MESSAGE);
            tableModel.setRowCount(0); // Vacía la tabla
            totalLabel.setText("$0.00"); // Reinicia el total
            return;
        }

        int currentUserId = UserSession.getCurrentUserId();
        System.out.println("Cargando carrito para el usuario ID: " + currentUserId);

        conexion con = new conexion(); // Instancia local de la clase de conexión
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSet rsItems = null;

        tableModel.setRowCount(0); // Limpia la tabla antes de cargar nuevos ítems
        BigDecimal totalCarrito = BigDecimal.ZERO; // Inicializa el total del carrito

        try {
            conn = con.getConnection(); // Obtiene la conexión a la base de datos
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cargar el carrito.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Consulta SQL para obtener el ID del carrito del usuario actual
            String sqlGetCarritoId = "SELECT id_carrito FROM Carritos WHERE id_usuario = ?";
            ps = conn.prepareStatement(sqlGetCarritoId);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();

            int idCarrito = -1;
            if (rs.next()) {
                idCarrito = rs.getInt("id_carrito");
                System.out.println("Carrito detectado para el usuario " + currentUserId + ": ID Carrito = " + idCarrito);

                // Consulta SQL para obtener los ítems del carrito y sus detalles de producto
                String sqlGetCarritoItems = "SELECT ci.id_producto, p.nombre, ci.cantidad, p.precio, (ci.cantidad * p.precio) AS subtotal " +
                                            "FROM Carrito_Items ci " +
                                            "JOIN Productos p ON ci.id_producto = p.id_producto " +
                                            "WHERE ci.id_carrito = ?";

                ps = conn.prepareStatement(sqlGetCarritoItems);
                ps.setInt(1, idCarrito);
                rsItems = ps.executeQuery();

                boolean hasItems = false;
                while (rsItems.next()) {
                    hasItems = true;
                    // Crea una fila para la tabla con los datos del ítem del carrito
                    Object[] row = new Object[5];
                    row[0] = rsItems.getInt("id_producto");
                    row[1] = rsItems.getString("nombre");
                    row[2] = rsItems.getInt("cantidad");
                    BigDecimal precioUnitario = rsItems.getBigDecimal("precio");
                    BigDecimal subtotal = rsItems.getBigDecimal("subtotal");
                    row[3] = String.format("$%.2f", precioUnitario);
                    row[4] = String.format("$%.2f", subtotal);
                    tableModel.addRow(row); // Añade la fila a la tabla
                    totalCarrito = totalCarrito.add(subtotal); // Suma al total del carrito
                }

                if (!hasItems) {
                    System.out.println("El carrito del usuario " + currentUserId + " está vacío en la DB.");
                }

            } else {
                System.out.println("El usuario " + currentUserId + " no tiene un carrito existente en la tabla Carritos.");
            }

        } catch (SQLException e) {
            // Manejo de errores de SQL
            JOptionPane.showMessageDialog(this, "Error al cargar el carrito: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al cargar carrito: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Asegura que todos los recursos de la base de datos se cierren
            try {
                if (rsItems != null) rsItems.close();
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar RS/PS en CarritoForm (loadCarritoItems): " + e.getMessage());
            }
            try {
                if (conn != null && !conn.isClosed()) {
                    con.desconectar(); // Cierra la conexión usando el método de la clase conexion
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión en CarritoForm (loadCarritoItems): " + e.getMessage());
            }
        }
        // Actualiza la etiqueta del total del carrito
        totalLabel.setText(String.format("$%.2f", totalCarrito));
    }

    // Método para vaciar el carrito del usuario
    private void emptyUserCart() {
        // Verifica si hay un usuario logueado
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "No hay un usuario logueado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int currentUserId = UserSession.getCurrentUserId();
        conexion con = new conexion(); // Instancia local de la clase de conexión
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = con.getConnection(); // Obtiene la conexión
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para vaciar el carrito.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            conn.setAutoCommit(false); // Inicia una transacción

            // Consulta SQL para obtener el ID del carrito del usuario
            String sqlGetCarritoId = "SELECT id_carrito FROM Carritos WHERE id_usuario = ?";
            ps = conn.prepareStatement(sqlGetCarritoId);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();

            int idCarrito = -1;
            if (rs.next()) {
                idCarrito = rs.getInt("id_carrito");

                // Consulta SQL para eliminar todos los ítems del carrito
                String sqlDeleteItems = "DELETE FROM Carrito_Items WHERE id_carrito = ?";
                ps = conn.prepareStatement(sqlDeleteItems);
                ps.setInt(1, idCarrito);
                int itemsDeleted = ps.executeUpdate();
                System.out.println(itemsDeleted + " ítems eliminados del carrito " + idCarrito);

                conn.commit(); // Confirma la transacción
                JOptionPane.showMessageDialog(this, "El carrito ha sido vaciado exitosamente.", "Carrito Vaciado", JOptionPane.INFORMATION_MESSAGE);
                loadCarritoItems(); // Recarga los ítems del carrito para actualizar la UI
            } else {
                JOptionPane.showMessageDialog(this, "No tienes un carrito para vaciar.", "Carrito Vacío", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            // Manejo de errores de SQL y rollback de la transacción
            try {
                if (conn != null && !conn.isClosed()) conn.rollback(); // Deshace la transacción en caso de error
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback al vaciar carrito: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error al vaciar el carrito: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al vaciar carrito: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Asegura que todos los recursos se cierren
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar RS/PS en emptyUserCart: " + e.getMessage());
            }
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true); // Restaura el modo auto-commit
                    con.desconectar(); // Cierra la conexión
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión en emptyUserCart: " + e.getMessage());
            }
        }
    }

    // Método para procesar la compra del carrito
    private void processCartCheckout() {
        // Verifica si el usuario está logueado
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para poder realizar la compra del carrito.", "Error de Sesión", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verifica si el carrito está vacío
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tu carrito está vacío. No hay ítems para procesar el pago.", "Carrito Vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Obtiene el total de la compra desde la etiqueta de la UI
        String totalText = totalLabel.getText().replace("$", "");
        BigDecimal totalCompra = new BigDecimal(totalText);

        int currentUserId = UserSession.getCurrentUserId();

        // **Centraliza la gestión de la conexión para la transacción**
        // Se usa una única instancia de conexión para toda la transacción
        conexion con = new conexion();
        Connection connection = null;

        try {
            connection = con.getConnection(); // Obtiene la conexión
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para realizar la compra.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }
            connection.setAutoCommit(false); // Inicia una transacción

            // 1. Verificar límite de crédito
            BigDecimal limiteCredito = BigDecimal.ZERO;
            try (PreparedStatement psCheckCredit = connection.prepareStatement("SELECT limite_credito FROM tarjeta_credito WHERE usuario_id = ? FOR UPDATE")) {
                psCheckCredit.setInt(1, currentUserId);
                try (ResultSet rsCredit = psCheckCredit.executeQuery()) {
                    if (rsCredit.next()) {
                        limiteCredito = rsCredit.getBigDecimal("limite_credito");
                    } else {
                        JOptionPane.showMessageDialog(this, "No se encontró una tarjeta de crédito asociada a tu cuenta. No se puede realizar la compra.", "Error de Pago", JOptionPane.ERROR_MESSAGE);
                        connection.rollback(); // Deshace la transacción
                        return;
                    }
                }
            }

            // Comprueba si el total de la compra excede el límite de crédito
            if (totalCompra.compareTo(limiteCredito) > 0) {
                JOptionPane.showMessageDialog(this,
                    String.format("Saldo insuficiente. El total del carrito es $%.2f, pero tu límite de crédito es $%.2f.",
                        totalCompra, limiteCredito),
                    "Saldo Insuficiente", JOptionPane.WARNING_MESSAGE);
                connection.rollback(); // Deshace la transacción
                return;
            }

            // 2. Verificar stock para cada producto en el carrito
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int productId = (int) tableModel.getValueAt(i, 0);
                int cantidadEnCarrito = (int) tableModel.getValueAt(i, 2);

                try (PreparedStatement psGetProductStock = connection.prepareStatement("SELECT stock FROM Productos WHERE id_producto = ?")) {
                    psGetProductStock.setInt(1, productId);
                    try (ResultSet rsProductStock = psGetProductStock.executeQuery()) {
                        if (rsProductStock.next()) {
                            int currentStock = rsProductStock.getInt("stock");
                            // Si la cantidad en el carrito es mayor que el stock disponible
                            if (cantidadEnCarrito > currentStock) {
                                JOptionPane.showMessageDialog(this,
                                    String.format("Stock insuficiente para el producto ID %d (en carrito: %d, disponible: %d). Por favor, ajusta la cantidad en tu carrito.",
                                        productId, cantidadEnCarrito, currentStock),
                                    "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                                connection.rollback(); // Deshace la transacción
                                return;
                            }
                        } else {
                            // Si el producto no se encuentra en la base de datos
                            JOptionPane.showMessageDialog(this, "Producto ID " + productId + " no encontrado en la base de datos. No se puede completar la compra.", "Error de Producto", JOptionPane.ERROR_MESSAGE);
                            connection.rollback(); // Deshace la transacción
                            return;
                        }
                    }
                }
            }

            // Pide confirmación al usuario para la compra
            int confirm = JOptionPane.showConfirmDialog(this,
                String.format("¿Desea confirmar la compra de los ítems en su carrito por un total de $%.2f?", totalCompra),
                "Confirmar Compra del Carrito", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                connection.rollback(); // Deshace la transacción si el usuario cancela
                return;
            }

            // 3. Debitar el límite de crédito
            try (PreparedStatement psUpdateCredit = connection.prepareStatement("UPDATE tarjeta_credito SET limite_credito = limite_credito - ? WHERE usuario_id = ?")) {
                psUpdateCredit.setBigDecimal(1, totalCompra);
                psUpdateCredit.setInt(2, currentUserId);
                int rowsAffected = psUpdateCredit.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No se pudo debitar el límite de crédito. Es posible que el usuario_id no exista en tarjeta_credito.");
                }
                System.out.println("Límite de crédito actualizado para usuario " + currentUserId + ": debitado $" + totalCompra);
            }

            // 4. Insertar en la tabla de órdenes
            int idOrden = -1;
            String sqlOrden = "INSERT INTO ordenes (id_usuario, estado, fecha_orden, total_orden) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement psOrden = connection.prepareStatement(sqlOrden, Statement.RETURN_GENERATED_KEYS)) {
                psOrden.setInt(1, currentUserId);
                psOrden.setString(2, "pagado"); // El estado de la orden es 'pagado'
                psOrden.setBigDecimal(3, totalCompra);
                psOrden.executeUpdate();

                try (ResultSet rsOrder = psOrden.getGeneratedKeys()) {
                    if (rsOrder.next()) {
                        idOrden = rsOrder.getInt(1); // Obtiene el ID de la orden generada
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la orden generada.");
                    }
                }
                System.out.println("Orden creada con ID: " + idOrden);
            }

            // 5. Insertar en la tabla de detalle_ordenes y actualizar el stock para CADA producto en el carrito
            String sqlDetalle = "INSERT INTO detalle_ordenes (id_orden, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE Productos SET stock = stock - ? WHERE id_producto = ?";

            try (PreparedStatement psDetalle = connection.prepareStatement(sqlDetalle);
                 PreparedStatement psUpdateStock = connection.prepareStatement(sqlUpdateStock)) {

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int productId = (int) tableModel.getValueAt(i, 0);
                    int cantidadComprada = (int) tableModel.getValueAt(i, 2);
                    String precioUnitarioFormatted = (String) tableModel.getValueAt(i, 3);
                    BigDecimal precioUnitario = new BigDecimal(precioUnitarioFormatted.replace("$", ""));

                    // Añade el detalle de la orden al lote (batch)
                    psDetalle.setInt(1, idOrden);
                    psDetalle.setInt(2, productId);
                    psDetalle.setInt(3, cantidadComprada);
                    psDetalle.setBigDecimal(4, precioUnitario);
                    psDetalle.addBatch();

                    // Añade la actualización de stock al lote (batch)
                    psUpdateStock.setInt(1, cantidadComprada);
                    psUpdateStock.setInt(2, productId);
                    psUpdateStock.addBatch();
                }

                psDetalle.executeBatch(); // Ejecuta todas las inserciones de detalles de orden
                psUpdateStock.executeBatch(); // Ejecuta todas las actualizaciones de stock
                System.out.println("Detalles de orden e stocks actualizados para todos los ítems del carrito.");
            }

            // 6. Vaciar el carrito del usuario después de una compra exitosa
            int idCarritoToDelete = -1;
            try (PreparedStatement psGetCartId = connection.prepareStatement("SELECT id_carrito FROM Carritos WHERE id_usuario = ?")) {
                psGetCartId.setInt(1, currentUserId);
                try (ResultSet rsCartId = psGetCartId.executeQuery()) {
                    if (rsCartId.next()) {
                        idCarritoToDelete = rsCartId.getInt("id_carrito");
                    }
                }
            }

            if (idCarritoToDelete != -1) {
                try (PreparedStatement psDeleteCartItems = connection.prepareStatement("DELETE FROM Carrito_Items WHERE id_carrito = ?")) {
                    psDeleteCartItems.setInt(1, idCarritoToDelete);
                    psDeleteCartItems.executeUpdate();
                    System.out.println("Carrito de usuario " + currentUserId + " vaciado exitosamente.");
                }
            }

            connection.commit(); // Confirma la transacción completa
            JOptionPane.showMessageDialog(this, "¡Compra del carrito realizada con éxito! Orden #" + idOrden, "Compra Exitosa", JOptionPane.INFORMATION_MESSAGE);

            loadCarritoItems(); // Recarga el carrito para mostrarlo vacío
            this.dispose(); // Cierra la ventana del carrito después de la compra

        } catch (SQLException e) {
            // Manejo de errores de SQL: revierte la transacción completa
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.rollback(); // Deshace toda la transacción en caso de error
                }
            } catch (SQLException ex) {
                System.err.println("Error al revertir la transacción de compra del carrito: " + ex.getMessage());
            }
            System.err.println("Error SQL al procesar la compra del carrito: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al procesar la compra del carrito: " + e.getMessage(), "Error de Compra", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Asegura que la conexión principal para la transacción se cierre
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.setAutoCommit(true); // Restaura el auto-commit
                    con.desconectar(); // Cierra la conexión proporcionada por 'con'
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión final de CarritoForm (processCartCheckout): " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Ejecuta la interfaz gráfica en el hilo de eventos de Swing
        java.awt.EventQueue.invokeLater(() -> {
            // Crea y hace visible una nueva instancia de CarritoForm
            new CarritoForm().setVisible(true);
        });
    }
}