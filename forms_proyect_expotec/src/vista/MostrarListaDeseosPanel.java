package vista;

import modelo.Producto;
import controlador.conexion;
import util.ProductoSeleccionadoListener;
import util.UserSession;
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

public class MostrarListaDeseosPanel extends JPanel {
    private JPanel productosPanel;
    private int userId;
    private ProductoSeleccionadoListener listener;

    public MostrarListaDeseosPanel(int userId, ProductoSeleccionadoListener listener) {
        this.userId = userId;
        this.listener = listener;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(187, 187, 187));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(23, 23, 23));
        JLabel titleLabel = new JLabel("Mi Lista de Deseos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        productosPanel = new JPanel();
        productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
        productosPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        productosPanel.setBackground(new Color(187, 187, 187));

        cargarListaDeseos();

        JScrollPane scrollPane = new JScrollPane(productosPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public MostrarListaDeseosPanel(ProductoSeleccionadoListener listener) {
        int currentUserId = UserSession.getCurrentUserId();
        if (currentUserId == -1) {
            JOptionPane.showMessageDialog(this, "No hay usuario logueado. Inicia sesión para ver tu lista de deseos.", "Error de Sesión", JOptionPane.WARNING_MESSAGE);
            this.userId = -1;
        } else {
            this.userId = currentUserId;
        }
        this.listener = listener;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(187, 187, 187));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(23, 23, 23));
        JLabel titleLabel = new JLabel("Mi Lista de Deseos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        productosPanel = new JPanel();
        productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
        productosPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        productosPanel.setBackground(new Color(187, 187, 187));

        if (this.userId != -1) {
            cargarListaDeseos();
        } else {
            displayNoUserMessage();
        }

        JScrollPane scrollPane = new JScrollPane(productosPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public MostrarListaDeseosPanel(int userId) {
        this(userId, null);
    }

    private void displayNoUserMessage() {
        productosPanel.removeAll();
        productosPanel.setLayout(new BorderLayout());
        JLabel noUserLabel = new JLabel("Inicia sesión para ver tu lista de deseos.", SwingConstants.CENTER);
        noUserLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        noUserLabel.setForeground(Color.GRAY);
        productosPanel.add(noUserLabel, BorderLayout.CENTER);
        productosPanel.revalidate();
        productosPanel.repaint();
    }

    public void refreshWishList() {
        SwingUtilities.invokeLater(this::cargarListaDeseos);
    }

    private void cargarListaDeseos() {
        if (this.userId == -1) {
            displayNoUserMessage();
            return;
        }

        List<Producto> productos = new ArrayList<>();
        try (Connection connection = new conexion().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.stock, p.categoria, p.imagen, u.nombre AS nombre_publicador " +
                             "FROM Productos p " +
                             "JOIN Lista_Deseos ld ON p.id_producto = ld.id_producto " +
                             "LEFT JOIN Usuarios u ON p.id_usuario_subida = u.id_usuario " +
                             "WHERE ld.id_usuario = ?")) {

            if (connection == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cargar la lista de deseos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            System.err.println("Error SQL al obtener productos de la lista de deseos: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al cargar la lista de deseos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        productosPanel.removeAll();

        if (productos.isEmpty()) {
            productosPanel.setLayout(new BorderLayout());
            JLabel noProductsLabel = new JLabel("No tienes productos en tu lista de deseos. ¡Explora y añade algunos!", SwingConstants.CENTER);
            noProductsLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
            noProductsLabel.setForeground(Color.GRAY);
            productosPanel.add(noProductsLabel, BorderLayout.CENTER);
        } else {
            productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
            for (Producto producto : productos) {
                ProductoCardPanel card = new ProductoCardPanel(producto, this.listener);
                productosPanel.add(card);
            }
        }
        productosPanel.revalidate();
        productosPanel.repaint();
    }

    private void eliminarDeListaDeseos(int productId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres eliminar este producto de tu lista de deseos?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            conexion con = new conexion();
            try (Connection connection = con.getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "DELETE FROM Lista_Deseos WHERE id_usuario = ? AND id_producto = ?")) {

                if (connection == null) {
                    JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para eliminar el producto.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ps.setInt(1, userId);
                ps.setInt(2, productId);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Producto eliminado de tu lista de deseos exitosamente.", "Eliminación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                    if (listener != null) {
                        listener.onProductoRemovidoDeListaDeseos(new Producto(productId, null, null, null, 0, null, null, null));
                    }
                    refreshWishList();
                } else {
                    JOptionPane.showMessageDialog(this, "No se encontró el producto en tu lista de deseos o no se pudo eliminar.", "Error", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException e) {
                System.err.println("Error SQL al eliminar producto de la lista de deseos: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error de base de datos al eliminar el producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ProductoCardPanel extends JPanel {
        private ProductoSeleccionadoListener cardListener;
        private Producto producto;

        public ProductoCardPanel(Producto producto, ProductoSeleccionadoListener listener) {
            this.producto = producto;
            this.cardListener = listener;

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

            JLabel lblPrecio = new JLabel("Q" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP));
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

            JButton btnEliminar = new JButton("Eliminar");
            btnEliminar.setBackground(new Color(200, 70, 70));
            btnEliminar.setForeground(Color.WHITE);
            btnEliminar.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnEliminar.setFocusPainted(false);
            btnEliminar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnEliminar.addActionListener(e -> {
                eliminarDeListaDeseos(producto.getId());
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(btnEliminar);
            add(buttonPanel, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getSource() == ProductoCardPanel.this && e.getComponent() != btnEliminar) {
                        if (cardListener != null) {
                            cardListener.onProductoSeleccionado(producto);
                        }
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                }
            });
        }
    }
}