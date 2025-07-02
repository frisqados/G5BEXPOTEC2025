package forms_proyect_expotec; // Asegúrate de que este sea el paquete correcto para tu formulario

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JLabel; // Para el título y el total
import javax.swing.JButton; // Para los botones de acción
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import java.awt.FlowLayout; // Para el panel de botones
import java.awt.Dimension; // Para establecer dimensiones preferidas

import controlador.conexion;
import util.UserSession;

public class CarritoForm extends JFrame {

    private JTable carritoTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel; // Etiqueta para mostrar el total del carrito

    public CarritoForm() {
        initComponents();
        loadCarritoItems(); // Llama a este método para cargar los ítems del carrito al iniciar el formulario
    }

    private void initComponents() {
        setTitle("Mi Carrito de Compras");
        setSize(1200, 700); // Establece las medidas iniciales del formulario
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        setLocationRelativeTo(null); // Centrar la ventana

        // Definición de colores personalizados
        Color darkBlue = new Color(34, 70, 113); // #224671
        Color lightGrey = new Color(240, 240, 240); // Gris muy claro para fondos
        Color mediumGrey = new Color(220, 220, 220); // Gris para bordes o fondos alternos
        Color darkText = Color.BLACK; // Texto principal
        Color headerText = Color.WHITE; // Texto del encabezado de la tabla

        // --- Panel Principal (Contenedor de todo) ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(lightGrey); // Fondo general del panel principal
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding alrededor de los bordes

        // --- Panel de Encabezado ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(darkBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // Padding interno

        JLabel titleLabel = new JLabel("Mi Carrito de Compras");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Fuente más grande y negrita
        titleLabel.setForeground(headerText); // Texto blanco
        headerPanel.add(titleLabel, BorderLayout.WEST); // Título a la izquierda del encabezado

        mainPanel.add(headerPanel, BorderLayout.NORTH); // Colocar el encabezado arriba

        // --- Configuración de la Tabla de Carrito ---
        String[] columnNames = {"ID Producto", "Nombre Producto", "Cantidad", "Precio Unitario", "Subtotal"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas de la tabla no sean editables
            }
        };
        carritoTable = new JTable(tableModel);

        // Estilos de la tabla
        carritoTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        carritoTable.setRowHeight(35); // Aumentar altura de filas
        carritoTable.setGridColor(mediumGrey);
        carritoTable.setShowVerticalLines(false);
        carritoTable.setFillsViewportHeight(true); // La tabla llenará la altura del JScrollPane

