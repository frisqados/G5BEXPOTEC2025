package vista;

import modelo.Producto; // Keep this import
import controlador.conexion;
import util.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
// Import for GridBagConstraints
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class DashboardPanel extends JPanel {

    private JLabel lblTotalVentas;
    private JLabel lblIngresosTotales;
    private JTextArea topProductsArea;
    private JPanel chartPanel;

    private Map<String, BigDecimal> monthlySalesData;

    public DashboardPanel() {
        monthlySalesData = new LinkedHashMap<>();
        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(30, 30));
        setBackground(new Color(248, 248, 255));
        setBorder(new EmptyBorder(50, 50, 50, 50));

        // --- Title Label ---
        JLabel titleLabel = new JLabel("Panel de Control", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 46));
        titleLabel.setForeground(new Color(50, 55, 100));
        add(titleLabel, BorderLayout.NORTH);

        // --- Main Content Panel using GridBagLayout ---
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // More generous insets
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // --- Total Products Sold Card ---
        RoundedPanel totalVentasCard = new RoundedPanel(new Color(235, 240, 250), 20); // Very light blue
        totalVentasCard.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        JLabel totalVentasTitle = new JLabel("Productos Vendidos", SwingConstants.CENTER);
        totalVentasTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalVentasTitle.setForeground(new Color(70, 80, 130));
        totalVentasCard.add(totalVentasTitle);
        lblTotalVentas = new JLabel("0", SwingConstants.CENTER);
        lblTotalVentas.setFont(new Font("Segoe UI", Font.BOLD, 68));
        lblTotalVentas.setForeground(new Color(50, 130, 255));
        totalVentasCard.add(lblTotalVentas);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0.4; // Less vertical weight for KPI cards
        contentPanel.add(totalVentasCard, gbc);

        // --- Total Revenue Card ---
        RoundedPanel ingresosCard = new RoundedPanel(new Color(240, 250, 240), 20); // Very light green
        ingresosCard.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        JLabel ingresosTitle = new JLabel("Ingresos Totales", SwingConstants.CENTER);
        ingresosTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        ingresosTitle.setForeground(new Color(70, 130, 80));
        ingresosCard.add(ingresosTitle);
        lblIngresosTotales = new JLabel("$0.00", SwingConstants.CENTER);
        lblIngresosTotales.setFont(new Font("Segoe UI", Font.BOLD, 68));
        lblIngresosTotales.setForeground(new Color(50, 180, 70));
        ingresosCard.add(lblIngresosTotales);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0.4;
        contentPanel.add(ingresosCard, gbc);

        // --- Top Products Sold Panel ---
        RoundedPanel topProductsPanel = new RoundedPanel(new Color(255, 252, 240), 20); // Very light yellow
        topProductsPanel.setLayout(new BorderLayout(20, 20));
        JLabel topProductsTitle = new JLabel("Top Productos Vendidos", SwingConstants.CENTER);
        topProductsTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        topProductsTitle.setForeground(new Color(160, 110, 20));
        topProductsTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        topProductsPanel.add(topProductsTitle, BorderLayout.NORTH);

        topProductsArea = new JTextArea();
        topProductsArea.setEditable(false);
        topProductsArea.setFont(new Font("Consolas", Font.PLAIN, 16)); // Monospaced but a bit more modern
        topProductsArea.setBackground(new Color(255, 255, 248));
        topProductsArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topProductsArea.setLineWrap(true);
        topProductsArea.setWrapStyleWord(true);
        JScrollPane scrollTopProducts = new JScrollPane(topProductsArea);
        scrollTopProducts.setBorder(BorderFactory.createEmptyBorder());
        scrollTopProducts.getViewport().setBackground(topProductsArea.getBackground());
        topProductsPanel.add(scrollTopProducts, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.6; // More vertical weight for the list
        contentPanel.add(topProductsPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // --- Chart Panel (Monthly Revenue) ---
        chartPanel = new RoundedPanel(Color.WHITE, 20) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int chartPadding = 50;
                int actualWidth = getWidth() - 2 * chartPadding;
                int actualHeight = getHeight() - 2 * chartPadding;

                if (monthlySalesData == null || monthlySalesData.isEmpty() || monthlySalesData.values().stream().allMatch(bd -> bd.compareTo(BigDecimal.ZERO) == 0)) {
                    g2d.setColor(new Color(170, 170, 170));
                    g2d.setFont(new Font("Segoe UI", Font.ITALIC, 20));
                    String noDataMsg = "Sin datos de ventas mensuales.";
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(noDataMsg)) / 2;
                    int y = getHeight() / 2;
                    g2d.drawString(noDataMsg, x, y);
                    g2d.dispose();
                    return;
                }

                List<String> periods = new ArrayList<>(monthlySalesData.keySet());
                List<BigDecimal> sales = new ArrayList<>(monthlySalesData.values());

                BigDecimal maxSales = BigDecimal.ZERO;
                if (!sales.isEmpty()) {
                    maxSales = sales.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
                }
                if (maxSales.compareTo(BigDecimal.ZERO) == 0) maxSales = BigDecimal.ONE;

                int barWidth = (actualWidth / periods.size()) - 25;
                if (barWidth < 20) barWidth = 20;
                if (barWidth > 60) barWidth = 60;

                int totalBarsWidth = periods.size() * barWidth;
                int totalGapWidth = actualWidth - totalBarsWidth;
                int actualGap = periods.size() > 1 ? totalGapWidth / (periods.size() - 1) : 0;
                if (actualGap < 10) actualGap = 10;

                int startX = chartPadding + (actualWidth - (totalBarsWidth + (periods.size() - 1) * actualGap)) / 2;
                int baseY = getHeight() - chartPadding;
                int chartHeight = baseY - (chartPadding + 40); // Space for title

                // Chart Title
                g2d.setColor(new Color(50, 55, 100));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
                String chartTitleText = "Ingresos Mensuales";
                FontMetrics fm = g2d.getFontMetrics();
                int titleX = (getWidth() - fm.stringWidth(chartTitleText)) / 2;
                g2d.drawString(chartTitleText, titleX, chartPadding - 10);

                // Y-axis labels and grid lines (softer gray)
                g2d.setColor(new Color(230, 230, 230));
                g2d.setStroke(new BasicStroke(1));
                int numYLabels = 5;
                for (int i = 0; i <= numYLabels; i++) {
                    int y = baseY - (i * chartHeight / numYLabels);
                    g2d.drawLine(chartPadding - 10, y, getWidth() - chartPadding + 10, y);
                    BigDecimal value = maxSales.multiply(new BigDecimal(i)).divide(new BigDecimal(numYLabels), 2, RoundingMode.HALF_UP);
                    g2d.setColor(new Color(90, 90, 90));
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    String label = "$" + value.toPlainString();
                    g2d.drawString(label, chartPadding - 15 - fm.stringWidth(label), y + fm.getAscent() / 2);
                }

                // Axes (subtler)
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawLine(chartPadding - 10, chartPadding - 20, chartPadding - 10, baseY);
                g2d.drawLine(chartPadding - 10, baseY, getWidth() - chartPadding + 10, baseY);

                // Bars and X-axis labels (softer blues)
                for (int i = 0; i < sales.size(); i++) {
                    BigDecimal currentSale = sales.get(i);
                    int barActualHeight = currentSale.divide(maxSales, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(chartHeight)).intValue();
                    if (barActualHeight == 0 && currentSale.compareTo(BigDecimal.ZERO) > 0) barActualHeight = 2; // Smallest visible

                    Color barStartColor = new Color(150, 200, 255);
                    Color barEndColor = new Color(50, 150, 255);
                    GradientPaint gp = new GradientPaint(startX + i * (barWidth + actualGap), baseY - barActualHeight, barStartColor, startX + i * (barWidth + actualGap), baseY, barEndColor);
                    g2d.setPaint(gp);
                    g2d.fillRect(startX + i * (barWidth + actualGap), baseY - barActualHeight, barWidth, barActualHeight);

                    // Value labels (smaller, lighter)
                    g2d.setColor(new Color(70, 70, 70));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    String valueStr = "$" + currentSale.setScale(2, RoundingMode.HALF_UP).toPlainString();
                    int valueX = startX + i * (barWidth + actualGap) + (barWidth - fm.stringWidth(valueStr)) / 2;
                    g2d.drawString(valueStr, valueX, baseY - barActualHeight - 8);

                    // Period labels (smaller, lighter)
                    g2d.setColor(new Color(120, 120, 120));
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String periodStr = periods.get(i);
                    int periodX = startX + i * (barWidth + actualGap) + (barWidth - fm.stringWidth(periodStr)) / 2;
                    g2d.drawString(periodStr, periodX, baseY + 20);
                }
                g2d.dispose();
            }
        };
        chartPanel.setPreferredSize(new Dimension(700, 400)); // Adjust chart height
        chartPanel.setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.EAST);
    }

    private void loadDashboardData() {
        int userId = UserSession.getCurrentUserId();
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Debe iniciar sesión para ver el dashboard.", "Error de Sesión", JOptionPane.ERROR_MESSAGE);
            lblTotalVentas.setText("N/A");
            lblIngresosTotales.setText("$N/A");
            topProductsArea.setText("Inicia sesión para ver tus datos de ventas.");
            monthlySalesData.clear();
            chartPanel.repaint();
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // --- 1. Total Productos e Ingresos ---
            String salesSummarySql = "SELECT SUM(\"do\".cantidad) AS total_productos_vendidos, " +
                                     "SUM(\"do\".cantidad * \"do\".precio_unitario) AS ingresos_totales " +
                                     "FROM detalle_ordenes \"do\" " +
                                     "JOIN productos p ON \"do\".id_producto = p.id_producto " +
                                     "JOIN ordenes o ON \"do\".id_orden = o.id_orden " +
                                     "WHERE p.id_usuario_subida = ? AND o.estado = 'completada'";
            PreparedStatement psSummary = con.prepareStatement(salesSummarySql);
            psSummary.setInt(1, userId);
            ResultSet rsSummary = psSummary.executeQuery();
            if (rsSummary.next()) {
                lblTotalVentas.setText(String.valueOf(rsSummary.getInt("total_productos_vendidos")));
                BigDecimal totalRevenue = rsSummary.getBigDecimal("ingresos_totales");
                lblIngresosTotales.setText("$" + (totalRevenue != null ? totalRevenue.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00"));
            } else {
                lblTotalVentas.setText("0");
                lblIngresosTotales.setText("$0.00");
            }
            rsSummary.close();
            psSummary.close();

            // --- 2. Top Productos ---
            String topProductsSql = "SELECT p.nombre, SUM(\"do\".cantidad) AS cantidad_vendida " +
                                    "FROM productos p " +
                                    "JOIN detalle_ordenes \"do\" ON p.id_producto = \"do\".id_producto " +
                                    "JOIN ordenes o ON \"do\".id_orden = o.id_orden " +
                                    "WHERE p.id_usuario_subida = ? AND o.estado = 'completada' " +
                                    "GROUP BY p.nombre ORDER BY cantidad_vendida DESC LIMIT 5";
            PreparedStatement psTopProducts = con.prepareStatement(topProductsSql);
            psTopProducts.setInt(1, userId);
            ResultSet rsTopProducts = psTopProducts.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-30s %15s%n", "Producto", "Vendidos"));
            sb.append(String.format("%-30s %15s%n", "------------------------------", "---------------"));
            boolean foundProducts = false;
            while (rsTopProducts.next()) {
                foundProducts = true;
                sb.append(String.format("%-30s %15d%n", rsTopProducts.getString("nombre"), rsTopProducts.getInt("cantidad_vendida")));
            }
            if (!foundProducts) sb.append("\n    No hay ventas aún.\n");
            topProductsArea.setText(sb.toString());
            rsTopProducts.close();
            psTopProducts.close();

            // --- 3. Ingresos Mensuales ---
            monthlySalesData.clear();
            for (int i = 0; i < 6; i++) {
                LocalDate date = LocalDate.now().minusMonths(5 - i);
                String monthYear = date.format(DateTimeFormatter.ofPattern("MMM"));
                monthlySalesData.put(monthYear, BigDecimal.ZERO);
            }
            String monthlySalesSql = "SELECT TO_CHAR(o.fecha_orden, 'Mon') AS mes_anio, " +
                                     "SUM(\"do\".cantidad * \"do\".precio_unitario) AS ingresos_mes " +
                                     "FROM ordenes o JOIN detalle_ordenes \"do\" ON o.id_orden = \"do\".id_orden " +
                                     "JOIN productos p ON \"do\".id_producto = p.id_producto " +
                                     "WHERE p.id_usuario_subida = ? AND o.estado = 'completada' " +
                                     "AND o.fecha_orden >= NOW() - INTERVAL '6 months' " +
                                     "GROUP BY mes_anio, TO_CHAR(o.fecha_orden, 'YYYYMM') ORDER BY TO_CHAR(o.fecha_orden, 'YYYYMM') ASC";
            PreparedStatement psMonthlySales = con.prepareStatement(monthlySalesSql);
            psMonthlySales.setInt(1, userId);
            ResultSet rsMonthlySales = psMonthlySales.executeQuery();
            while (rsMonthlySales.next()) {
                monthlySalesData.put(rsMonthlySales.getString("mes_anio"), rsMonthlySales.getBigDecimal("ingresos_mes"));
            }
            rsMonthlySales.close();
            psMonthlySales.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try { if (con != null) con.close(); } catch (SQLException finalEx) { System.err.println("Error al cerrar conexión: " + finalEx.getMessage()); }
        }
    }

    public void refreshData() {
        loadDashboardData();
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}

class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color backgroundColor;
    private int shadowSize = 5;
    private Color shadowColor = new Color(0, 0, 0, 20);

    public RoundedPanel(Color bgColor, int radius) {
        this.backgroundColor = bgColor;
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw shadow
        g2d.setColor(shadowColor);
        g2d.fillRoundRect(shadowSize, shadowSize, width - shadowSize - 1, height - shadowSize - 1, cornerRadius, cornerRadius);

        // Draw background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, width - shadowSize - 1, height - shadowSize - 1, cornerRadius, cornerRadius);

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width + shadowSize, size.height + shadowSize);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width - shadowSize, height - shadowSize);
    }
}