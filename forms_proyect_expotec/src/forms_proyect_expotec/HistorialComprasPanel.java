package forms_proyect_expotec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import controlador.conexion;
import util.UserSession;

public class HistorialComprasPanel extends JPanel {

    private JLabel lblTitulo;
    private int userId;
    private JPanel titlePanel;
    private JPanel contentPanel;
    private JScrollPane mainScrollPane;
    private JPanel centerMessagePanel; // Panel para el mensaje de "no hay datos"

    public HistorialComprasPanel() {
        setLayout(new BorderLayout(25, 25));
        setBackground(new Color(30, 30, 33));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        this.userId = UserSession.getCurrentUserId();
        String userName = UserSession.getCurrentUserName();
        if (this.userId == 0 || userName == null) {
            JOptionPane.showMessageDialog(this, "No hay una sesión de usuario activa. Inicie sesión para ver el historial.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            // No retornar aquí, el panel debe existir para que la interfaz principal pueda mostrarlo.
            // La lógica de carga ya manejará el caso de usuario no logueado mostrando el mensaje apropiado.
        }

        lblTitulo = new JLabel("Historial de Compras de " + (userName != null ? userName : "Invitado"));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 38));
        lblTitulo.setForeground(new Color(240, 240, 240));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        titlePanel = new JPanel();
        titlePanel.setBackground(getBackground());
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        titlePanel.add(lblTitulo);
        add(titlePanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // **HACEMOS EL FONDO TRANSPARENTE AQUÍ**
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getViewport().setOpaque(false); // **TAMBIÉN HACEMOS TRANSPARENTE EL VIEPORT DEL SCROLLPANE**
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Preparamos el panel para el mensaje de "no hay datos"
        JLabel noDataLabel = new JLabel("<html><div style='text-align: center;'>" +
                                        "¡Parece que aún no tienes compras!<br>" +
                                        "Explora nuestros productos y haz tu primera compra." +
                                        "</div></html>");
        noDataLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
        noDataLabel.setForeground(new Color(190, 190, 190));
        noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        centerMessagePanel = new JPanel(new GridBagLayout());
        centerMessagePanel.setOpaque(false); // También transparente
        centerMessagePanel.add(noDataLabel);

        add(mainScrollPane, BorderLayout.CENTER); // Añadimos el scrollpane por defecto

        cargarHistorialCompras(); // Carga inicial
    }

    // Método para ser llamado desde PrincipalForm cuando la sesión del usuario cambia
    public void refreshData() {
        this.userId = UserSession.getCurrentUserId();
        String userName = UserSession.getCurrentUserName();
        lblTitulo.setText("Historial de Compras de " + (userName != null ? userName : "Invitado"));
        cargarHistorialCompras();
    }

