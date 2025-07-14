package vista;

import javax.swing.*;
import java.awt.*;

public class botonHamburger extends JButton {
    public botonHamburger() {
        setPreferredSize(new Dimension(40, 40));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Mostrar/Ocultar Menú");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        // Antialiasing para mejor calidad
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Color de las rayas, puedes cambiar aquí si quieres
        g2.setColor(getForeground() != null ? getForeground() : Color.WHITE);

        int ancho = getWidth();
        int alto = getHeight();

        int rayas = 3;
        int espacio = 6; // espacio entre rayas
        int grosor = 3;  // grosor de cada raya
        int longitud = ancho - 16; // longitud de las rayas (margen lateral de 8 px)

        int yInicial = (alto - (rayas * grosor + (rayas -1) * espacio)) / 2;

        for (int i = 0; i < rayas; i++) {
            int y = yInicial + i * (grosor + espacio);
            g2.fillRoundRect(8, y, longitud, grosor, 3, 3);
        }

        g2.dispose();
    }
}