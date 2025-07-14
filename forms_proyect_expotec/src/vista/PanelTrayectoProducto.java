package vista;

import controlador.ControladorEnvio;
import controlador.ControladorOrden;
import modelo.HistorialEnvio;
import modelo.Orden;
import util.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PanelTrayectoProducto extends JPanel {
    private JComboBox<OrdenItem> cmbOrdenes;
    private JPanel panelVisual, panelInfo;
    private JLabel lblOrderDescription, imgWarehouse, imgStore, imgCustomerHouse;
    private JLabel lblWarehouseStatus, lblStoreStatus, lblCustomerHouseStatus;
    private JProgressBar pbWarehouse, pbStore, pbCustomerHouse;
    private JScrollPane historialScrollPane;
    private JPanel historialDisplayPanel;

    private final ControladorEnvio controladorEnvio = new ControladorEnvio();
    private final ControladorOrden controladorOrden = new ControladorOrden();

    private final Color TEXT_PRIMARY = UIManager.getColor("Label.foreground");
    private final Color BACKGROUND_CARD = UIManager.getColor("Panel.background");
    private final Color BORDER_COLOR = UIManager.getColor("Component.borderColor");

    public PanelTrayectoProducto() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Seguimiento de Mis Órdenes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selectionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), "Seleccione una Orden",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14), TEXT_PRIMARY
        ));

        selectionPanel.add(new JLabel("Mis Órdenes:"));
        cmbOrdenes = new JComboBox<>();
        cmbOrdenes.setPreferredSize(new Dimension(250, 30));
        selectionPanel.add(cmbOrdenes);
        add(selectionPanel, BorderLayout.BEFORE_FIRST_LINE);

        panelVisual = new JPanel(new GridBagLayout());
        panelVisual.setBorder(BorderFactory.createTitledBorder("Estado del Pedido"));
        panelVisual.setBackground(BACKGROUND_CARD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblOrderDescription = new JLabel("Seleccione una orden para ver su progreso");
        lblOrderDescription.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        panelVisual.add(lblOrderDescription, gbc);

        imgWarehouse = new JLabel(loadIcon("/Image/Almacen.png"));
        lblWarehouseStatus = new JLabel("1. En Bodega: Pendiente");
        pbWarehouse = new JProgressBar(0, 100);
        pbWarehouse.setStringPainted(true);

        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; panelVisual.add(imgWarehouse, gbc);
        gbc.gridx = 1; panelVisual.add(lblWarehouseStatus, gbc);
        gbc.gridx = 2; panelVisual.add(pbWarehouse, gbc);

        imgStore = new JLabel(loadIcon("/Image/Entrega.png"));
        lblStoreStatus = new JLabel("2. En reparto: Pendiente");
        pbStore = new JProgressBar(0, 100);
        pbStore.setStringPainted(true);

        gbc.gridy = 2;
        gbc.gridx = 0; panelVisual.add(imgStore, gbc);
        gbc.gridx = 1; panelVisual.add(lblStoreStatus, gbc);
        gbc.gridx = 2; panelVisual.add(pbStore, gbc);

        imgCustomerHouse = new JLabel(loadIcon("/Image/Casa.png"));
        lblCustomerHouseStatus = new JLabel("3. Entregado: Pendiente");
        pbCustomerHouse = new JProgressBar(0, 100);
        pbCustomerHouse.setStringPainted(true);

        gbc.gridy = 3;
        gbc.gridx = 0; panelVisual.add(imgCustomerHouse, gbc);
        gbc.gridx = 1; panelVisual.add(lblCustomerHouseStatus, gbc);
        gbc.gridx = 2; panelVisual.add(pbCustomerHouse, gbc);

        historialDisplayPanel = new JPanel();
        historialDisplayPanel.setLayout(new BoxLayout(historialDisplayPanel, BoxLayout.Y_AXIS));
        historialScrollPane = new JScrollPane(historialDisplayPanel);
        historialScrollPane.setPreferredSize(new Dimension(600, 200));

        panelInfo = new JPanel(new BorderLayout(10, 10));
        panelInfo.add(panelVisual, BorderLayout.NORTH);
        panelInfo.add(historialScrollPane, BorderLayout.CENTER);

        add(panelInfo, BorderLayout.CENTER);

        cmbOrdenes.addActionListener(e -> {
            if (cmbOrdenes.getSelectedItem() instanceof OrdenItem selected) {
                cargarTrayectoOrden(selected.getIdOrden());
            }
        });

        cargarOrdenesUsuario();
    }

    private ImageIcon loadIcon(String path) {
        try {
            ImageIcon original = new ImageIcon(getClass().getResource(path));
            return new ImageIcon(original.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen: " + path);
            return new ImageIcon();
        }
    }

    private void cargarOrdenesUsuario() {
        cmbOrdenes.removeAllItems();
        cmbOrdenes.addItem(new OrdenItem(-1, "--- Seleccione una orden ---"));

        if (!UserSession.isLoggedIn()) return;

        List<Orden> ordenes = controladorOrden.obtenerOrdenesPorUsuario(UserSession.getCurrentUserId());
        for (Orden orden : ordenes) {
            String texto = "Orden #" + orden.getIdOrden() + " - " + orden.getEstado() + " (" + orden.getFechaOrden().toLocalDateTime().toLocalDate() + ")";
            cmbOrdenes.addItem(new OrdenItem(orden.getIdOrden(), texto));
        }
    }

    private void cargarTrayectoOrden(int idOrden) {
        List<HistorialEnvio> historial = controladorEnvio.obtenerHistorialEnvioPorOrden(idOrden);
        historialDisplayPanel.removeAll();

        lblOrderDescription.setText("Seguimiento de la Orden #" + idOrden);

        Orden orden = controladorOrden.obtenerOrdenPorId(idOrden);
        if (orden != null) {
            Duration diff = Duration.between(orden.getFechaOrden().toLocalDateTime(), LocalDateTime.now());
            double horas = diff.toMinutes() / 60.0;

            int bodega = 0, entrega = 0, cliente = 0;
            if (horas < 1) {
                bodega = (int)((horas / 1.0) * 100);
            } else if (horas < 3) {
                bodega = 100;
                entrega = (int)(((horas - 1) / 2.0) * 100);
            } else {
                bodega = 100;
                entrega = 100;
                cliente = (int)Math.min(((horas - 3) / 1.5) * 100, 100);
            }

            pbWarehouse.setValue(bodega);
            lblWarehouseStatus.setText("1. En Bodega: " + (bodega >= 100 ? "Completado" : "En progreso"));

            pbStore.setValue(entrega);
            lblStoreStatus.setText("2. En reparto: " + (entrega >= 100 ? "En camino" : entrega > 0 ? "En progreso" : "Pendiente"));

            pbCustomerHouse.setValue(cliente);
            lblCustomerHouseStatus.setText("3. Entregado: " + (cliente >= 100 ? "Completado" : cliente > 0 ? "En progreso" : "Pendiente"));
        }

        for (HistorialEnvio evento : historial) {
            JPanel eventoPanel = new JPanel(new GridLayout(0, 1));
            eventoPanel.setBorder(BorderFactory.createTitledBorder(evento.getEstadoOrden()));
            eventoPanel.add(new JLabel("Fecha: " + evento.getFechaHoraFormateada()));
            eventoPanel.add(new JLabel("Ubicación: " + evento.getUbicacionActual()));
            if (evento.getNotas() != null && !evento.getNotas().isEmpty())
                eventoPanel.add(new JLabel("Notas: " + evento.getNotas()));
            historialDisplayPanel.add(eventoPanel);
        }

        historialDisplayPanel.revalidate();
        historialDisplayPanel.repaint();
    }

    public void onUserSessionChange() {
        cargarOrdenesUsuario();
        if (cmbOrdenes.getItemCount() > 0) cmbOrdenes.setSelectedIndex(0);
    }

    private static class OrdenItem {
        private final int idOrden;
        private final String display;

        public OrdenItem(int idOrden, String display) {
            this.idOrden = idOrden;
            this.display = display;
        }

        public int getIdOrden() { return idOrden; }

        @Override
        public String toString() { return display; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrdenItem that = (OrdenItem) o;
            return idOrden == that.idOrden;
        }

        @Override
        public int hashCode() {
            return Objects.hash(idOrden);
        }
    }
}
