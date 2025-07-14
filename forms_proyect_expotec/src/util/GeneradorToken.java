package util;

import java.security.SecureRandom;

public class GeneradorToken {

    private static final String CARACTERES = "0123456789";
    private static final int LONGITUD_CODIGO = 6; 

    public static String generarCodigoNumerico() {
        SecureRandom random = new SecureRandom();
        StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo.append(CARACTERES.charAt(random.nextInt(CARACTERES.length())));
        }
        return codigo.toString();
    }
}