        // Estilo del encabezado de la tabla
        carritoTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16)); // Fuente más grande para encabezado
        carritoTable.getTableHeader().setBackground(darkBlue);
        carritoTable.getTableHeader().setForeground(headerText);
        carritoTable.getTableHeader().setReorderingAllowed(false);
        carritoTable.getTableHeader().setResizingAllowed(true);

        // Centrar el texto en las celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        for (int i = 0; i < carritoTable.getColumnCount(); i++) {
            carritoTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Añadir la tabla a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(carritoTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(mediumGrey, 1)); // Borde sutil

        mainPanel.add(scrollPane, BorderLayout.CENTER); // La tabla ocupa el centro del panel principal

        // --- Panel de Resumen (Total y Botones de Acción) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(lightGrey);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0)); // Padding superior

        // Panel para el Total
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Alinear a la derecha
        summaryPanel.setBackground(Color.WHITE); // Fondo blanco para el resumen
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mediumGrey, 1), // Borde exterior
                BorderFactory.createEmptyBorder(10, 20, 10, 20) // Padding interior
        ));

        JLabel totalTextLabel = new JLabel("Total del Carrito: ");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTextLabel.setForeground(darkText);

        totalLabel = new JLabel("$0.00"); // Etiqueta donde se mostrará el total
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(darkBlue); // Color del total

        summaryPanel.add(totalTextLabel);
        summaryPanel.add(totalLabel);

        bottomPanel.add(summaryPanel, BorderLayout.NORTH); // El resumen arriba en el panel inferior

        // Panel para los Botones de Acción
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); // Espacio entre botones
        actionButtonPanel.setBackground(lightGrey); // Fondo igual que el mainPanel

        JButton continueShoppingButton = new JButton("Continuar Comprando");
        styleButton(continueShoppingButton, darkBlue, headerText); // Aplicar estilo al botón
        continueShoppingButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Continuar Comprando... (Lógica no implementada)", "Acción", JOptionPane.INFORMATION_MESSAGE);
            // Lógica para cerrar el carrito y volver a la tienda o al form1
            this.dispose();
        });

        JButton checkoutButton = new JButton("Proceder al Pago");
        styleButton(checkoutButton, darkBlue, headerText); // Aplicar estilo al botón
        checkoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Procediendo al Pago... (Lógica no implementada)", "Acción", JOptionPane.INFORMATION_MESSAGE);
            // Lógica para ir al proceso de pago
        });

        JButton emptyCartButton = new JButton("Vaciar Carrito");
        styleButton(emptyCartButton, Color.RED, headerText); // Botón de vaciar en rojo
        emptyCartButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres vaciar el carrito?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                emptyUserCart(); // Llama al método para vaciar el carrito
            }
        });

        actionButtonPanel.add(continueShoppingButton);
        actionButtonPanel.add(emptyCartButton); // Vaciar carrito antes de proceder al pago
        actionButtonPanel.add(checkoutButton);


        bottomPanel.add(actionButtonPanel, BorderLayout.SOUTH); // Los botones abajo en el panel inferior

        mainPanel.add(bottomPanel, BorderLayout.SOUTH); // Colocar el panel inferior en el panel principal

        add(mainPanel); // Añadir el panel principal al JFrame
    }

    /**
     * Método para aplicar un estilo consistente a los botones.
     */
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Quitar el borde de foco
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1), // Borde más oscuro del color de fondo
            BorderFactory.createEmptyBorder(10, 20, 10, 20) // Padding interno
        ));
        button.setOpaque(true); // Necesario para que el color de fondo se vea
    }


    private void loadCarritoItems() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "No hay un usuario logueado. Por favor, inicie sesión.", "Error de Sesión", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int currentUserId = UserSession.getCurrentUserId();
        System.out.println("Cargando carrito para el usuario ID: " + currentUserId);

        conexion con = new conexion();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSet rsItems = null;

        tableModel.setRowCount(0); // Limpiar la tabla
        double totalCarrito = 0.0; // Variable para calcular el total

        try {
            con.conectar();
            conn = con.getConnection();

            // PASO 1: Obtener el id_carrito del usuario
            String sqlGetCarritoId = "SELECT id_carrito FROM Carritos WHERE id_usuario = ?";
            ps = conn.prepareStatement(sqlGetCarritoId);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();

            int idCarrito = -1;
            if (rs.next()) {
                idCarrito = rs.getInt("id_carrito");
                System.out.println("Carrito detectado para el usuario " + currentUserId + ": ID Carrito = " + idCarrito);

                // PASO 2: Obtener los ítems del carrito
                String sqlGetCarritoItems = "SELECT ci.id_producto, p.nombre_producto, ci.cantidad, p.precio_unitario, (ci.cantidad * p.precio_unitario) AS subtotal " +
                                            "FROM Carrito_Items ci " +
                                            "JOIN Productos p ON ci.id_producto = p.id_producto " +
                                            "WHERE ci.id_carrito = ?";

                ps = conn.prepareStatement(sqlGetCarritoItems);
                ps.setInt(1, idCarrito);
                rsItems = ps.executeQuery();

                boolean hasItems = false;
                while (rsItems.next()) {
                    hasItems = true;
                    Object[] row = new Object[5];
                    row[0] = rsItems.getInt("id_producto");
                    row[1] = rsItems.getString("nombre_producto");
                    row[2] = rsItems.getInt("cantidad");
                    row[3] = rsItems.getDouble("precio_unitario");
                    double subtotal = rsItems.getDouble("subtotal");
                    row[4] = subtotal;
                    tableModel.addRow(row);
                    totalCarrito += subtotal; // Sumar al total
                }

                if (!hasItems) {
                    System.out.println("El carrito del usuario " + currentUserId + " está vacío.");
                }

            } else {
                System.out.println("El usuario " + currentUserId + " no tiene un carrito existente en la tabla Carritos.");
                JOptionPane.showMessageDialog(this, "Tu carrito está vacío o no ha sido creado aún.", "Carrito Vacío", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el carrito: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al cargar carrito: " + e.getMessage());
        } finally {
            try {
                if (rsItems != null) rsItems.close();
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) con.desconectar();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de la base de datos en CarritoForm: " + e.getMessage());
            }
        }
        // Actualizar la etiqueta del total después de cargar todos los ítems
        totalLabel.setText(String.format("$%.2f", totalCarrito));
    }

    /**
     * Método para vaciar el carrito del usuario actual.
     */
    private void emptyUserCart() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "No hay un usuario logueado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int currentUserId = UserSession.getCurrentUserId();
        conexion con = new conexion();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con.conectar();
            conn = con.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Obtener el id_carrito del usuario
            String sqlGetCarritoId = "SELECT id_carrito FROM Carritos WHERE id_usuario = ?";
            ps = conn.prepareStatement(sqlGetCarritoId);
            ps.setInt(1, currentUserId);
            rs = ps.executeQuery();

            int idCarrito = -1;
            if (rs.next()) {
                idCarrito = rs.getInt("id_carrito");

                // Eliminar ítems del carrito_items
                String sqlDeleteItems = "DELETE FROM Carrito_Items WHERE id_carrito = ?";
                ps = conn.prepareStatement(sqlDeleteItems);
                ps.setInt(1, idCarrito);
                int itemsDeleted = ps.executeUpdate();
                System.out.println(itemsDeleted + " ítems eliminados del carrito " + idCarrito);

                // Opcional: Si quieres eliminar el carrito completo de la tabla Carritos también
                // String sqlDeleteCarrito = "DELETE FROM Carritos WHERE id_carrito = ?";
                // ps = conn.prepareStatement(sqlDeleteCarrito);
                // ps.setInt(1, idCarrito);
                // ps.executeUpdate();
                // System.out.println("Carrito " + idCarrito + " eliminado.");

                conn.commit(); // Confirmar la transacción
                JOptionPane.showMessageDialog(this, "El carrito ha sido vaciado exitosamente.", "Carrito Vaciado", JOptionPane.INFORMATION_MESSAGE);
                loadCarritoItems(); // Recargar la tabla para mostrar el carrito vacío
            } else {
                JOptionPane.showMessageDialog(this, "No tienes un carrito para vaciar.", "Carrito Vacío", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Revertir la transacción en caso de error
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Error al vaciar el carrito: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error SQL al vaciar carrito: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Restaurar autocommit
                    con.desconectar();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos de la base de datos en emptyUserCart: " + e.getMessage());
            }
        }
    }


    public static void main(String[] args) {
        UserSession.login(1, "UsuarioDePrueba", "prueba@ejemplo.com");

        java.awt.EventQueue.invokeLater(() -> {
            new CarritoForm().setVisible(true);
        });
    }
}
