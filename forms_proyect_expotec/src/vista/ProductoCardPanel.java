package vista;

import modelo.Producto;
import util.ProductoSeleccionadoListener;
import util.CartCardActionListener; // Importante para el modo carrito

import javax.swing.*;
import javax.swing.border.EmptyBorder; // Importar EmptyBorder
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;

public class ProductoCardPanel extends JPanel {
    private ProductoSeleccionadoListener cardListener; // Para el modo catálogo
    private CartCardActionListener cartActionListener; // Para el modo carrito

    private Producto producto;
    private JLabel lblStock; // Para el modo catálogo
    private JLabel lblCantidad; // No usado directamente, la cantidad está en el spinner
    private JSpinner quantitySpinner; // Para el modo carrito
    private JLabel lblSubtotal; // Para el modo carrito

    // Constructor para el modo CATÁLOGO
    public ProductoCardPanel(Producto producto, ProductoSeleccionadoListener listener) {
        this.producto = producto;
        this.cardListener = listener;
        initializeCommonComponents();
        setupCatalogMode();
    }

    // Constructor para el modo CARRITO
    public ProductoCardPanel(Producto producto, int cantidadEnCarrito, CartCardActionListener listener) {
        this.producto = producto;
        this.cartActionListener = listener;
        initializeCommonComponents();
        setupCartMode(cantidadEnCarrito);
    }

