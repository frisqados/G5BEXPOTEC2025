package util; // Asegúrate de que este paquete coincida con la ubicación de tu archivo

import javax.swing.ImageIcon;
import java.awt.Image;

/**
 * Clase de utilidad para el manejo de imágenes, como escalar ImageIcon.
 */
public class ImageUtil {

    /**
     * Crea un ImageIcon escalado a las dimensiones especificadas.
     *
     * @param imageData Un array de bytes que contiene los datos de la imagen (por ejemplo, desde la base de datos).
     * @param width El ancho deseado para la imagen escalada.
     * @param height La altura deseada para la imagen escalada.
     * @return Un ImageIcon escalado, o null si los datos de la imagen son nulos.
     */
    public static ImageIcon createScaledImageIcon(byte[] imageData, int width, int height) {
        if (imageData == null) {
            return null;
        }
        ImageIcon originalIcon = new ImageIcon(imageData);
        // Escalar la imagen usando Image.SCALE_SMOOTH para una mejor calidad visual
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}
