package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {

    private Connection conn;

    public conexion() {
        this.conn = null;
    }

    public void conectar() {
        try {
            // CADENA DE CONEXIÓN MODIFICADA
            // Añade &prepareThreshold=0 al final de la cadena de conexión
            String cadena = "jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:6543/postgres?user=postgres.dmysuzpnstbefvvklffu&password=crash3344&prepareThreshold=0";
            conn = DriverManager.getConnection(cadena);
            System.out.println("Conectado a la base de datos");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    public void desconectar() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexión cerrada");
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conectar();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar estado de la conexión: " + e.getMessage());
            conectar(); // Intentar reconectar si la conexión se perdió
        }
        return conn;
    }
}