    private void initializeCommonComponents() {
        setLayout(new BorderLayout(5, 5)); // Ajustado a 5,5 para una tarjeta compacta
        setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1)); // Borde más delgado
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBackground(UIManager.getColor("Panel.background"));

        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(100, 100)); // Tamaño de imagen más pequeño para el carrito
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);

        if (producto.getImagen() != null) {
            ImageIcon originalIcon = new ImageIcon(producto.getImagen());
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblImagen.setIcon(new ImageIcon(scaledImage));
        } else {
            lblImagen.setText("Sin imagen");
        }
        add(lblImagen, BorderLayout.WEST); // Imagen a la izquierda para diseño de carrito
    }

    private void setupCatalogMode() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(getBackground());

        JLabel lblNombre = new JLabel(producto.getNombre());
        lblNombre.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f)); // Fuente más pequeña
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT); // Alineación a la izquierda

        JLabel lblPrecio = new JLabel("$" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP));
        lblPrecio.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f)); // Fuente más pequeña
        lblPrecio.setForeground(UIManager.getColor("TextInfo.foreground"));
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStock = new JLabel("Stock: " + producto.getStock());
        lblStock.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 10f));
        lblStock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPublicador = new JLabel("Publicado por: " + (producto.getPublisherName() != null ? producto.getPublisherName() : "Desconocido"));
        lblPublicador.setFont(UIManager.getFont("Label.font").deriveFont(Font.ITALIC, 10f));
        lblPublicador.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(lblNombre);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(lblPrecio);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(lblStock);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(lblPublicador);

        add(infoPanel, BorderLayout.CENTER);

        // Listener para el modo catálogo
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
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UIManager.getColor("Panel.background"));
            }
        });
    }

    private void setupCartMode(int initialQuantity) {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 1, 0, 3)); // 3 filas: nombre, precio, cantidad/subtotal
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 0)); // Margen a la izquierda
        centerPanel.setBackground(getBackground()); // Hereda el color de fondo

        JLabel lblProductName = new JLabel(producto.getNombre());
        lblProductName.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f));
        centerPanel.add(lblProductName);

        JLabel lblUnitPrice = new JLabel("Precio U.: $" + producto.getPrecio().setScale(2, BigDecimal.ROUND_HALF_UP));
        lblUnitPrice.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 12f));
        centerPanel.add(lblUnitPrice);

        // Panel para Cantidad y Subtotal
        JPanel qtySubtotalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        qtySubtotalPanel.setBackground(getBackground());

        JLabel qtyLabel = new JLabel("Cant:");

        // --- INICIO DE MODIFICACIONES CLAVE ---
        int stockActual = producto.getStock();
        int minQuantity = 1; // Cantidad mínima que un usuario puede tener de un producto
        int maxQuantity = Math.max(0, stockActual); // El máximo no puede ser negativo; si stock es 0, max es 0.

        // Ajustar la cantidad inicial para que sea válida dentro del rango [minQuantity, maxQuantity]
        // O [0, 0] si no hay stock
        int effectiveInitialQuantity = initialQuantity;

        if (maxQuantity == 0) { // Si no hay stock disponible
            effectiveInitialQuantity = 0; // La cantidad inicial debe ser 0
            minQuantity = 0; // El mínimo también debe ser 0 para evitar el error
        } else { // Si hay stock disponible
            if (effectiveInitialQuantity < minQuantity) {
                effectiveInitialQuantity = minQuantity; // Asegurar que sea al menos 1
            }
            if (effectiveInitialQuantity > maxQuantity) {
                effectiveInitialQuantity = maxQuantity; // Asegurar que no exceda el stock
            }
        }
        // --- FIN DE MODIFICACIONES CLAVE ---

        quantitySpinner = new JSpinner(new SpinnerNumberModel(effectiveInitialQuantity, minQuantity, maxQuantity, 1));
        ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField().setColumns(2); // Colocar un tamaño más pequeño
        
        // Deshabilitar el spinner si no hay stock, o si la cantidad inicial es 0 y el stock también es 0
        if (maxQuantity == 0) {
            quantitySpinner.setEnabled(false);
        }

        quantitySpinner.addChangeListener(e -> {
            int newQuantity = (int) quantitySpinner.getValue();
            // Asegurarse de que la cantidad no exceda el stock real si hay algún cambio manual o externo
            if (newQuantity > producto.getStock()) {
                newQuantity = producto.getStock();
                quantitySpinner.setValue(newQuantity); // Corregir el valor del spinner
            }
            if (cartActionListener != null) {
                cartActionListener.onQuantityChanged(producto.getId(), newQuantity);
            }
            updateSubtotalDisplay(newQuantity); // Actualizar el subtotal cada vez que cambia la cantidad
        });

        lblSubtotal = new JLabel("Subtotal: $" + producto.getPrecio().multiply(BigDecimal.valueOf(effectiveInitialQuantity)).setScale(2, BigDecimal.ROUND_HALF_UP));
        lblSubtotal.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 12f));

        // Actualizar el subtotal inicial si el stock era 0
        if (maxQuantity == 0) {
            lblSubtotal.setText("Subtotal: $0.00 (Sin Stock)");
            lblSubtotal.setForeground(Color.RED); // Destacar que no hay stock
        }

        qtySubtotalPanel.add(qtyLabel);
        qtySubtotalPanel.add(quantitySpinner);
        qtySubtotalPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Espacio
        qtySubtotalPanel.add(lblSubtotal);
        
        centerPanel.add(qtySubtotalPanel); // Añadir el panel de cantidad/subtotal al panel central

        add(centerPanel, BorderLayout.CENTER);

        // Botón de Eliminar ('X')
        JButton btnRemove = new JButton("X");
        btnRemove.setFocusPainted(false);
        btnRemove.setFont(new Font("Arial", Font.BOLD, 10)); // Tamaño de fuente más pequeño para 'X'
        btnRemove.setPreferredSize(new Dimension(30, 30)); // Tamaño más pequeño
        btnRemove.setMargin(new Insets(0,0,0,0)); // Eliminar márgenes internos del botón
        btnRemove.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Está seguro de que desea eliminar '" + producto.getNombre() + "' del carrito?",
                    "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (cartActionListener != null) {
                    // Nota: Aquí se llama a onProductoSeleccionado, lo cual es típico para eliminar
                    // Si tienes un método específico para eliminar por ID o Producto, úsalo aquí.
                    cartActionListener.onProductoSeleccionado(producto);
                }
            }
        });
        JPanel removeButtonPanel = new JPanel(new BorderLayout()); // Usar BorderLayout para posicionar el botón
        removeButtonPanel.setBackground(getBackground());
        removeButtonPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); // Pequeño margen
        removeButtonPanel.add(btnRemove, BorderLayout.NORTH); // Botón arriba a la derecha
        add(removeButtonPanel, BorderLayout.EAST);

        // Remover MouseListeners del modo catálogo si existen (ya que este es modo carrito)
        for (MouseListener ml : getMouseListeners()) {
            removeMouseListener(ml);
        }
    }

    public void updateQuantityDisplay(int newQuantity) {
        if (quantitySpinner != null) {
            // Asegúrate de que el nuevo valor sea válido para el spinner.
            // Si el stock cambió o se intentó establecer un valor inválido.
            int maxStock = producto.getStock();
            int minVal = (maxStock > 0) ? 1 : 0; // Mínimo 1 si hay stock, 0 si no
            int maxVal = Math.max(0, maxStock); // Máximo es el stock real, no negativo

            if (newQuantity < minVal) newQuantity = minVal;
            if (newQuantity > maxVal) newQuantity = maxVal;
            
            // Actualizar el modelo del spinner si el rango cambió (por ejemplo, stock disminuyó)
            SpinnerNumberModel model = (SpinnerNumberModel) quantitySpinner.getModel();
            
            // --- CORRECCIÓN AQUÍ ---
            if (((Number)model.getMinimum()).intValue() != minVal || ((Number)model.getMaximum()).intValue() != maxVal) {
            // --- FIN CORRECCIÓN ---
                model = new SpinnerNumberModel(newQuantity, minVal, maxVal, 1);
                quantitySpinner.setModel(model);
                // Si el stock ahora es 0, deshabilitar el spinner
                if (maxVal == 0) {
                    quantitySpinner.setEnabled(false);
                    lblSubtotal.setText("Subtotal: $0.00 (Sin Stock)");
                    lblSubtotal.setForeground(Color.RED);
                } else {
                    quantitySpinner.setEnabled(true);
                    lblSubtotal.setForeground(UIManager.getColor("Label.foreground")); // Restablecer color
                }
            } else {
                quantitySpinner.setValue(newQuantity);
            }
        }
        updateSubtotalDisplay(newQuantity);
    }

    public void updateSubtotalDisplay(int currentQuantity) {
        if (lblSubtotal != null) {
            // Asegúrate de que el subtotal se muestre como $0.00 si el stock es 0, incluso si currentQuantity > 0 por error
            if (producto.getStock() <= 0) {
                lblSubtotal.setText("Subtotal: $0.00 (Sin Stock)");
                lblSubtotal.setForeground(Color.RED);
            } else {
                lblSubtotal.setText("Subtotal: $" + producto.getPrecio().multiply(BigDecimal.valueOf(currentQuantity)).setScale(2, BigDecimal.ROUND_HALF_UP));
                lblSubtotal.setForeground(UIManager.getColor("Label.foreground")); // Restablecer color si el stock es > 0
            }
        }
    }

    public Producto getProducto() {
        return producto;
    }

    public void updateStockDisplay(int newStock) {
        // En el modo catálogo, actualiza la etiqueta de stock
        if (lblStock != null) {
            lblStock.setText("Stock: " + newStock);
        }
        // En el modo carrito, si el stock cambia, necesitamos actualizar el spinner y el subtotal
        if (quantitySpinner != null) {
            // Actualizar el stock del objeto producto (asumiendo que 'newStock' es el stock más reciente)
            producto.setStock(newStock); // Asegúrate de tener un setter para stock en tu clase Producto

            // Llama a updateQuantityDisplay para que el spinner se ajuste al nuevo stock
            // y para que el subtotal se actualice (si el usuario tenía más de lo disponible ahora)
            updateQuantityDisplay((int) quantitySpinner.getValue());
        }
    }

    // Sobrescribir getPreferredSize para dar un tamaño fijo a las tarjetas del carrito si quieres
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 120); // Ancho fijo, alto un poco más grande
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); // Permite expansión horizontal, pero mantiene la altura
    }
}