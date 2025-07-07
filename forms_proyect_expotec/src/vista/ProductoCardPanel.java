package vista; // Or components_ui, depending on your project structure

import modelo.Producto;
import util.ProductoSeleccionadoListener; // Make sure this import is correct
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal; // For handling currency values

public class ProductoCardPanel extends JPanel {
    private ProductoSeleccionadoListener cardListener;

    public ProductoCardPanel(Producto producto, ProductoSeleccionadoListener listener) {
        this.cardListener = listener;

        // Set up the card's general appearance
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Indicate it's clickable

        // --- Product Image Panel ---
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

        // --- Product Information Panel ---
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
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        infoPanel.add(lblPrecio);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        infoPanel.add(lblStock);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        infoPanel.add(lblPublicador);

        add(infoPanel, BorderLayout.CENTER);

        // --- Mouse Listener for Card Click ---
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (cardListener != null) {
                    cardListener.onProductoSeleccionado(producto); // Notify the listener
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(230, 230, 230)); // Hover effect
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE); // Reset on exit
            }
        });
    }
}