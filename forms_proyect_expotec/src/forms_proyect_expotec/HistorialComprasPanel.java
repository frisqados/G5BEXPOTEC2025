package forms_proyect_expotec;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import controlador.conexion;
import util.UserSession;

public class HistorialComprasPanel extends JPanel {

    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;
    private JLabel lblTitulo;
    private int userId;
    private JPanel titlePanel; // Declarar titlePanel como variable de instancia

    public HistorialComprasPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(45, 45, 48));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        this.userId = UserSession.getCurrentUserId();
        String userName = UserSession.getCurrentUserName();
        if (this.userId == 0 || userName == null) {
            JOptionPane.showMessageDialog(this, "No hay una sesión de usuario activa. Inicie sesión para ver el historial.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblTitulo = new JLabel("Historial de Compras de " + userName);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitulo.setForeground(new Color(230, 230, 230));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        titlePanel = new JPanel();
        titlePanel.setBackground(getBackground());
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        titlePanel.add(lblTitulo);
        add(titlePanel, BorderLayout.NORTH);

        String[] columnas = {"ID Orden", "Fecha Orden", "Estado", "Total Orden", "Producto", "Cantidad", "Precio Unitario"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3 || column == 6) {
                    return Double.class;
                }
                return Object.class;
            }
        };

        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setBackground(new Color(60, 63, 65));
        tablaHistorial.setForeground(new Color(220, 220, 220));
        tablaHistorial.setGridColor(new Color(75, 75, 78));
        tablaHistorial.setSelectionBackground(new Color(80, 120, 150));
        tablaHistorial.setSelectionForeground(Color.WHITE);

        JTableHeader header = tablaHistorial.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(30, 30, 33));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "GT"));
        
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    label.setText(currencyFormat.format(value));
                }
                label.setHorizontalAlignment(JLabel.RIGHT);
                return label;
            }
        };

        tablaHistorial.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaHistorial.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tablaHistorial.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tablaHistorial.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer);
        tablaHistorial.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tablaHistorial.getColumnModel().getColumn(6).setCellRenderer(currencyRenderer);

        JScrollPane scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setBackground(getBackground());
        scrollPane.getViewport().setBackground(tablaHistorial.getBackground());
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 78), 1));
        
        add(scrollPane, BorderLayout.CENTER);

        cargarHistorialCompras();
    }

    private void cargarHistorialCompras() {
        modeloTabla.setRowCount(0);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = new conexion().getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos para cargar el historial.", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
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

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                Object[] row = new Object[7];
                row[0] = rs.getInt("id_orden");
                LocalDateTime dateTime = rs.getTimestamp("fecha_orden").toLocalDateTime();
                row[1] = dateTime.format(dtf);
                row[2] = rs.getString("estado");
                row[3] = rs.getDouble("total_orden");
                row[4] = rs.getString("nombre_producto");
                row[5] = rs.getInt("cantidad");
                row[6] = rs.getDouble("precio_unitario");
                modeloTabla.addRow(row);
            }

            if (modeloTabla.getRowCount() == 0) {
                JLabel noDataLabel = new JLabel("No se encontraron compras para este usuario. ¡Es hora de explorar nuestros productos!");
                noDataLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
                noDataLabel.setForeground(new Color(180, 180, 180));
                noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
                
                JPanel centerPanel = new JPanel(new GridBagLayout());
                centerPanel.setBackground(getBackground());
                centerPanel.add(noDataLabel);
                
                removeAll();
                add(titlePanel, BorderLayout.NORTH);
                add(centerPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
            } else {
                Component[] components = getComponents();
                boolean scrollPaneExists = false;
                for (Component comp : components) {
                    if (comp instanceof JScrollPane) {
                        scrollPaneExists = true;
                        break;
                    }
                }
                if (!scrollPaneExists) {
                    removeAll();
                    add(titlePanel, BorderLayout.NORTH);
                    JScrollPane newScrollPane = new JScrollPane(tablaHistorial);
                    newScrollPane.setBackground(getBackground());
                    newScrollPane.getViewport().setBackground(tablaHistorial.getBackground());
                    newScrollPane.setBorder(BorderFactory.createLineBorder(new Color(75, 75, 78), 1));
                    add(newScrollPane, BorderLayout.CENTER);
                    revalidate();
                    repaint();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el historial de compras: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos en cargarHistorialCompras: " + e.getMessage());
            }
        }
    }
}