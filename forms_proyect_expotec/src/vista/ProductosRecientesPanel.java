package vista;

import modelo.Producto;
import util.ProductoSeleccionadoListener; // Necesario para el listener en ProductoCardPanel
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Locale;

public class ProductosRecientesPanel extends JPanel {

    private final Color PRIMARY_TEXT_COLOR = UIManager.getColor("Label.foreground");
    private final Color SECONDARY_TEXT_COLOR = UIManager.getColor("Label.disabledForeground"); // Defined here
    private final Color BORDER_COLOR = UIManager.getColor("Component.borderColor");
    private final Color BACKGROUND_COLOR = UIManager.getColor("Panel.background");
    private final Color CARD_BACKGROUND = UIManager.getColor("List.background"); // Unused if using ProductoCardPanel's own background
    private final Color BUTTON_PRIMARY_BACKGROUND = new Color(70, 130, 180);
    private final Color BUTTON_FOREGROUND = Color.WHITE;
    private final Color DANGER_COLOR = new Color(220, 20, 60);

    private JPanel productsGridPanel;
    private JLabel lblNoItemsMessage;
    
    private JPanel centerPanelWrapper;
    private CardLayout centerCardLayout;

    private final int MAX_RECENT_PRODUCTS = 12;
    private LinkedList<Producto> recentProducts = new LinkedList<>();

    // --- NUEVO: Referencia al listener principal del formulario (PrincipalForm) ---
    private ProductoSeleccionadoListener mainProductListener;

    // Constructor que acepta un listener, que usualmente será el PrincipalForm
    public ProductosRecientesPanel(ProductoSeleccionadoListener listener) {
        this.mainProductListener = listener; // Guarda la referencia al listener principal
        initComponents();
        updateProductGrid();
    }
    
    // --- Opcional: Si necesitas un constructor sin listener, para pruebas rápidas
    // Si usas este, asegúrate de que onProductoSeleccionado se maneje fuera o pasas null.
    // public ProductosRecientesPanel() {
    //     initComponents();
    //     updateProductGrid();
    // }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(40, 80, 40, 80));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 30));
        contentPanel.setOpaque(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Productos Vistos Recientemente");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titleLabel.setForeground(PRIMARY_TEXT_COLOR);
        headerPanel.add(titleLabel);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        productsGridPanel = new JPanel();
        productsGridPanel.setLayout(new GridLayout(0, 3, 20, 20));
        productsGridPanel.setBackground(BACKGROUND_COLOR);
        productsGridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        lblNoItemsMessage = createStyledLabel("No hay productos vistos recientemente.", SECONDARY_TEXT_COLOR, new Font("Segoe UI", Font.ITALIC, 16));
        lblNoItemsMessage.setHorizontalAlignment(SwingConstants.CENTER);
        lblNoItemsMessage.setVerticalAlignment(SwingConstants.CENTER);
        lblNoItemsMessage.setVisible(false);

        JScrollPane scrollPane = new JScrollPane(productsGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        centerCardLayout = new CardLayout();
        centerPanelWrapper = new JPanel(centerCardLayout);
        centerPanelWrapper.add(scrollPane, "PRODUCTS_GRID");
        centerPanelWrapper.add(lblNoItemsMessage, "NO_ITEMS_MESSAGE");
        
        contentPanel.add(centerPanelWrapper, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        JButton btnLimpiarHistorial = createStyledButton("Limpiar Historial", DANGER_COLOR, BUTTON_FOREGROUND);
        btnLimpiarHistorial.addActionListener(e -> clearRecentProducts());
        buttonPanel.add(btnLimpiarHistorial);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createStyledLabel(String text, Color foreground, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(foreground);
        label.setFont(font);
        return label;
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    public void addRecentProduct(Producto product) {
        if (product == null) {
            return;
        }

        // Remover el producto si ya existe para moverlo al principio
        recentProducts.remove(product); 
        recentProducts.addFirst(product);

        // Limitar el tamaño del historial
        if (recentProducts.size() > MAX_RECENT_PRODUCTS) {
            recentProducts.removeLast();
        }
        
        updateProductGrid();
    }

    private void clearRecentProducts() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea borrar su historial de productos vistos?",
                "Confirmar Limpiar Historial", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            recentProducts.clear();
            updateProductGrid();
            JOptionPane.showMessageDialog(this, "Historial de productos vistos limpiado.", "Historial Limpiado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Actualiza el JPanel de la cuadrícula con las ProductoCardPanel.
     */
    private void updateProductGrid() {
        productsGridPanel.removeAll(); // Limpiar el contenido anterior

        if (recentProducts.isEmpty()) {
            centerCardLayout.show(centerPanelWrapper, "NO_ITEMS_MESSAGE");
        } else {
            for (Producto p : recentProducts) {
                // --- CAMBIO CLAVE: Usar ProductoCardPanel en modo catálogo ---
                ProductoCardPanel card = new ProductoCardPanel(p, mainProductListener);
                // El listener ya se maneja internamente en ProductoCardPanel
                productsGridPanel.add(card);
            }
            centerCardLayout.show(centerPanelWrapper, "PRODUCTS_GRID");
        }
        
        productsGridPanel.revalidate();
        productsGridPanel.repaint();
        revalidate();
        repaint();
    }

   

    public void loadSampleProducts() {
        
    }

    public void refreshData() {
        updateProductGrid();
    }
}