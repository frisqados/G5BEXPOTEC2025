package vista;

import modelo.Producto;
import modelo.ProductoDAO; // Importar la nueva clase DAO
import util.ProductoSeleccionadoListener;
import util.ImageUtil; // Posible nueva clase para manejo de imágenes
import util.UserSession; // Si aún la usas, asegúrate que esté correctamente importada

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MostrarProductosPanel extends JPanel {
    private JPanel productosPanel;
    private JTextField searchField;
    private JButton searchButton;
    private ProductoSeleccionadoListener listener;
    private Timer searchTimer;
    private int idUsuario;
    private ProductoDAO productoDAO; // Instancia del DAO

    // Constantes para mejorar la legibilidad y mantenimiento
    private static final int TIMER_DELAY = 300;
    private static final int CARD_IMAGE_SIZE = 150;
    private static final int GRID_COLS = 4;
    private static final int GRID_GAP = 15;
    private static final int BORDER_PADDING = 25;

    public MostrarProductosPanel(ProductoSeleccionadoListener listener, int idUsuario) {
        this.listener = listener;
        this.idUsuario = idUsuario;
        this.productoDAO = new ProductoDAO(); // Inicializar el DAO

        setupPanelLayout();
        setupHeaderPanel();
        setupSearchFunctionality();
        setupProductsPanel();

        cargarProductosAsync(null); // Carga inicial
    }

    private void setupPanelLayout() {
        setLayout(new BorderLayout(GRID_GAP, GRID_GAP));
        setBackground(UIManager.getColor("Panel.background"));
    }

    private void setupHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, BORDER_PADDING, 10, BORDER_PADDING));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel titleLabel = new JLabel("Explorar Productos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBarPanel.setBackground(UIManager.getColor("Panel.background"));

        searchBarPanel.add(new JLabel("Buscar:"));
        searchField = new JTextField(20);
        searchBarPanel.add(searchField);

        searchButton = new JButton("Buscar");
        searchButton.addActionListener(e -> {
            searchTimer.stop();
            cargarProductosAsync(searchField.getText().trim());
        });
        searchBarPanel.add(searchButton);

        headerPanel.add(searchBarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void setupSearchFunctionality() {
        searchTimer = new Timer(TIMER_DELAY, e -> cargarProductosAsync(searchField.getText().trim()));
        searchTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { restartSearchTimer(); }
            @Override public void removeUpdate(DocumentEvent e) { restartSearchTimer(); }
            @Override public void changedUpdate(DocumentEvent e) { restartSearchTimer(); }
            private void restartSearchTimer() {
                if (searchTimer.isRunning()) {
                    searchTimer.restart();
                } else {
                    searchTimer.start();
                }
            }
        });
    }

    private void setupProductsPanel() {
        productosPanel = new JPanel();
        productosPanel.setLayout(new BoxLayout(productosPanel, BoxLayout.Y_AXIS));
        productosPanel.setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
        productosPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane scrollPane = new JScrollPane(productosPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Carga productos de forma asíncrona usando SwingWorker.
     * Muestra recomendaciones si no hay término de búsqueda, de lo contrario, muestra resultados de búsqueda.
     */
    public void cargarProductosAsync(String searchTerm) {
        SwingWorker<List<List<Producto>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<List<Producto>> doInBackground() throws Exception {
                List<List<Producto>> results = new ArrayList<>();
                List<Producto> recomendaciones = new ArrayList<>();
                List<Producto> todos = new ArrayList<>();

                if (searchTerm == null || searchTerm.isEmpty()) {
                    // Solo cargar recomendaciones si no hay término de búsqueda
                    recomendaciones = productoDAO.buscarRecomendaciones(idUsuario);
                }
                // Siempre cargar todos los productos (filtrados o no)
                todos = productoDAO.buscarProductos(searchTerm);

                results.add(recomendaciones);
                results.add(todos);
                return results;
            }

            @Override
            protected void done() {
                try {
                    List<List<Producto>> results = get();
                    List<Producto> recomendaciones = results.get(0);
                    List<Producto> todos = results.get(1);

                    productosPanel.removeAll(); // Limpiar el panel antes de añadir nuevos componentes

                    if (!recomendaciones.isEmpty()) {
                        addSectionHeader("Recomendado para ti");
                        addProductsToPanel(recomendaciones, true); // true para indicar que son recomendaciones (limitadas)
                        productosPanel.add(Box.createRigidArea(new Dimension(0, 30))); // Espacio entre secciones
                    }

                    addSectionHeader("Todos los productos");
                    addProductsToPanel(todos, false);

                    productosPanel.revalidate();
                    productosPanel.repaint();
                } catch (InterruptedException | ExecutionException ex) {
                    // Manejo de errores de la tarea SwingWorker
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MostrarProductosPanel.this,
                            "Error al cargar productos: " + ex.getMessage(),
                            "Error de Carga", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void addSectionHeader(String title) {
        JLabel lblHeader = new JLabel(title);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        productosPanel.add(lblHeader);
        productosPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Espacio debajo del título
    }

    private void addProductsToPanel(List<Producto> products, boolean isRecommendationSection) {
        JPanel panelGrid = new JPanel(new GridLayout(0, GRID_COLS, GRID_GAP, GRID_GAP));
        panelGrid.setBackground(UIManager.getColor("Panel.background"));
        panelGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Si es la sección de recomendaciones, solo añade hasta MAX_RECOMENDACIONES
        int count = 0;
        for (Producto producto : products) {
            if (isRecommendationSection && count >= ProductoDAO.MAX_RECOMENDACIONES) {
                break; // Limitar las recomendaciones visibles si se cargaron más por alguna razón
            }
            panelGrid.add(new ProductoCardPanel(producto, listener));
            count++;
        }
        productosPanel.add(panelGrid);
    }

    // --- Clase interna ProductoCardPanel ---
    private class ProductoCardPanel extends JPanel {
        private ProductoSeleccionadoListener cardListener;

        public ProductoCardPanel(Producto producto, ProductoSeleccionadoListener listener) {
            this.cardListener = listener;

            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(UIManager.getColor("Panel.background"));

            JLabel lblImagen = new JLabel();
            lblImagen.setPreferredSize(new Dimension(CARD_IMAGE_SIZE, CARD_IMAGE_SIZE));
            lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
            lblImagen.setVerticalAlignment(SwingConstants.CENTER);

            // Usa ImageUtil para cargar y escalar la imagen
            if (producto.getImagen() != null) {
                lblImagen.setIcon(ImageUtil.createScaledImageIcon(producto.getImagen(), CARD_IMAGE_SIZE, CARD_IMAGE_SIZE));
            } else {
                lblImagen.setText("No hay imagen");
            }

            add(lblImagen, BorderLayout.NORTH);

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(UIManager.getColor("Panel.background"));
            infoPanel.setBorder(new EmptyBorder(5, 10, 10, 10)); // Añadir un poco de padding

            JLabel lblNombre = new JLabel(producto.getNombre());
            lblNombre.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
            lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblPrecio = new JLabel("$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP));
            lblPrecio.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
            lblPrecio.setForeground(UIManager.getColor("TextInfo.foreground"));
            lblPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblStock = new JLabel("Stock: " + producto.getStock());
            lblStock.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
            lblStock.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblStock.setForeground(producto.getStock() > 0 ? UIManager.getColor("Label.foreground") : Color.RED); // Resaltar stock 0

            JLabel lblPublicador = new JLabel("Publicado por: " + (producto.getPublisherName() != null ? producto.getPublisherName() : "Desconocido"));
            lblPublicador.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC, 11f));
            lblPublicador.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(lblNombre);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblPrecio);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblStock);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblPublicador);

            add(infoPanel, BorderLayout.CENTER);

            // Efectos de hover y click
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (cardListener != null) {
                        cardListener.onProductoSeleccionado(producto);
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(UIManager.getColor("Panel.background").brighter());
                    setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.accentColor"), 2)); // Borde de acento
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(UIManager.getColor("Panel.background"));
                    setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
                }
            });
        }
    }
}