package util;

public class UserSession {
    private static int currentUserId = 0; // Inicializamos a 0 para indicar que no hay usuario logueado
    private static String currentUserName;
    private static String currentUserEmail;
    
    public static void login(int id, String name, String email) {
        currentUserId = id;
        currentUserName = name;
        currentUserEmail = email;
        System.out.println("Usuario logueado: ID=" + id + ", Nombre=" + name + ", Email=" + email);
    }

    public static void logout() {
        currentUserId = 0; // Restablecemos a 0 cuando no hay usuario logueado
        currentUserName = null;
        currentUserEmail = null;
        System.out.println("Sesión de usuario cerrada.");
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserName() {
        return currentUserName;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static boolean isLoggedIn() {
        // Es más seguro verificar por el ID del usuario.
        // Asume que un ID de usuario de 0 (o -1 si prefieres) significa que no hay sesión activa.
        return currentUserId != 0; 
    }
    
    
    
}