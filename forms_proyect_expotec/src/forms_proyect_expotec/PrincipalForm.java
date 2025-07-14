package forms_proyect_expotec;

import com.formdev.flatlaf.FlatDarkLaf;
import modelo.Producto;
import util.UserSession;
import util.ProductoSeleccionadoListener;
import vista.*; // Importa todos los paneles de vista
import vista.botonHamburger; // Asegúrate de que esta clase esté en el paquete 'vista'

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class PrincipalForm extends JFrame implements ProductoSeleccionadoListener {

    // --- Constantes para mejorar la legibilidad y mantenimiento ---
    private static final int MENU_ANIMATION_STEP = 10;
    private static final int MENU_ANIMATION_DELAY = 10;
    private static final int MENU_OPEN_WIDTH = 200;
    private static final int TOP_PANEL_HEIGHT = 60;
    private static final int CONTENT_PADDING = 20;

    // --- Componentes de la UI ---
    private JPanel panelSuperior;
    private JPanel panelMenuLateral;
    private JButton btnMenuToggle;
    private JLabel lblUsuarioActual;
    private JButton btnCerrarSesion;

    // Mapa para almacenar los botones del menú y acceder a ellos fácilmente
    private Map<String, JButton> menuButtons;

    private JPanel panelPrincipalContent;
    private CardLayout cardLayout;

    // Instancias de los paneles (se inicializan en el constructor)
    private MostrarProductosPanel mostrarProductosPanelInstance;
    private MostrarListaDeseosPanel mostrarListaDeseosPanelInstance;
    private IngresoProductoPanel ingresoProductoPanelInstance;
    private EditarProductoPanel editarProductoPanelInstance;
    private CarritoPanel carritoPanelInstance;
    private DashboardPanel dashboardPanelInstance;
    private PerfilPanel perfilPanelInstance;
    private HistorialComprasPanel historialComprasPanelInstance;
    private PanelTrayectoProducto panelTrayectoProductoInstance;
    private ProductosRecientesPanel productosRecientesPanelInstance;
    
    // --- Estado de la UI ---
    private boolean menuDesplegado = false;
    private Timer timerMenu;

    public PrincipalForm() {
        initLookAndFeel();
        setupJFrame();
        setupWindowListener();

        // Inicializar componentes principales
        setupTopPanel();
        setupSideMenuPanel();
        setupMainContentPanel();
        initPanels(); // Cargar e inicializar todos los paneles de contenido
        addPanelsToContent(); // Añadir los paneles al CardLayout

        // Estado inicial de la UI
        showPanel("MostrarProductos"); // Panel por defecto al iniciar
        actualizarEstadoUsuarioGUI(); // Ajustar visibilidad de elementos según la sesión

        setVisible(true);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Error al configurar el Look and Feel: " + e.getMessage());
            // Considera usar un JOptionPane para informar al usuario si esto es crítico
        }
    }

    private void setupJFrame() {
        setTitle("Aplicación Principal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en la pantalla
        setLayout(new BorderLayout());
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UserSession.logout(); // Limpiar sesión al cerrar la aplicación
                System.out.println("Sesión de usuario limpiada al cerrar la aplicación.");
            }
        });
    }

    private void setupTopPanel() {
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setPreferredSize(new Dimension(getWidth(), TOP_PANEL_HEIGHT));
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelLogoMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panelLogoMenu.setOpaque(false); // Permite que el color de fondo del padre se vea

        btnMenuToggle = new botonHamburger(); // Asume que botonHamburger es una subclase de JButton
        btnMenuToggle.setForeground(Color.WHITE); // Ajustar color según el tema FlatLaf
        btnMenuToggle.addActionListener(e -> toggleMenu());
        panelLogoMenu.add(btnMenuToggle);

        loadAndAddLogo(panelLogoMenu); // Método para cargar y añadir el logo
        panelSuperior.add(panelLogoMenu, BorderLayout.WEST);

        JPanel panelUsuarioCerrar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelUsuarioCerrar.setOpaque(false);

        lblUsuarioActual = new JLabel();
        lblUsuarioActual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelUsuarioCerrar.add(lblUsuarioActual);

        btnCerrarSesion = new JButton();
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.addActionListener(e -> handleLoginLogout());
        panelUsuarioCerrar.add(btnCerrarSesion);

        panelSuperior.add(panelUsuarioCerrar, BorderLayout.EAST);
    }

    private void loadAndAddLogo(JPanel parentPanel) {
        try {
            // Asegúrate de que la ruta del recurso sea correcta, "/Image/logo.png"
            ImageIcon logoIcon = new ImageIcon(new ImageIcon(getClass().getResource("/Image/logo.png"))
                    .getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            JLabel lblLogo = new JLabel(logoIcon);
            parentPanel.add(lblLogo);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen del logo: " + e.getMessage());
            // Mostrar un placeholder si la imagen no carga
            JLabel lblLogoPlaceholder = new JLabel("Logo");
            lblLogoPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 20));
            parentPanel.add(lblLogoPlaceholder);
        }
    }

    private void handleLoginLogout() {
        if (UserSession.isLoggedIn()) {
            UserSession.logout();
            JOptionPane.showMessageDialog(this, "Sesión cerrada. ¡Hasta pronto!", "Sesión Cerrada", JOptionPane.INFORMATION_MESSAGE);
        }
        new LoginForm().setVisible(true); // Redirigir al LoginForm
        dispose(); // Cerrar la ventana principal
    }

    private void setupSideMenuPanel() {
        panelMenuLateral = new JPanel();
        panelMenuLateral.setLayout(new BoxLayout(panelMenuLateral, BoxLayout.Y_AXIS));
        panelMenuLateral.setPreferredSize(new Dimension(0, getHeight())); // Empieza oculto
        panelMenuLateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));
        add(panelMenuLateral, BorderLayout.WEST);

        JLabel tituloMenu = new JLabel("Menú");
        tituloMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tituloMenu.setBorder(new EmptyBorder(15, 15, 15, 15));
        tituloMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelMenuLateral.add(tituloMenu);

        panelMenuLateral.add(Box.createRigidArea(new Dimension(0, 10)));

        initializeMenuButtons(); // Crear y añadir los botones del menú
        addMenuButtonListeners(); // Añadir los listeners a los botones
    }

    private void initializeMenuButtons() {
        menuButtons = new HashMap<>();
        // Las claves del mapa deben coincidir con los nombres de las tarjetas del CardLayout
        addMenuButton("Productos", "MostrarProductos");
        addMenuButton("Lista de Deseos", "MostrarListaDeseos");
        addMenuButton("Vistos Recientemente", "ProductosRecientes");
        addMenuButton("Ingresar Productos", "IngresarProductos");
        addMenuButton("Editar Productos", "EditarProductos");
        addMenuButton("Perfil", "Perfil");
        addMenuButton("Carrito", "Carrito");
        addMenuButton("Mi Dashboard de Ventas", "Dashboard");
        addMenuButton("Historial de Compras", "HistorialCompras");
        addMenuButton("Seguimiento de Pedidos", "SeguimientoPedidos");
    }

    private void addMenuButton(String text, String panelName) {
        JButton button = crearBotonEstiloMenu(text);
        menuButtons.put(panelName, button); // Guarda el botón en el mapa con el nombre del panel (clave)
        panelMenuLateral.add(button);
    }

    private void addMenuButtonListeners() {
        // --- Añadir listeners con verificación de nulidad ---
        JButton button;

        button = menuButtons.get("MostrarProductos");
        if (button != null) {
            button.addActionListener(e -> {
                showPanel("MostrarProductos");
                if (mostrarProductosPanelInstance != null) {
                    mostrarProductosPanelInstance.cargarProductosAsync(null);
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'MostrarProductos' no se encontró en el mapa.");
        }

        button = menuButtons.get("MostrarListaDeseos");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("MostrarListaDeseos");
                    if (mostrarListaDeseosPanelInstance != null) {
                        mostrarListaDeseosPanelInstance.refreshWishList();
                    }
                } else {
                    showAccessDeniedMessage("lista de deseos");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'MostrarListaDeseos' no se encontró en el mapa.");
        }

        button = menuButtons.get("ProductosRecientes");
        if (button != null) {
            button.addActionListener(e -> {
                showPanel("ProductosRecientes");
                if (productosRecientesPanelInstance != null) {
                    productosRecientesPanelInstance.refreshData();
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'ProductosRecientes' no se encontró en el mapa.");
        }
            
        button = menuButtons.get("IngresarProductos");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("IngresarProductos");
                } else {
                    showAccessDeniedMessage("ingresar productos");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'IngresarProductos' no se encontró en el mapa.");
        }

        button = menuButtons.get("EditarProductos");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("EditarProductos");
                    if (editarProductoPanelInstance != null) {
                        editarProductoPanelInstance.refreshData();
                    }
                } else {
                    showAccessDeniedMessage("editar tus productos");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'EditarProductos' no se encontró en el mapa.");
        }

        button = menuButtons.get("Perfil");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("Perfil");
                    if (perfilPanelInstance != null) {
                        perfilPanelInstance.refreshData();
                    }
                } else {
                    showAccessDeniedMessage("ver tu perfil");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'Perfil' no se encontró en el mapa.");
        }

        button = menuButtons.get("Carrito");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("Carrito");
                    if (carritoPanelInstance != null) {
                        carritoPanelInstance.refreshCartDisplay();
                    }
                } else {
                    showAccessDeniedMessage("ver tu carrito");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'Carrito' no se encontró en el mapa.");
        }

        button = menuButtons.get("Dashboard");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) { 
                    showPanel("Dashboard");
                    if (dashboardPanelInstance != null) {
                        dashboardPanelInstance.refreshData();
                    }
                } else {
                    showAccessDeniedMessage("ver tu Dashboard de Ventas");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'Dashboard' no se encontró en el mapa.");
        }

        button = menuButtons.get("HistorialCompras");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("HistorialCompras");
                } else {
                    showAccessDeniedMessage("ver tu Historial de Compras");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'HistorialCompras' no se encontró en el mapa.");
        }

        button = menuButtons.get("SeguimientoPedidos");
        if (button != null) {
            button.addActionListener(e -> {
                if (UserSession.isLoggedIn()) {
                    showPanel("SeguimientoPedidos");
                    if (panelTrayectoProductoInstance != null) {
                        panelTrayectoProductoInstance.onUserSessionChange();
                    }
                } else {
                    showAccessDeniedMessage("rastrear tus pedidos");
                }
                toggleMenu();
            });
        } else {
            System.err.println("Error: El botón para 'SeguimientoPedidos' no se encontró en el mapa.");
        }
    }

    private void showAccessDeniedMessage(String featureName) {
        JOptionPane.showMessageDialog(this,
                "Necesitas iniciar sesión para " + featureName + ".",
                "Acceso Denegado", JOptionPane.WARNING_MESSAGE);
    }

    private void setupMainContentPanel() {
        cardLayout = new CardLayout();
        panelPrincipalContent = new JPanel(cardLayout);
        panelPrincipalContent.setBorder(new EmptyBorder(CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING));
        add(panelPrincipalContent, BorderLayout.CENTER);
    }

    private void initPanels() {
        int currentUserId = UserSession.getCurrentUserId();

        // Es importante pasar `this` (PrincipalForm) como ProductoSeleccionadoListener
        mostrarProductosPanelInstance = new MostrarProductosPanel(this, currentUserId);
        mostrarListaDeseosPanelInstance = new MostrarListaDeseosPanel(currentUserId, this);
        ingresoProductoPanelInstance = new IngresoProductoPanel();
        editarProductoPanelInstance = new EditarProductoPanel();
        carritoPanelInstance = new CarritoPanel();
        dashboardPanelInstance = new DashboardPanel();
        perfilPanelInstance = new PerfilPanel();
        historialComprasPanelInstance = new HistorialComprasPanel();
        panelTrayectoProductoInstance = new PanelTrayectoProducto();
        productosRecientesPanelInstance = new ProductosRecientesPanel(this);
        // productosRecientesPanelInstance.loadSampleProducts(); // Comentado, se carga cuando se muestra el panel.
    }

    private void addPanelsToContent() {
        panelPrincipalContent.add(mostrarProductosPanelInstance, "MostrarProductos");
        panelPrincipalContent.add(mostrarListaDeseosPanelInstance, "MostrarListaDeseos");
        panelPrincipalContent.add(ingresoProductoPanelInstance, "IngresarProductos");
        panelPrincipalContent.add(editarProductoPanelInstance, "EditarProductos");
        panelPrincipalContent.add(carritoPanelInstance, "Carrito");
        panelPrincipalContent.add(dashboardPanelInstance, "Dashboard");
        panelPrincipalContent.add(perfilPanelInstance, "Perfil");
        panelPrincipalContent.add(historialComprasPanelInstance, "HistorialCompras");
        panelPrincipalContent.add(panelTrayectoProductoInstance, "SeguimientoPedidos");
        panelPrincipalContent.add(productosRecientesPanelInstance, "ProductosRecientes");
    }

    // --- Métodos de UI / Componentes ---
    private JButton crearBotonEstiloMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        boton.setOpaque(true);
        boton.setBackground(UIManager.getColor("Button.background"));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(UIManager.getColor("Button.hoverBackground"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(UIManager.getColor("Button.background"));
            }
        });
        return boton;
    }

    private void toggleMenu() {
        if (timerMenu != null && timerMenu.isRunning()) {
            timerMenu.stop();
        }

        int anchoActual = panelMenuLateral.getWidth();
        int anchoObjetivo = menuDesplegado ? 0 : MENU_OPEN_WIDTH;

        menuDesplegado = !menuDesplegado;

        timerMenu = new Timer(MENU_ANIMATION_DELAY, new ActionListener() {
            int currentWidth = anchoActual;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuDesplegado) {
                    currentWidth += MENU_ANIMATION_STEP;
                    if (currentWidth >= anchoObjetivo) {
                        currentWidth = anchoObjetivo;
                        timerMenu.stop();
                    }
                } else {
                    currentWidth -= MENU_ANIMATION_STEP;
                    if (currentWidth <= anchoObjetivo) {
                        currentWidth = anchoObjetivo;
                        timerMenu.stop();
                    }
                }
                panelMenuLateral.setPreferredSize(new Dimension(currentWidth, getHeight()));
                panelMenuLateral.revalidate();
                panelMenuLateral.repaint(); // Añadir repaint para asegurar el refresco visual
            }
        });
        timerMenu.start();
    }

    /**
     * Muestra el panel especificado por su nombre en el CardLayout.
     *
     * @param panelName El nombre de la tarjeta a mostrar.
     */
    public void showPanel(String panelName) {
        cardLayout.show(panelPrincipalContent, panelName);
        panelPrincipalContent.revalidate();
        panelPrincipalContent.repaint();
    }

    /**
     * Actualiza el estado de los componentes de la UI basados en la sesión del usuario.
     */
    private void actualizarEstadoUsuarioGUI() {
        boolean loggedIn = UserSession.isLoggedIn();
        
        lblUsuarioActual.setText(loggedIn ? "Bienvenido: " + UserSession.getCurrentUserName() : "Bienvenido: Invitado");
        btnCerrarSesion.setText(loggedIn ? "Cerrar Sesión" : "Iniciar Sesión");

        // Habilitar/deshabilitar botones generales para usuarios logueados
        JButton button;

        button = menuButtons.get("MostrarListaDeseos");
        if (button != null) button.setEnabled(loggedIn);

        button = menuButtons.get("Perfil");
        if (button != null) button.setEnabled(loggedIn);

        button = menuButtons.get("Carrito");
        if (button != null) button.setEnabled(loggedIn);

        button = menuButtons.get("HistorialCompras");
        if (button != null) button.setEnabled(loggedIn);

        button = menuButtons.get("SeguimientoPedidos");
        if (button != null) button.setEnabled(loggedIn);

        // Productos Recientes siempre disponible para todos
        button = menuButtons.get("ProductosRecientes");
        if (button != null) button.setEnabled(true); 

        // Habilitar/deshabilitar botones de "vendedor" solo si el usuario está logueado
        // Si necesitas un rol específico para vendedor, la lógica debería ir en UserSession
        button = menuButtons.get("IngresarProductos");
        if (button != null) button.setEnabled(loggedIn); 

        button = menuButtons.get("EditarProductos");
        if (button != null) button.setEnabled(loggedIn); 

        button = menuButtons.get("Dashboard");
        if (button != null) button.setEnabled(loggedIn);    
    }

    // --- Implementación de ProductoSeleccionadoListener ---
    @Override
    public void onProductoSeleccionado(Producto producto) {
        if (producto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String cardName = "Detalles_" + producto.getId();

        // Obtener el componente existente si ya está en el CardLayout
        Component existingComponent = getComponentByName(panelPrincipalContent, cardName);

        // Siempre creamos una nueva instancia para el producto, siguiendo la Opción 1
        DetallesProductoPanel detallesPanel = new DetallesProductoPanel(producto, carritoPanelInstance, this);
        detallesPanel.setName(cardName); // Aseguramos que el nuevo panel tenga el nombre correcto

        if (existingComponent != null) {
            // Si el componente ya existe, lo removemos antes de añadir el nuevo.
            // Esto asegura que siempre se muestre una instancia "fresca" del panel de detalles.
            panelPrincipalContent.remove(existingComponent);
        }
        
        // Agregamos el nuevo panel (o lo re-agregamos si ya existía y lo eliminamos)
        panelPrincipalContent.add(detallesPanel, cardName);

        // Mostrar el nuevo panel
        cardLayout.show(panelPrincipalContent, cardName);

        if (productosRecientesPanelInstance != null) {
            productosRecientesPanelInstance.addRecentProduct(producto);
        }

        if (menuDesplegado) {
            toggleMenu();
        }
    }

    @Override
    public void onProductoRemovidoDeListaDeseos(Producto producto) {
        System.out.println("PrincipalForm: Producto removido de lista de deseos: ID " + producto.getId());
    }

    @Override
    public void volverAlCatalogo() {
        showPanel("MostrarProductos");
        if (mostrarProductosPanelInstance != null) {
            mostrarProductosPanelInstance.cargarProductosAsync(null);
        }
    }

  
    private Component getComponentByName(JPanel parent, String name) {
        for (Component comp : parent.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }

    // Método main para iniciar la aplicación (si este es tu punto de entrada)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PrincipalForm::new);
    }
}