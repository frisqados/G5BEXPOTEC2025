package vista;

import modelo.Producto;
import controlador.conexion; // Importa tu clase de conexión
import util.UserSession; // Importa tu clase de sesión de usuario
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class MostrarListaDeseosFrame extends JFrame {
    private JPanel productosPanel;
    private JTextField searchField;
    private JButton searchButton;
    private int idUsuarioLogueado; // Variable para almacenar el ID del usuario logueado

    /**
     * Constructor para el frame de la lista de deseos.
     * @param idUsuarioLogueado El ID del usuario cuya lista de deseos se va a mostrar.
     */
    public MostrarListaDeseosFrame(int idUsuarioLogueado) {
        this.idUsuarioLogueado = idUsuarioLogueado; // Asigna el ID del usuario logueado

        setTitle("Mi Lista de Deseos - Tienda Online");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cambiado a DISPOSE_ON_CLOSE para no cerrar la aplicación principal
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(23, 23, 23));
        JLabel titleLabel = new JLabel("Mis Productos Deseados");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // --- Panel para la barra de búsqueda ---
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBarPanel.setBackground(new Color(23, 23, 23));
        
        JLabel searchLabel = new JLabel("Buscar en deseos:");
        searchLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchLabel.setForeground(Color.WHITE);
        searchBarPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchBarPanel.add(searchField);

        searchButton = new JButton("Buscar");
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            cargarProductosListaDeseosDesdeBD(searchTerm); // Llama al método de carga con el término de búsqueda
        });
        searchBarPanel.add(searchButton);

        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchBarPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        productosPanel = new JPanel();
        productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
        productosPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        productosPanel.setBackground(new Color(187,187,187));

        cargarProductosListaDeseosDesdeBD(null); // Carga inicial de productos sin filtro

        JScrollPane scrollPane = new JScrollPane(productosPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * Carga los productos de la lista de deseos del usuario desde la base de datos.
     * Permite filtrar por un término de búsqueda.
     * @param searchTerm Término de búsqueda para filtrar productos, o null si no hay filtro.
     */
    private void cargarProductosListaDeseosDesdeBD(String searchTerm) {
        List<Producto> productos = new ArrayList<>();
        conexion con = new conexion();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = con.getConnection();
            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cargar la lista de deseos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Consulta SQL para obtener productos de la lista de deseos del usuario
            // ¡CORRECCIÓN AQUÍ! Cambiado de "ListaDeseos" a "lista_deseos"
            String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.stock, p.categoria, p.imagen, u.nombre AS nombre_publicador " +
                         "FROM Productos p " +
                         "JOIN lista_deseos ld ON p.id_producto = ld.id_producto " + // ¡Nombre de la tabla corregido!
                         "LEFT JOIN Usuarios u ON p.id_usuario_subida = u.id_usuario " +
                         "WHERE ld.id_usuario = ?"; // Filtrar por el ID del usuario logueado
            
            // Si hay un término de búsqueda, añade la cláusula WHERE adicional
            if (searchTerm != null && !searchTerm.isEmpty()) {
                sql += " AND (p.nombre LIKE ? OR p.descripcion LIKE ? OR p.categoria LIKE ?)";
            }
            
            ps = connection.prepareStatement(sql);
            ps.setInt(1, idUsuarioLogueado); // Establece el ID del usuario logueado

            // Si hay un término de búsqueda, establece los parámetros adicionales
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);
                ps.setString(4, searchPattern);
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                BigDecimal precio = rs.getBigDecimal("precio");
                int stock = rs.getInt("stock");
                String categoria = rs.getString("categoria");
                byte[] imagen = rs.getBytes("imagen");
                String nombrePublicador = rs.getString("nombre_publicador");

                productos.add(new Producto(id, nombre, descripcion, precio, stock, categoria, imagen, nombrePublicador));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener productos de la lista de deseos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al cargar la lista de deseos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (connection != null) con.desconectar();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }

        productosPanel.removeAll(); // Limpiar el panel antes de añadir nuevos productos

        if (productos.isEmpty()) {
            productosPanel.setLayout(new BorderLayout());
            JLabel noProductsLabel = new JLabel("Tu lista de deseos está vacía o no hay productos que coincidan con tu búsqueda.", SwingConstants.CENTER);
            noProductsLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
            noProductsLabel.setForeground(Color.GRAY);
            productosPanel.add(noProductsLabel, BorderLayout.CENTER);
        } else {
            productosPanel.setLayout(new GridLayout(0, 4, 25, 25)); // Restaurar el layout
            for (Producto producto : productos) {
                ProductoCardPanel card = new ProductoCardPanel(producto);
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        DetallesProductoFrame detallesFrame = new DetallesProductoFrame(producto);
                        detallesFrame.setVisible(true);
                    }
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        card.setBackground(new Color(230, 230, 230));
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        card.setBackground(Color.WHITE);
                    }
                });
                productosPanel.add(card);
            }
        }
        productosPanel.revalidate();
        productosPanel.repaint();
    }

    // El método main es solo para pruebas. En una aplicación real, el id_usuario_logueado
    // se pasaría desde el sistema de login.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Suponiendo que el usuario logueado tiene el ID 1. Esto debe ser dinámico en tu aplicación.
            new MostrarListaDeseosFrame(1); 
        });
    }

    /**
     * Clase interna para representar la tarjeta de un producto.
     * Es idéntica a la de MostrarProductosFrame, ya que el diseño es el mismo.
     */
    private class ProductoCardPanel extends JPanel {
        public ProductoCardPanel(Producto producto) {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            setBackground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

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

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

            JLabel lblNombre = new JLabel(producto.getNombre());
            lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
            lblNombre.setForeground(new Color(30, 30, 30));
            lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblPrecio = new JLabel("$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP));
            lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 18));
            lblPrecio.setForeground(new Color(0, 100, 0));
            lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblStock = new JLabel("Stock: " + producto.getStock());
            lblStock.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblStock.setForeground(Color.GRAY);
            lblStock.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblPublicador = new JLabel("Publicado por: " + (producto.getPublisherName() != null ? producto.getPublisherName() : "Desconocido"));
            lblPublicador.setFont(new Font("SansSerif", Font.ITALIC, 11));
            lblPublicador.setForeground(new Color(100, 100, 100));
            lblPublicador.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(lblNombre);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblPrecio);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblStock);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblPublicador);

            add(infoPanel, BorderLayout.CENTER);
        }
    }
}