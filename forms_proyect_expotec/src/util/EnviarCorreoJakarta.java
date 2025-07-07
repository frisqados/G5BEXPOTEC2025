package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EnviarCorreoJakarta {

    public static boolean enviarEmail(String para, String asunto, String contenidoHtml) {
        final String remitente = "patzanaron9@gmail.com"; // <-- CAMBIA esto
        final String clave = "mjow lodn yvkv azlt";       // <-- Y esto (contraseña de aplicación)
        
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");

        Session sesion = Session.getInstance(propiedades, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, clave);
            }
        });

        try {
            Message mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(remitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
            mensaje.setSubject(asunto);
            
            // Aquí nos aseguramos de que el contenido sea HTML
            mensaje.setContent(contenidoHtml, "text/html; charset=utf-8");

            Transport.send(mensaje);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
