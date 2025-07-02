package vista;

import modelo.Producto;
import controlador.conexion; // Importa tu clase de conexión
import javax.swing.*;
import java.awt.*;
import java.sql.Connection; // Para Connection
import java.sql.PreparedStatement; // Para PreparedStatement
import java.sql.ResultSet; // Para ResultSet
import java.sql.SQLException; // Para SQLException
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Para BigDecimal

public class MostrarProductosFrame extends JFrame {
    private JPanel productosPanel;

    public MostrarProductosFrame() {
        setTitle("Explorar Productos - Tienda Online");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(23, 23, 23));
        JLabel titleLabel = new JLabel("Explorar Productos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        productosPanel = new JPanel();
        productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
        productosPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        productosPanel.setBackground(new Color(245, 245, 245));

        cargarProductosDesdeBD(); // Llama al método para cargar productos directamente

        JScrollPane scrollPane = new JScrollPane(productosPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void cargarProductosDesdeBD() {
        List<Producto> productos = new ArrayList<>();
        conexion con = new conexion(); // Instancia de tu clase de conexión
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = con.getConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cargar productos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Nombre de la tabla 'Productos' (con mayúscula)
            String sql = "SELECT id_producto, nombre, descripcion, precio, stock, categoria, imagen FROM Productos";
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                BigDecimal precio = rs.getBigDecimal("precio");
                int stock = rs.getInt("stock");
                String categoria = rs.getString("categoria");
                byte[] imagen = rs.getBytes("imagen"); // Obtener imagen como byte array

                productos.add(new Producto(id, nombre, descripcion, precio, stock, categoria, imagen));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener productos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al cargar productos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (connection != null) connection.close(); // Cerrar conexión
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }

        // --- Actualizar el panel de productos con los datos cargados ---
        productosPanel.removeAll(); 

        if (productos.isEmpty()) {
            productosPanel.setLayout(new BorderLayout()); 
            JLabel noProductsLabel = new JLabel("Lo sentimos, no hay productos disponibles en este momento.", SwingConstants.CENTER);
            noProductsLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
            noProductsLabel.setForeground(Color.GRAY);
            productosPanel.add(noProductsLabel, BorderLayout.CENTER);
        } else {
            productosPanel.setLayout(new GridLayout(0, 4, 25, 25)); 
            for (Producto producto : productos) {
                productosPanel.add(new ProductoCardPanel(producto));
            }
        }
        productosPanel.revalidate();
        productosPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MostrarProductosFrame::new);
    }
}