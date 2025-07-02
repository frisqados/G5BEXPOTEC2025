package util;
/**
 * Clase para gestionar la sesión del usuario actual.
 * Utiliza un patrón Singleton simple (a través de métodos estáticos)
 * para almacenar y proporcionar acceso a la información del usuario logueado.
 */
public class UserSession {
    private static int currentUserId;
    private static String currentUserName;
    private static String currentUserEmail;
    // Puedes añadir más campos si los necesitas, como el apellido, etc.

    /**
     * Establece la información del usuario al iniciar sesión.
     * @param id El ID del usuario.
     * @param name El nombre del usuario.
     * @param email El correo electrónico del usuario.
     */
    public static void login(int id, String name, String email) {
        currentUserId = id;
        currentUserName = name;
        currentUserEmail = email;
        System.out.println("Usuario logueado: ID=" + id + ", Nombre=" + name + ", Email=" + email);
    }

    /**
     * Limpia la información de la sesión al cerrar sesión.
     */
    public static void logout() {
        currentUserId = 0; // O un valor que indique que no hay usuario logueado
        currentUserName = null;
        currentUserEmail = null;
        System.out.println("Sesión de usuario cerrada.");
    }

    /**
     * Obtiene el ID del usuario actualmente logueado.
     * @return El ID del usuario, o 0 si no hay usuario logueado.
     */
    public static int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Obtiene el nombre del usuario actualmente logueado.
     * @return El nombre del usuario, o null si no hay usuario logueado.
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Obtiene el correo electrónico del usuario actualmente logueado.
     * @return El correo electrónico del usuario, o null si no hay usuario logueado.
     */
    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    /**
     * Verifica si hay un usuario actualmente logueado.
     * @return true si hay un usuario logueado, false en caso contrario.
     */
    public static boolean isLoggedIn() {
        return currentUserName != null; // O currentUserId != 0
    }
}
