package vista;

import modelo.Producto;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class ProductoCardPanel extends JPanel {
    private Producto producto;

    // El constructor ahora solo necesita el Producto.
    // La acción de "Ver Detalles" se maneja directamente creando DetallesProductoFrame.
    public ProductoCardPanel(Producto producto) {
        this.producto = producto;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(112,112,108), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(new Color(112,112,108));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JLabel imagenLabel = new JLabel();
        imagenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagenLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagenLabel.setPreferredSize(new Dimension(200, 200));
        imagenLabel.setOpaque(true);
        imagenLabel.setBackground(new Color(245, 245, 245));
        
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(producto.getImagen()));
                if (originalImage != null) {
                    Image scaledImage = originalImage.getScaledInstance(
                        imagenLabel.getPreferredSize().width, 
                        imagenLabel.getPreferredSize().height, 
                        Image.SCALE_SMOOTH
                    );
                    imagenLabel.setIcon(new ImageIcon(scaledImage));
                } else {
                    imagenLabel.setText("Error de imagen");
                    imagenLabel.setForeground(Color.RED);
                }
            } catch (IOException e) {
                System.err.println("Error al cargar imagen del producto " + producto.getNombre() + ": " + e.getMessage());
                imagenLabel.setText("No Image Available");
                imagenLabel.setForeground(Color.DARK_GRAY);
                imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            }
        } else {
            imagenLabel.setText("No Image Available");
            imagenLabel.setForeground(Color.DARK_GRAY);
            imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        }
        topPanel.add(imagenLabel, BorderLayout.CENTER);

        JButton verDetallesButton = new JButton("Ver Detalles");
        verDetallesButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        verDetallesButton.setBackground(new Color(34, 70, 113));
        verDetallesButton.setForeground(Color.WHITE);
        verDetallesButton.setFocusPainted(false);
        verDetallesButton.setBorderPainted(false);
        verDetallesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        verDetallesButton.addActionListener(e -> {
            // Aquí la Vista directamente crea la vista de detalles.
            // En un MVC completo, esto se delegaría a un controlador.
            new DetallesProductoFrame(producto);
        });
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(verDetallesButton);
        topPanel.add(buttonWrapper, BorderLayout.NORTH);

        add(topPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        infoPanel.setBackground(new Color(112,112,108));

        JLabel nombreLabel = new JLabel("<html><b>" + producto.getNombre() + "</b></html>");
        nombreLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nombreLabel.setForeground(new Color(30, 30, 30));
        nombreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(nombreLabel);

        JLabel precioLabel = new JLabel("<html><b>$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "</b></html>");
        precioLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        precioLabel.setForeground(new Color(178, 34, 34));
        precioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(precioLabel);

        infoPanel.add(Box.createVerticalStrut(5));

        JLabel stockLabel = new JLabel("Stock: " + producto.getStock());
        stockLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stockLabel.setForeground(new Color(245,245,245));
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(stockLabel);

        add(infoPanel, BorderLayout.SOUTH);

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    new DetallesProductoFrame(producto);
                }
            }
        });
    }
}