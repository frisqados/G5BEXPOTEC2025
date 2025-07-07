package forms_proyect_expotec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.border.EmptyBorder;

import util.UserSession;
import util.ProductoSeleccionadoListener;
import modelo.Producto;

import vista.IngresoProductoPanel;
import vista.MostrarListaDeseosPanel;
import vista.MostrarProductosPanel;
import vista.DetallesProductoPanel;
import vista.CarritoPanel;
import vista.DashboardPanel;
import vista.PerfilPanel;
// Importa el nuevo panel de Historial de Compras
import forms_proyect_expotec.HistorialComprasPanel;

public class PrincipalForm extends JFrame implements ProductoSeleccionadoListener {

    private JPanel panelSuperior;
    private JPanel panelMenuToggleBar;
    private JPanel panelMenuDesplegable;
    private JButton btnMenuToggle;
    private boolean menuDesplegado = false;
    private final int ALTURA_MENU_DESPLEGADO = 60;
    private Timer timerMenu;

    private JLabel lblUsuarioActual;
    private JButton btnCerrarSesion;

    private JButton btnProductos;
    private JButton btnListaDeseos;
    private JButton btnIngresarProductos;
    private JButton btnPerfil;
    private JButton btnCarrito;
    private JButton btnDashboard;
    private JButton btnHistorialCompras; // ¡NUEVO: Botón para Historial de Compras!

    private JPanel panelPrincipalContent;
    private CardLayout cardLayout;

    private MostrarProductosPanel mostrarProductosPanelInstance;
    private MostrarListaDeseosPanel mostrarListaDeseosPanelInstance;
    private IngresoProductoPanel ingresoProductoPanelInstance;
    private CarritoPanel carritoPanelInstance;
    private DashboardPanel dashboardPanelInstance;
    private PerfilPanel perfilPanelInstance;
    private HistorialComprasPanel historialComprasPanelInstance; // ¡NUEVO: Instancia del HistorialComprasPanel!

