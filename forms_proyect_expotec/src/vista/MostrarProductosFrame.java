package vista;

import modelo.Producto;
import controlador.conexion; // Importa tu clase de conexión
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter; // Para el MouseListener
import java.awt.event.MouseEvent; // Para el MouseListener
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
        productosPanel.setBackground(new Color(187,187,187));

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

            // --- SQL MODIFICADO: JOIN con la tabla Usuarios para obtener el nombre del publicador ---
            String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.stock, p.categoria, p.imagen, u.nombre AS nombre_publicador " +
                         "FROM Productos p " +
                         "LEFT JOIN Usuarios u ON p.id_usuario_subida = u.id_usuario"; // LEFT JOIN para incluir productos sin publicador
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
                String nombrePublicador = rs.getString("nombre_publicador"); // Obtener el nombre del publicador

                // Crear instancia de Producto con el nombre del publicador
                productos.add(new Producto(id, nombre, descripcion, precio, stock, categoria, imagen, nombrePublicador));
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
                // Pasa el objeto Producto completo a ProductoCardPanel
                ProductoCardPanel card = new ProductoCardPanel(producto);
                // Añadir MouseListener a la tarjeta para abrir DetallesProductoFrame
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Abrir DetallesProductoFrame cuando se hace clic en la tarjeta
                        DetallesProductoFrame detallesFrame = new DetallesProductoFrame(producto);
                        detallesFrame.setVisible(true);
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        card.setBackground(new Color(230, 230, 230)); // Efecto hover
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        card.setBackground(Color.WHITE); // Restaurar color
                    }
                });
                productosPanel.add(card);
            }
        }
        productosPanel.revalidate();
        productosPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MostrarProductosFrame::new);
    }

    /**
     * Clase interna para representar la tarjeta de un producto.
     * Esta clase es la que mostrará los detalles del producto, incluyendo el nombre del publicador.
     */
    private class ProductoCardPanel extends JPanel {
        public ProductoCardPanel(Producto producto) {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Borde exterior sutil
                    BorderFactory.createEmptyBorder(10, 10, 10, 10) // Padding interior
            ));
            setBackground(Color.WHITE); // Fondo blanco para la tarjeta
            setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano para indicar que es clicable

            // Panel para la imagen
            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setBackground(Color.WHITE);
            JLabel lblImagen = new JLabel();
            lblImagen.setPreferredSize(new Dimension(150, 150));
            lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
            lblImagen.setVerticalAlignment(SwingConstants.CENTER);
            if (producto.getImagen() != null) {
                ImageIcon originalIcon = new ImageIcon(producto.getImagen());
                Image scaledImage = originalIcon.getImage().getScaledInstance(
                        150, 150, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(scaledImage));
            } else {
                lblImagen.setText("No hay imagen");
                lblImagen.setFont(new Font("SansSerif", Font.ITALIC, 10));
                lblImagen.setForeground(Color.LIGHT_GRAY);
            }
            imagePanel.add(lblImagen, BorderLayout.CENTER);
            add(imagePanel, BorderLayout.NORTH);

            // Panel para la información del producto
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Padding superior

            JLabel lblNombre = new JLabel(producto.getNombre());
            lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
            lblNombre.setForeground(new Color(30, 30, 30));
            lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar texto

            JLabel lblPrecio = new JLabel("$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP));
            lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 18));
            lblPrecio.setForeground(new Color(0, 100, 0)); // Precio en verde
            lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblStock = new JLabel("Stock: " + producto.getStock());
            lblStock.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblStock.setForeground(Color.GRAY);
            lblStock.setAlignmentX(Component.CENTER_ALIGNMENT);

            // --- Nuevo JLabel para el nombre del publicador ---
            JLabel lblPublicador = new JLabel("Publicado por: " + (producto.getPublisherName() != null ? producto.getPublisherName() : "Desconocido"));
            lblPublicador.setFont(new Font("SansSerif", Font.ITALIC, 11));
            lblPublicador.setForeground(new Color(100, 100, 100));
            lblPublicador.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(lblNombre);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacio
            infoPanel.add(lblPrecio);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacio
            infoPanel.add(lblStock);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Espacio
            infoPanel.add(lblPublicador); // Añadir el nombre del publicador

            add(infoPanel, BorderLayout.CENTER);

            
        }
    }
}