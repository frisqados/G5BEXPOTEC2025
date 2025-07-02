package vista;

import modelo.Producto;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class DetallesProductoFrame extends JFrame {

    public DetallesProductoFrame(Producto producto) {
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        Font labelFont = new Font("SansSerif", Font.BOLD, 16);
        Font valueFont = new Font("SansSerif", Font.PLAIN, 16);
        Font priceFont = new Font("SansSerif", Font.BOLD, 24);
        Font descriptionFont = new Font("SansSerif", Font.PLAIN, 14);

        setTitle("Detalles del Producto: " + producto.getNombre());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(25, 25));

        JPanel imagenPanel = new JPanel(new BorderLayout());
        imagenPanel.setBackground(Color.WHITE);
        imagenPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel imagenLabel = new JLabel();
        imagenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagenLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagenLabel.setOpaque(true);
        imagenLabel.setBackground(new Color(240, 240, 240));
        
        if (producto.getImagen() != null && producto.getImagen().length > 0) {
            try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(producto.getImagen()));
                if (originalImage != null) {
                    imagenLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
                        @Override
                        public void componentResized(java.awt.event.ComponentEvent e) {
                            int labelWidth = imagenLabel.getWidth();
                            int labelHeight = imagenLabel.getHeight();
                            if (labelWidth > 0 && labelHeight > 0) {
                                double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                                int newWidth = labelWidth;
                                int newHeight = (int) (newWidth / aspectRatio);
                                if (newHeight > labelHeight) {
                                    newHeight = labelHeight;
                                    newWidth = (int) (newHeight * aspectRatio);
                                }
                                Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                                imagenLabel.setIcon(new ImageIcon(scaledImage));
                            }
                        }
                    });
                } else {
                    imagenLabel.setText("Error al leer la imagen");
                    imagenLabel.setForeground(Color.RED);
                    imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 16)); 
                }
            } catch (IOException ex) {
                System.err.println("Error al cargar imagen para detalles: " + ex.getMessage());
                imagenLabel.setText("No hay imagen disponible");
                imagenLabel.setForeground(Color.DARK_GRAY);
                imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            }
        } else {
            imagenLabel.setText("No hay imagen disponible");
            imagenLabel.setForeground(Color.DARK_GRAY);
            imagenLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
        }
        imagenPanel.add(imagenLabel, BorderLayout.CENTER);
        add(imagenPanel, BorderLayout.WEST);

        JPanel detallesPanel = new JPanel();
        detallesPanel.setLayout(new GridBagLayout());
        detallesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        detallesPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        
        JLabel nombreLabel = new JLabel("<html><b>" + producto.getNombre() + "</b></html>");
        nombreLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        nombreLabel.setForeground(new Color(20, 20, 20));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        detallesPanel.add(nombreLabel, gbc);
        row++;

        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel precioLabel = new JLabel("<html><b>$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "</b></html>");
        precioLabel.setFont(priceFont);
        precioLabel.setForeground(new Color(178, 34, 34));
        gbc.gridy = row; detallesPanel.add(precioLabel, gbc);
        row++;
        
        gbc.insets = new Insets(15, 0, 5, 0);

        JLabel descripcionTitle = new JLabel("Descripción:");
        descripcionTitle.setFont(labelFont);
        
        gbc.gridy = row; detallesPanel.add(descripcionTitle, gbc);
        row++;
        
        JTextArea descripcionArea = new JTextArea(producto.getDescripcion());
        descripcionArea.setFont(descriptionFont);
        descripcionArea.setWrapStyleWord(true);
        descripcionArea.setLineWrap(true);
        descripcionArea.setEditable(false);
        descripcionArea.setBackground(detallesPanel.getBackground());
        descripcionArea.setBorder(null);
        descripcionArea.setSelectionColor(new Color(255, 204, 153));

        JScrollPane scrollDesc = new JScrollPane(descripcionArea);
        scrollDesc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollDesc.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        scrollDesc.setPreferredSize(new Dimension(500, 150));
        
        gbc.gridy = row; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        detallesPanel.add(scrollDesc, gbc);
        gbc.weighty = 0;
        row++;
        
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel categoriaLabel = new JLabel("<html><b>Categoría:</b> " + producto.getCategoria() + "</html>");
        categoriaLabel.setFont(valueFont);
        gbc.gridy = row; gbc.fill = GridBagConstraints.HORIZONTAL;
        detallesPanel.add(categoriaLabel, gbc);
        row++;

        JLabel stockLabel = new JLabel("<html><b>Stock Disponible:</b> " + producto.getStock() + " unidades</html>");
        stockLabel.setFont(valueFont);
        gbc.gridy = row; detallesPanel.add(stockLabel, gbc);
        row++;
        
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionButtonPanel.setBackground(Color.WHITE);
        JButton addToCartButton = new JButton("Añadir al Carrito");
        addToCartButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addToCartButton.setBackground(new Color(255, 164, 28));
        addToCartButton.setForeground(Color.BLACK);
        addToCartButton.setFocusPainted(false);
        addToCartButton.setBorderPainted(false);
        addToCartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButtonPanel.add(addToCartButton);

        JButton buyNowButton = new JButton("Comprar Ahora");
        buyNowButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        buyNowButton.setBackground(new Color(255, 153, 0));
        buyNowButton.setForeground(Color.WHITE);
        buyNowButton.setFocusPainted(false);
        buyNowButton.setBorderPainted(false);
        buyNowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButtonPanel.add(buyNowButton);

        gbc.gridy = row; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        detallesPanel.add(actionButtonPanel, gbc);

        add(detallesPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}