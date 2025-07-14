package vista;

import modelo.Producto;
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
        // Es crucial instalar FlatLaf antes de crear cualquier componente Swing
        // Esto generalmente se hace en el método main de tu aplicación principal.
        // Por ejemplo: FlatLaf.setup( new FlatLightLaf() );
        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        // FlatLaf típicamente usa un fondo ligeramente diferente al blanco puro.
        // UIManager.getColor("Panel.background") es una buena opción para respetar el L&F.
        setLayout(new BorderLayout(30, 30));
        setBackground(UIManager.getColor("Panel.background")); // Usa el color de fondo de FlatLaf
        setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Panel de Control", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 46));
        // FlatLaf tiene colores de texto predeterminados que funcionan bien.
        // O puedes definir un color personalizado.
        titleLabel.setForeground(UIManager.getColor("Label.foreground")); // O new Color(50, 55, 100)

        add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // Mantener transparente para ver el fondo del DashboardPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // --- Tarjetas de Resumen ---

        // Colores FlatLaf recomendados o ajustados para un aspecto moderno y suave
        // FlatLaf.light.background, FlatLaf.light.info, FlatLaf.light.border, etc.
        // Aquí ajustamos tus colores existentes para un look más "suave" si lo deseas,
        // o puedes usar UIManager.getColor("Panel.background") para los RoundedPanels si quieres un color uniforme.

        RoundedPanel totalVentasCard = new RoundedPanel(new Color(230, 240, 255), 20); // Azul claro suave
        totalVentasCard.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        JLabel totalVentasTitle = new JLabel("Productos Vendidos", SwingConstants.CENTER);
        totalVentasTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalVentasTitle.setForeground(UIManager.getColor("Panel.foreground")); // O un color personalizado
        totalVentasCard.add(totalVentasTitle);
        lblTotalVentas = new JLabel("0", SwingConstants.CENTER);
        lblTotalVentas.setFont(new Font("Segoe UI", Font.BOLD, 68));
        lblTotalVentas.setForeground(UIManager.getColor("Actions.Blue")); // Un azul FlatLaf
        totalVentasCard.add(lblTotalVentas);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0.4;
        contentPanel.add(totalVentasCard, gbc);

        RoundedPanel ingresosCard = new RoundedPanel(new Color(230, 255, 230), 20); // Verde claro suave
        ingresosCard.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        JLabel ingresosTitle = new JLabel("Ingresos Totales", SwingConstants.CENTER);
        ingresosTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        ingresosTitle.setForeground(UIManager.getColor("Panel.foreground")); // O un color personalizado
        ingresosCard.add(ingresosTitle);
        lblIngresosTotales = new JLabel("$0.00", SwingConstants.CENTER);
        lblIngresosTotales.setFont(new Font("Segoe UI", Font.BOLD, 68));
        lblIngresosTotales.setForeground(UIManager.getColor("Actions.Green")); // Un verde FlatLaf
        ingresosCard.add(lblIngresosTotales);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 0.4;
        contentPanel.add(ingresosCard, gbc);

        RoundedPanel topProductsPanel = new RoundedPanel(new Color(255, 240, 230), 20); // Naranja claro suave
        topProductsPanel.setLayout(new BorderLayout(20, 20));
        JLabel topProductsTitle = new JLabel("Top Productos Vendidos", SwingConstants.CENTER);
        topProductsTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        topProductsTitle.setForeground(UIManager.getColor("Panel.foreground")); // O un color personalizado
        topProductsTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        topProductsPanel.add(topProductsTitle, BorderLayout.NORTH);

        topProductsArea = new JTextArea();
        topProductsArea.setEditable(false);
        topProductsArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        topProductsArea.setBackground(UIManager.getColor("EditorPane.background")); // Usa el color de fondo de texto de FlatLaf
        topProductsArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topProductsArea.setLineWrap(true);
        topProductsArea.setWrapStyleWord(true);
        JScrollPane scrollTopProducts = new JScrollPane(topProductsArea);
        scrollTopProducts.setBorder(BorderFactory.createEmptyBorder()); // Elimina el borde del JScrollPane
        // El viewport background también debe ser el color de FlatLaf para la consistencia
        scrollTopProducts.getViewport().setBackground(topProductsArea.getBackground());
        topProductsPanel.add(scrollTopProducts, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.6;
        contentPanel.add(topProductsPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // --- Panel de Gráficos ---
        // Para el chartPanel, ya tienes un RoundedPanel, lo cual es genial.
        // Asegurarse de que el color de fondo sea coherente con FlatLaf.
        chartPanel = new RoundedPanel(UIManager.getColor("Component.background"), 20) { // Usa el color de fondo de componente de FlatLaf
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
                    g2d.setColor(UIManager.getColor("Label.disabledForeground")); // Color de texto deshabilitado de FlatLaf
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
                int chartHeight = baseY - (chartPadding + 40);

                g2d.setColor(UIManager.getColor("Label.foreground")); // Color de texto principal de FlatLaf
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
                String chartTitleText = "Ingresos Mensuales";
                FontMetrics fm = g2d.getFontMetrics();
                int titleX = (getWidth() - fm.stringWidth(chartTitleText)) / 2;
                g2d.drawString(chartTitleText, titleX, chartPadding - 10);

                g2d.setColor(UIManager.getColor("Panel.light")); // Un color claro para las líneas de la cuadrícula
                g2d.setStroke(new BasicStroke(1));
                int numYLabels = 5;
                for (int i = 0; i <= numYLabels; i++) {
                    int y = baseY - (i * chartHeight / numYLabels);
                    g2d.drawLine(chartPadding - 10, y, getWidth() - chartPadding + 10, y);
                    BigDecimal value = maxSales.multiply(new BigDecimal(i)).divide(new BigDecimal(numYLabels), 2, RoundingMode.HALF_UP);
                    g2d.setColor(UIManager.getColor("Label.disabledForeground")); // Un color de texto más suave para las etiquetas del eje
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    String label = "$" + value.toPlainString();
                    g2d.drawString(label, chartPadding - 15 - fm.stringWidth(label), y + fm.getAscent() / 2);
                }

                g2d.setColor(UIManager.getColor("Panel.light")); // Color para los ejes
                g2d.drawLine(chartPadding - 10, chartPadding - 20, chartPadding - 10, baseY);
                g2d.drawLine(chartPadding - 10, baseY, getWidth() - chartPadding + 10, baseY);

                for (int i = 0; i < sales.size(); i++) {
                    BigDecimal currentSale = sales.get(i);
                    int barActualHeight = currentSale.divide(maxSales, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(chartHeight)).intValue();
                    if (barActualHeight == 0 && currentSale.compareTo(BigDecimal.ZERO) > 0) barActualHeight = 2;

                    // Colores de degradado FlatLaf-friendly
                    Color barStartColor = UIManager.getColor("Actions.Blue"); // Azul FlatLaf
                    Color barEndColor = UIManager.getColor("Component.accentColor"); // Color de acento de FlatLaf
                    if (barStartColor == null) barStartColor = new Color(150, 200, 255); // Fallback
                    if (barEndColor == null) barEndColor = new Color(50, 150, 255); // Fallback

                    GradientPaint gp = new GradientPaint(startX + i * (barWidth + actualGap), baseY - barActualHeight, barStartColor, startX + i * (barWidth + actualGap), baseY, barEndColor);
                    g2d.setPaint(gp);
                    g2d.fillRect(startX + i * (barWidth + actualGap), baseY - barActualHeight, barWidth, barActualHeight);

                    g2d.setColor(UIManager.getColor("Label.foreground")); // Texto de valor de barra
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    String valueStr = "$" + currentSale.setScale(2, RoundingMode.HALF_UP).toPlainString();
                    int valueX = startX + i * (barWidth + actualGap) + (barWidth - fm.stringWidth(valueStr)) / 2;
                    g2d.drawString(valueStr, valueX, baseY - barActualHeight - 8);

                    g2d.setColor(UIManager.getColor("Label.disabledForeground")); // Texto de período de barra
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String periodStr = periods.get(i);
                    int periodX = startX + i * (barWidth + actualGap) + (barWidth - fm.stringWidth(periodStr)) / 2;
                    g2d.drawString(periodStr, periodX, baseY + 20);
                }
                g2d.dispose();
            }
        };
        chartPanel.setPreferredSize(new Dimension(700, 400));
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

            monthlySalesData.clear();
            // Para FlatLaf, asegúrate de que el formato de fecha sea compatible con tu base de datos
            // y que los nombres de los meses sean consistentes.
            // PostgreSQL TO_CHAR('Mon') da las abreviaturas locales (ej. 'Jul' para julio).
            for (int i = 0; i < 6; i++) {
                LocalDate date = LocalDate.now().minusMonths(5 - i);
                String monthAbbr = date.format(DateTimeFormatter.ofPattern("MMM")); // Ej. "jul.", "ago."
                // Si tu base de datos usa "Jul", "Aug", etc., ajusta el formato aquí para que coincida exactamente
                monthlySalesData.put(monthAbbr.replace(".", ""), BigDecimal.ZERO); // Eliminar el punto si TO_CHAR no lo incluye
            }

            String monthlySalesSql = "SELECT TO_CHAR(o.fecha_orden, 'Mon') AS mes_anio, " + // 'Mon' da la abreviatura del mes
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
                // Asegúrate de que la clave (nombre del mes) coincida con lo que esperas de la base de datos
                // y con lo que pones en monthlySalesData.put en la inicialización de los 6 meses.
                String mesAnioDB = rsMonthlySales.getString("mes_anio").trim().replace(".", ""); // Quita espacios y puntos si los hay
                monthlySalesData.put(mesAnioDB, rsMonthlySales.getBigDecimal("ingresos_mes"));
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
    private int shadowSize = 8; // Aumentar ligeramente el tamaño de la sombra para FlatLaf
    private Color shadowColor = new Color(0, 0, 0, 30); // Sombra un poco más oscura y visible

    public RoundedPanel(Color bgColor, int radius) {
        this.backgroundColor = bgColor;
        this.cornerRadius = radius;
        setOpaque(false); // Esencial para que el fondo del componente padre y la sombra se dibujen
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Asegura que el JComponent base se pinte (incluyendo el fondo del padre)
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Dibuja la sombra primero. La sombra debe estar "detrás" del panel principal.
        // Ajusta las coordenadas para que la sombra se dibuje desplazada y el panel principal en (0,0)
        g2d.setColor(shadowColor);
        // La sombra se dibuja fuera del área del panel principal.
        // El panel principal ocupará (0,0) a (width - shadowSize - 1, height - shadowSize - 1)
        // La sombra se extenderá más allá.
        g2d.fillRoundRect(shadowSize / 2, shadowSize / 2, width - shadowSize, height - shadowSize, cornerRadius, cornerRadius);
        // Si el panel es arrastrable, las sombras pueden tener un efecto mejor si se aplican a un borde
        // o si el panel padre es el que gestiona las sombras para todos sus hijos.
        // Para FlatLaf, a menudo es mejor usar la propiedad "Component.shadow" para los bordes si quieres un efecto nativo.
        // Pero dado que ya tienes tu propia implementación de sombra, la mantendremos.

        // Dibuja el fondo del panel redondeado
        g2d.setColor(backgroundColor);
        // Dibuja el panel principal un poco más pequeño para que la sombra se vea "detrás"
        g2d.fillRoundRect(0, 0, width - shadowSize, height - shadowSize, cornerRadius, cornerRadius);

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        // Ajusta el tamaño preferido para incluir el espacio de la sombra
        return new Dimension(size.width + shadowSize, size.height + shadowSize);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
      
        super.setBounds(x, y, width, height);
    }
}