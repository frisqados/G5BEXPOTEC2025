package vista;

import modelo.Producto;
import controlador.conexion;
import util.ProductoSeleccionadoListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.text.Normalizer;
import javax.swing.Timer;

public class MostrarProductosPanel extends JPanel {
    private JPanel productosPanel;
    private JTextField searchField;
    private JButton searchButton;
    private ProductoSeleccionadoListener listener;

    private Timer searchTimer;

    public MostrarProductosPanel(ProductoSeleccionadoListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout(15, 15));

        searchTimer = new Timer(300, e -> {
            String searchTerm = searchField.getText().trim();
            cargarProductosDesdeBD(searchTerm);
        });
        searchTimer.setRepeats(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(23, 23, 23));
        headerPanel.setBorder(new EmptyBorder(10, 25, 10, 25));

        JLabel titleLabel = new JLabel("Explorar Productos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBarPanel.setBackground(new Color(23, 23, 23));
        
        JLabel searchLabel = new JLabel("Buscar:");
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
            searchTimer.stop();
            String searchTerm = searchField.getText().trim();
            cargarProductosDesdeBD(searchTerm);
        });
        searchBarPanel.add(searchButton);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                restartSearchTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                restartSearchTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                restartSearchTimer();
            }

            private void restartSearchTimer() {
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else {
                    searchTimer.start();
                }
            }
        });
        
        headerPanel.add(searchBarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        productosPanel = new JPanel();
        productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
        productosPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        productosPanel.setBackground(new Color(187,187,187));

        cargarProductosDesdeBD(null);

        JScrollPane scrollPane = new JScrollPane(productosPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    public void cargarProductosDesdeBD(String searchTerm) {
        SwingWorker<List<Producto>, Void> worker = new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                List<Producto> productos = new ArrayList<>();
                Connection connection = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    connection = new conexion().getConnection();
                    if (connection == null) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MostrarProductosPanel.this, "No se pudo conectar a la base de datos para cargar productos.", "Error de Conexión", JOptionPane.ERROR_MESSAGE));
                        return productos;
                    }

                    String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.stock, p.categoria, p.imagen, u.nombre AS nombre_publicador " +
                                 "FROM Productos p " +
                                 "LEFT JOIN Usuarios u ON p.id_usuario_subida = u.id_usuario";
                    
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String normalizedSearchTerm = normalizeString(searchTerm);
                        
                        sql += " WHERE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.nombre, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u')) LIKE ? " +
                               " OR LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.descripcion, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u')) LIKE ? " +
                               " OR LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.categoria, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u')) LIKE ?";
                    }
                    
                    ps = connection.prepareStatement(sql);

                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String searchPattern = "%" + normalizeString(searchTerm) + "%";
                        ps.setString(1, searchPattern);
                        ps.setString(2, searchPattern);
                        ps.setString(3, searchPattern);
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
                    System.err.println("Error SQL al obtener productos: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MostrarProductosPanel.this, "Error de base de datos al cargar productos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                } finally {
                    try {
                        if (rs != null) rs.close();
                    } catch (SQLException e) {
                        System.err.println("Error al cerrar ResultSet en MostrarProductosPanel: " + e.getMessage());
                    }
                    try {
                        if (ps != null) ps.close();
                    } catch (SQLException e) {
                        System.err.println("Error al cerrar PreparedStatement en MostrarProductosPanel: " + e.getMessage());
                    }
                    try {
                        if (connection != null) connection.close();
                    } catch (SQLException e) {
                        System.err.println("Error al cerrar Connection en MostrarProductosPanel: " + e.getMessage());
                    }
                }
                return productos;
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    productosPanel.removeAll();

                    if (productos.isEmpty()) {
                        productosPanel.setLayout(new BorderLayout());
                        JLabel noProductsLabel = new JLabel("Lo sentimos, no hay productos disponibles que coincidan con su búsqueda.", SwingConstants.CENTER);
                        noProductsLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
                        noProductsLabel.setForeground(Color.GRAY);
                        productosPanel.add(noProductsLabel, BorderLayout.CENTER);
                    } else {
                        productosPanel.setLayout(new GridLayout(0, 4, 25, 25));
                        for (Producto producto : productos) {
                            ProductoCardPanel card = new ProductoCardPanel(producto, MostrarProductosPanel.this.listener);
                            productosPanel.add(card);
                        }
                    }
                    productosPanel.revalidate();
                    productosPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MostrarProductosPanel.this, "Error al procesar los resultados de la búsqueda: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String normalizeString(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase();
    }

    private class ProductoCardPanel extends JPanel {
        private ProductoSeleccionadoListener cardListener;

        public ProductoCardPanel(Producto producto, ProductoSeleccionadoListener listener) {
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

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (cardListener != null) {
                        cardListener.onProductoSeleccionado(producto);
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(230, 230, 230));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }
            });
        }
    }
}