    public PrincipalForm() {
        setTitle("Aplicación Principal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UserSession.logout();
                System.out.println("Sesión de usuario limpiada al cerrar la aplicación.");
            }
        });

        // --- Panel Superior (Negro) ---
        panelSuperior = new JPanel();
        panelSuperior.setBackground(Color.BLACK);
        panelSuperior.setPreferredSize(new Dimension(getWidth(), 60));
        add(panelSuperior, BorderLayout.NORTH);
        panelSuperior.setLayout(new GridBagLayout());

        GridBagConstraints gbcSuperior = new GridBagConstraints();
        gbcSuperior.insets = new Insets(5, 10, 5, 10);

        ImageIcon logoIcon = new ImageIcon(new ImageIcon("src/Image/logo.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JLabel lblLogo = new JLabel(logoIcon);
        gbcSuperior.gridx = 0; gbcSuperior.gridy = 0; gbcSuperior.anchor = GridBagConstraints.WEST;
        panelSuperior.add(lblLogo, gbcSuperior);

        lblUsuarioActual = new JLabel("Bienvenido: " + (UserSession.getCurrentUserName() != null ? UserSession.getCurrentUserName() : "Invitado"));
        lblUsuarioActual.setForeground(Color.WHITE);
        lblUsuarioActual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbcSuperior.gridx = 1; gbcSuperior.weightx = 1.0; gbcSuperior.anchor = GridBagConstraints.WEST;
        panelSuperior.add(lblUsuarioActual, gbcSuperior);

        btnCerrarSesion = crearBotonEstiloMenu("Cerrar Sesión");
        btnCerrarSesion.setBackground(new Color(200, 0, 0));
        btnCerrarSesion.addActionListener(e -> {
            UserSession.logout();
            JOptionPane.showMessageDialog(this, "Sesión cerrada. ¡Hasta pronto!", "Sesión Cerrada", JOptionPane.INFORMATION_MESSAGE);
            new LoginForm().setVisible(true);
            dispose();
        });
        gbcSuperior.gridx = 2; gbcSuperior.weightx = 0; gbcSuperior.anchor = GridBagConstraints.EAST;
        panelSuperior.add(btnCerrarSesion, gbcSuperior);

        // --- Panel de Barra de Menú (Toggle) ---
        panelMenuToggleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelMenuToggleBar.setBackground(new Color(40, 40, 40));
        panelMenuToggleBar.setPreferredSize(new Dimension(getWidth(), 30));

        btnMenuToggle = new JButton(" MENÚ ▼ ");
        btnMenuToggle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnMenuToggle.setForeground(Color.WHITE);
        btnMenuToggle.setBackground(new Color(60, 63, 65));
        btnMenuToggle.setFocusPainted(false);
        btnMenuToggle.setBorderPainted(false);
        btnMenuToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMenuToggle.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnMenuToggle.addActionListener(e -> toggleMenu());

        panelMenuToggleBar.add(btnMenuToggle);

        JPanel mainContentWrapper = new JPanel(new BorderLayout());
        mainContentWrapper.add(panelMenuToggleBar, BorderLayout.NORTH);

        // --- Panel de Contenido Principal (CardLayout) ---
        cardLayout = new CardLayout();
        panelPrincipalContent = new JPanel(cardLayout);
        panelPrincipalContent.setBackground(new Color(240, 240, 240));
        panelPrincipalContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainContentWrapper.add(panelPrincipalContent, BorderLayout.CENTER);
        add(mainContentWrapper, BorderLayout.CENTER);

        // --- Panel de Menú Desplegable ---
        panelMenuDesplegable = new JPanel();
        panelMenuDesplegable.setBackground(new Color(80, 83, 85));
        panelMenuDesplegable.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelMenuDesplegable.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        panelMenuDesplegable.setVisible(false);

        btnProductos = crearBotonEstiloMenu("Productos");
        btnProductos.addActionListener(e -> {
            showPanel("MostrarProductos");
            if (mostrarProductosPanelInstance != null) {
                mostrarProductosPanelInstance.cargarProductosDesdeBD(null);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnProductos);

        btnListaDeseos = crearBotonEstiloMenu("Lista de Deseos");
        btnListaDeseos.addActionListener(e -> {
            int userId = UserSession.getCurrentUserId();
            if (userId != 0) { // Usamos 0 como indicador de no logueado, según tu UserSession
                if (mostrarListaDeseosPanelInstance != null) {
                    mostrarListaDeseosPanelInstance.refreshWishList();
                }
                showPanel("MostrarListaDeseos");
            } else {
                JOptionPane.showMessageDialog(this, "No se ha iniciado sesión para ver la lista de deseos.", "Error", JOptionPane.WARNING_MESSAGE);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnListaDeseos);

        btnIngresarProductos = crearBotonEstiloMenu("Ingresar Productos");
        btnIngresarProductos.addActionListener(e -> {
            int userId = UserSession.getCurrentUserId();
            if (userId != 0) {
                showPanel("IngresarProductos");
            } else {
                JOptionPane.showMessageDialog(this, "Necesitas iniciar sesión para ingresar productos.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnIngresarProductos);

        btnPerfil = crearBotonEstiloMenu("Perfil");
        btnPerfil.addActionListener(e -> {
            int userId = UserSession.getCurrentUserId();
            if (userId != 0) {
                if (perfilPanelInstance != null) {
                    perfilPanelInstance.refreshData();
                }
                showPanel("Perfil");
            } else {
                JOptionPane.showMessageDialog(this, "Necesitas iniciar sesión para ver tu perfil.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnPerfil);

        btnCarrito = crearBotonEstiloMenu("Carrito");
        btnCarrito.addActionListener(e -> {
            int userId = UserSession.getCurrentUserId();
            if (userId != 0) {
                showPanel("Carrito");
                if (carritoPanelInstance != null) {
                    carritoPanelInstance.refreshCartDisplay();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Necesitas iniciar sesión para ver tu carrito.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnCarrito);

        btnDashboard = crearBotonEstiloMenu("Mi Dashboard de Ventas");
        btnDashboard.addActionListener(e -> {
            int userId = UserSession.getCurrentUserId();
            if (userId != 0) {
                showPanel("Dashboard");
                if (dashboardPanelInstance != null) {
                    dashboardPanelInstance.refreshData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Necesitas iniciar sesión para ver tu Dashboard de Ventas.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnDashboard);

        // --- ¡NUEVO: Botón para Historial de Compras! ---
        btnHistorialCompras = crearBotonEstiloMenu("Historial de Compras");
        btnHistorialCompras.addActionListener(e -> {
            int userId = UserSession.getCurrentUserId();
            if (userId != 0) {
                // Instanciar HistorialComprasPanel aquí si aún no se ha hecho
                // Esto es importante si el panel necesita ser refrescado cada vez que se abre
                // o si hay datos dependientes del usuario que se cargan en el constructor.
                // En este caso, ya lo instanciamos abajo, solo aseguramos que se muestre.
                showPanel("HistorialCompras");
                // Si tu HistorialComprasPanel tiene un método para refrescar datos, llámalo aquí:
                // if (historialComprasPanelInstance != null) {
                //     historialComprasPanelInstance.cargarHistorialCompras(); // Si existe un método público
                // }
            } else {
                JOptionPane.showMessageDialog(this, "Necesitas iniciar sesión para ver tu Historial de Compras.", "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
            }
            toggleMenu();
        });
        panelMenuDesplegable.add(btnHistorialCompras); // Añadir el botón al panel desplegable

        getLayeredPane().add(panelMenuDesplegable, JLayeredPane.POPUP_LAYER);
        panelMenuDesplegable.setBounds(0, panelSuperior.getPreferredSize().height + panelMenuToggleBar.getPreferredSize().height, getWidth(), 0);
        panelMenuDesplegable.revalidate();
        panelMenuDesplegable.repaint();

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (menuDesplegado) {
                    panelMenuDesplegable.setBounds(0, panelSuperior.getPreferredSize().height + panelMenuToggleBar.getPreferredSize().height, getWidth(), ALTURA_MENU_DESPLEGADO);
                } else {
                    panelMenuDesplegable.setBounds(0, panelSuperior.getPreferredSize().height + panelMenuToggleBar.getPreferredSize().height, getWidth(), 0);
                }
            }
        });

        // --- Inicializar y añadir todos los paneles al CardLayout ---
        mostrarProductosPanelInstance = new MostrarProductosPanel(this);
        panelPrincipalContent.add(mostrarProductosPanelInstance, "MostrarProductos");

        mostrarListaDeseosPanelInstance = new MostrarListaDeseosPanel(UserSession.getCurrentUserId(), this);
        panelPrincipalContent.add(mostrarListaDeseosPanelInstance, "MostrarListaDeseos");

        ingresoProductoPanelInstance = new IngresoProductoPanel();
        panelPrincipalContent.add(ingresoProductoPanelInstance, "IngresarProductos");

        carritoPanelInstance = new CarritoPanel();
        panelPrincipalContent.add(carritoPanelInstance, "Carrito");

        dashboardPanelInstance = new DashboardPanel();
        panelPrincipalContent.add(dashboardPanelInstance, "Dashboard");

        perfilPanelInstance = new PerfilPanel();
        panelPrincipalContent.add(perfilPanelInstance, "Perfil");

        // ¡NUEVO: Inicializar y añadir el HistorialComprasPanel!
        historialComprasPanelInstance = new HistorialComprasPanel();
        panelPrincipalContent.add(historialComprasPanelInstance, "HistorialCompras"); // Asocia el panel con la clave "HistorialCompras"

        // --- Mostrar el panel de productos inmediatamente al inicio ---
        showPanel("MostrarProductos");

        setVisible(true);
    }

    private JButton crearBotonEstiloMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(60, 63, 65));
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(90, 93, 95));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(60, 63, 65));
            }
        });
        return boton;
    }

    private void toggleMenu() {
        menuDesplegado = !menuDesplegado;
        int inicioAltura = panelMenuDesplegable.getHeight();
        int finAltura = menuDesplegado ? ALTURA_MENU_DESPLEGADO : 0;
        int deltaAltura = finAltura - inicioAltura;
        int pasos = 15;
        int duracionPorPaso = 15;

        btnMenuToggle.setText(menuDesplegado ? " MENÚ ▲ " : " MENÚ ▼ ");

        if (timerMenu != null && timerMenu.isRunning()) {
            timerMenu.stop();
        }

        timerMenu = new Timer(duracionPorPaso, new ActionListener() {
            int currentStep = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                currentStep++;
                float progress = (float) currentStep / pasos;
                if (progress > 1.0f) {
                    progress = 1.0f;
                }

                int animatedHeight = inicioAltura + (int) (deltaAltura * progress);

                if (currentStep == pasos) {
                    animatedHeight = finAltura;
                    timerMenu.stop();
                }

                panelMenuDesplegable.setBounds(0, panelSuperior.getPreferredSize().height + panelMenuToggleBar.getPreferredSize().height, getWidth(), animatedHeight);
                panelMenuDesplegable.revalidate();
                panelMenuDesplegable.repaint();

                if (!menuDesplegado && currentStep == pasos) {
                    panelMenuDesplegable.setVisible(false);
                } else if (menuDesplegado && currentStep == 1) {
                    panelMenuDesplegable.setVisible(true);
                }
            }
        });
        timerMenu.start();
    }

    public void showPanel(String panelName) {
        cardLayout.show(panelPrincipalContent, panelName);
        panelPrincipalContent.revalidate();
        panelPrincipalContent.repaint();
    }

    @Override
    public void onProductoSeleccionado(Producto producto) {
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String cardName = "Detalles_" + producto.getId();

        Component existingPanel = getComponentByName(panelPrincipalContent, cardName);
        if (existingPanel != null) {
            panelPrincipalContent.remove(existingPanel);
        }

        DetallesProductoPanel detallesPanel = new DetallesProductoPanel(producto, carritoPanelInstance, this);
        detallesPanel.setName(cardName);

        panelPrincipalContent.add(detallesPanel, cardName);
        cardLayout.show(panelPrincipalContent, cardName);

        if (menuDesplegado) {
            toggleMenu();
        }
    }

    @Override
    public void onProductoRemovidoDeListaDeseos(Producto producto) {
        System.out.println("PrincipalForm: Notificación de que el producto con ID " + producto.getId() + " fue removido de la lista de deseos.");
    }

    @Override
    public void volverAlCatalogo() {
        showPanel("MostrarProductos");
        if (mostrarProductosPanelInstance != null) {
            mostrarProductosPanelInstance.cargarProductosDesdeBD(null);
        }
    }

    private Component getComponentByName(JPanel parent, String name) {
        for (Component comp : parent.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                return comp;
            }
        }
        return null;
    }
}