package controlador;

import com.formdev.flatlaf.FlatDarkLaf;
import forms_proyect_expotec.LoginForm;
import forms_proyect_expotec.PrincipalForm;
import forms_proyect_expotec.SplashScreen;

import javax.swing.*;

public class main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());  // Tema oscuro FlatLaf
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            SplashScreen principal = new SplashScreen();
            principal.setVisible(true);
        });
    }
}