    private void cargarHistorialCompras() {
        contentPanel.removeAll(); // Limpiamos el contenido anterior del contentPanel
        mainScrollPane.setViewportView(contentPanel); // Aseguramos que el viewport muestre contentPanel

        if (userId == 0) { // Si no hay usuario logueado
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.add(centerMessagePanel); // Mostrar el mensaje de "no hay datos"
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.revalidate();
            contentPanel.repaint();
            mainScrollPane.getVerticalScrollBar().setValue(0);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Map<Integer, List<Object[]>> ordenesMap = new LinkedHashMap<>();
        Map<Integer, Double> ordenesTotal = new LinkedHashMap<>();
        Map<Integer, String> ordenesFecha = new LinkedHashMap<>();
        Map<Integer, String> ordenesEstado = new LinkedHashMap<>();

        try {
            conn = new conexion().getConnection();
            if (conn == null) {
                // Si no hay conexión, mostrar el mensaje de error y el mensaje de no hay datos
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cargar el historial.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                contentPanel.add(Box.createVerticalGlue());
                contentPanel.add(centerMessagePanel);
                contentPanel.add(Box.createVerticalGlue());
                contentPanel.revalidate();
                contentPanel.repaint();
                mainScrollPane.getVerticalScrollBar().setValue(0);
                return;
            }

            String sql = "SELECT o.id_orden, o.fecha_orden, o.estado, o.total_orden, " +
                         "p.nombre AS nombre_producto, det.cantidad, det.precio_unitario " +
                         "FROM ordenes o " +
                         "JOIN detalle_ordenes det ON o.id_orden = det.id_orden " +
                         "JOIN productos p ON det.id_producto = p.id_producto " +
                         "WHERE o.id_usuario = ? " +
                         "ORDER BY o.fecha_orden DESC, o.id_orden DESC;";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, this.userId);
            rs = pstmt.executeQuery();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy HH:mm", new Locale("es", "GT"));

            while (rs.next()) {
                int idOrden = rs.getInt("id_orden");
                LocalDateTime dateTime = rs.getTimestamp("fecha_orden").toLocalDateTime();
                String fechaFormateada = dateTime.format(dtf);
                String estado = rs.getString("estado");
                double totalOrden = rs.getDouble("total_orden");
                String nombreProducto = rs.getString("nombre_producto");
                int cantidad = rs.getInt("cantidad");
                double precioUnitario = rs.getDouble("precio_unitario");

                ordenesMap.computeIfAbsent(idOrden, k -> new ArrayList<>()).add(
                    new Object[]{nombreProducto, cantidad, precioUnitario}
                );
                ordenesTotal.put(idOrden, totalOrden);
                ordenesFecha.put(idOrden, fechaFormateada);
                ordenesEstado.put(idOrden, estado);
            }

            if (ordenesMap.isEmpty()) {
                contentPanel.add(Box.createVerticalGlue());
                contentPanel.add(centerMessagePanel);
                contentPanel.add(Box.createVerticalGlue());
            } else {
                for (Map.Entry<Integer, List<Object[]>> entry : ordenesMap.entrySet()) {
                    int idOrden = entry.getKey();
                    List<Object[]> productos = entry.getValue();
                    double total = ordenesTotal.get(idOrden);
                    String fecha = ordenesFecha.get(idOrden);
                    String estado = ordenesEstado.get(idOrden);
                    JPanel orderPanel = crearPanelOrden(idOrden, fecha, estado, total, productos);
                    
                    // Aseguramos que el panel de orden no crezca más de lo necesario
                    orderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, orderPanel.getPreferredSize().height));
                    
                    contentPanel.add(orderPanel);
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el historial de compras: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            // Asegurarse de que el mensaje de "no hay datos" se muestre también en caso de error SQL
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.add(centerMessagePanel);
            contentPanel.add(Box.createVerticalGlue());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos en cargarHistorialCompras: " + e.getMessage());
            }
        }
        
        // Actualización inmediata de la interfaz
        contentPanel.revalidate();
        contentPanel.repaint();
        mainScrollPane.getVerticalScrollBar().setValue(0);
    }

    private JPanel crearPanelOrden(int idOrden, String fecha, String estado, double total, List<Object[]> productos) {
        JPanel panelOrden = new JPanel(new BorderLayout(15, 10));
        panelOrden.setBackground(new Color(45, 45, 48));
        panelOrden.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(60, 60, 63), 1, false),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panelOrden.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblOrderId = new JLabel("Orden #" + idOrden);
        lblOrderId.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblOrderId.setForeground(new Color(250, 250, 250));

        JLabel lblFecha = new JLabel(fecha);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblFecha.setForeground(new Color(180, 180, 180));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(lblOrderId);
        leftHeader.add(lblFecha);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        JLabel lblTotal = new JLabel(NumberFormat.getCurrencyInstance(new Locale("es", "GT")).format(total));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(new Color(100, 200, 100));
        headerPanel.add(lblTotal, BorderLayout.EAST);

        panelOrden.add(headerPanel, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setOpaque(false);
        productsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "GT"));

        for (Object[] producto : productos) {
            String nombre = (String) producto[0];
            int cantidad = (int) producto[1];
            double precioUnitario = (double) producto[2];

            JPanel productItemPanel = new JPanel(new BorderLayout());
            productItemPanel.setOpaque(false);
            productItemPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

            JLabel lblProductInfo = new JLabel(cantidad + "x " + nombre);
            lblProductInfo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblProductInfo.setForeground(new Color(220, 220, 220));
            productItemPanel.add(lblProductInfo, BorderLayout.WEST);

            JLabel lblProductPrice = new JLabel(currencyFormat.format(precioUnitario * cantidad));
            lblProductPrice.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblProductPrice.setForeground(new Color(220, 220, 220));
            lblProductPrice.setHorizontalAlignment(SwingConstants.RIGHT);
            productItemPanel.add(lblProductPrice, BorderLayout.EAST);
            
            productsPanel.add(productItemPanel);
        }
        panelOrden.add(productsPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        JLabel lblEstado = new JLabel("Estado: " + estado);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblEstado.setForeground(new Color(150, 180, 255));
        footerPanel.add(lblEstado, BorderLayout.EAST);

        panelOrden.add(footerPanel, BorderLayout.SOUTH);

        return panelOrden;
    }
}