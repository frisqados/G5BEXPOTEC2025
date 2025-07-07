package util;

import java.security.SecureRandom;

public class GeneradorToken {

    private static final String CARACTERES = "0123456789"; // Solo números para un código simple
    private static final int LONGITUD_CODIGO = 6; // Longitud del código de verificación

    /**
     * Genera un código numérico aleatorio de la longitud especificada.
     * @return El código numérico generado como String.
     */
    public static String generarCodigoNumerico() {
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo.append(CARACTERES.charAt(random.nextInt(CARACTERES.length())));
        }
        return codigo.toString();
    }
}