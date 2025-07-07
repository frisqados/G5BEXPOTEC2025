package vista;

import controlador.conexion;
import modelo.Producto;
import util.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReseñasPanel extends JPanel {

    private Producto producto;
    private JTextArea reseñaTextArea;
    private JButton enviarReseñaButton;
    private JPanel reseñasDisplayPanel;
    private JScrollPane reseñasScrollPane;

    public ReseñasPanel(Producto producto) {
        this.producto = producto;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), "Reseñas de Usuarios", 
                                                    TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 16), new Color(50, 50, 50)));
        setBackground(new Color(245, 245, 245));

        JPanel addReviewPanel = new JPanel(new BorderLayout(5, 5));
        addReviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addReviewPanel.setBackground(new Color(245, 245, 245));

        reseñaTextArea = new JTextArea(5, 40);
        reseñaTextArea.setLineWrap(true);
        reseñaTextArea.setWrapStyleWord(true);
        reseñaTextArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        reseñaTextArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollPaneTextArea = new JScrollPane(reseñaTextArea);
        scrollPaneTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneTextArea.setPreferredSize(new Dimension(scrollPaneTextArea.getPreferredSize().width, 80));

        JLabel writeReviewLabel = new JLabel("Escribe tu reseña (máx. 500 caracteres):");
        writeReviewLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        addReviewPanel.add(writeReviewLabel, BorderLayout.NORTH);
        addReviewPanel.add(scrollPaneTextArea, BorderLayout.CENTER);

        enviarReseñaButton = new JButton("Enviar Reseña");
        enviarReseñaButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        enviarReseñaButton.setBackground(new Color(30, 144, 255));
        enviarReseñaButton.setForeground(Color.WHITE);
        enviarReseñaButton.setFocusPainted(false);
        enviarReseñaButton.setBorderPainted(false);
        enviarReseñaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        enviarReseñaButton.addActionListener(e -> enviarReseña());
        addReviewPanel.add(enviarReseñaButton, BorderLayout.SOUTH);

        add(addReviewPanel, BorderLayout.NORTH);

        reseñasDisplayPanel = new JPanel();
        reseñasDisplayPanel.setLayout(new BoxLayout(reseñasDisplayPanel, BoxLayout.Y_AXIS));
        reseñasDisplayPanel.setBackground(Color.WHITE);
        reseñasDisplayPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        reseñasScrollPane = new JScrollPane(reseñasDisplayPanel);
        reseñasScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        reseñasScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        reseñasScrollPane.setPreferredSize(new Dimension(600, 300)); 
        reseñasScrollPane.setMinimumSize(new Dimension(400, 200));

        add(reseñasScrollPane, BorderLayout.CENTER);

        cargarReseñas();
    }

    private void enviarReseña() {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Debes iniciar sesión para enviar una reseña.", "Inicio de Sesión Requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String textoReseña = reseñaTextArea.getText().trim();
        if (textoReseña.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, escribe tu reseña antes de enviarla.", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (textoReseña.length() < 10) {
            JOptionPane.showMessageDialog(this, "La reseña es demasiado corta. Por favor, sé más descriptivo (mínimo 10 caracteres).", "Reseña Corta", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (textoReseña.length() > 500) {
            JOptionPane.showMessageDialog(this, "La reseña es demasiado larga (máximo 500 caracteres).", "Reseña Larga", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Error de conexión a la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String insertReseñaSql = "INSERT INTO reseñas (id_producto, id_usuario, texto_reseña) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(insertReseñaSql);
            ps.setInt(1, producto.getId());
            ps.setInt(2, UserSession.getCurrentUserId());
            ps.setString(3, textoReseña);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Reseña enviada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                reseñaTextArea.setText("");
                cargarReseñas();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo enviar la reseña.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al enviar reseña: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
        }
    }

    public void cargarReseñas() {
        reseñasDisplayPanel.removeAll();
        reseñasDisplayPanel.revalidate(); 
        reseñasDisplayPanel.repaint();

        Connection con = null;
        try {
            con = new conexion().getConnection();
            if (con == null) {
                reseñasDisplayPanel.add(createErrorLabel("Error al cargar reseñas: No se pudo conectar a la base de datos."));
                return;
            }

            String selectReseñasSql = "SELECT r.texto_reseña, r.fecha_reseña, u.nombre, u.apellido " +
                                      "FROM reseñas r " +
                                      "JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                                      "WHERE r.id_producto = ? " +
                                      "ORDER BY r.fecha_reseña DESC";
            PreparedStatement ps = con.prepareStatement(selectReseñasSql);
            ps.setInt(1, producto.getId());
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                reseñasDisplayPanel.add(createNoReviewsLabel());
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                while (rs.next()) {
                    String textoReseña = rs.getString("texto_reseña");
                    String nombreUsuario = rs.getString("nombre") + " " + rs.getString("apellido");
                    Date fechaReseña = rs.getTimestamp("fecha_reseña");

                    JPanel reseñaItemPanel = new JPanel();
                    reseñaItemPanel.setLayout(new BorderLayout());
                    reseñaItemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                        new EmptyBorder(12, 12, 12, 12)
                    ));
                    reseñaItemPanel.setBackground(Color.WHITE);
                    reseñaItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    reseñaItemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, reseñaItemPanel.getPreferredSize().height));


                    JLabel headerLabel = new JLabel("<html><b>" + nombreUsuario + "</b> <span style='font-size:10px; color:gray;'>(" + dateFormat.format(fechaReseña) + ")</span></html>");
                    headerLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
                    headerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
                    reseñaItemPanel.add(headerLabel, BorderLayout.NORTH);

                    JTextArea reseñaContent = new JTextArea(textoReseña);
                    reseñaContent.setEditable(false);
                    reseñaContent.setLineWrap(true);
                    reseñaContent.setWrapStyleWord(true);
                    reseñaContent.setBackground(Color.WHITE);
                    reseñaContent.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    
                    JScrollPane contentScrollPane = new JScrollPane(reseñaContent);
                    contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
                    contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    
                    int preferredHeight = reseñaContent.getPreferredSize().height;
                    contentScrollPane.setPreferredSize(new Dimension(reseñaContent.getPreferredSize().width, Math.min(preferredHeight, 100)));
                    contentScrollPane.setMinimumSize(new Dimension(100, 50));
                    contentScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

                    reseñaItemPanel.add(contentScrollPane, BorderLayout.CENTER);
                    
                    reseñasDisplayPanel.add(reseñaItemPanel);
                    reseñasDisplayPanel.add(Box.createVerticalStrut(15));
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al cargar reseñas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            reseñasDisplayPanel.add(createErrorLabel("Error al cargar reseñas: " + ex.getMessage()));
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException finalEx) {
                System.err.println("Error al cerrar conexión: " + finalEx.getMessage());
            }
            reseñasDisplayPanel.revalidate();
            reseñasDisplayPanel.repaint();
            reseñasScrollPane.revalidate();
            reseñasScrollPane.repaint();
            SwingUtilities.invokeLater(() -> reseñasScrollPane.getVerticalScrollBar().setValue(0));
        }
    }

    private JLabel createErrorLabel(String message) {
        JLabel errorLabel = new JLabel("<html><p style='color:red; text-align:center;'>" + message + "</p></html>");
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return errorLabel;
    }

    private JLabel createNoReviewsLabel() {
        JLabel noReviewsLabel = new JLabel("<html><p style='text-align:center;'>No hay reseñas para este producto todavía. <br>¡Sé el primero en dejar una!</p></html>");
        noReviewsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noReviewsLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        noReviewsLabel.setForeground(Color.GRAY);
        noReviewsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return noReviewsLabel;
    }
}