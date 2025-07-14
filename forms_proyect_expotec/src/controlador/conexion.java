package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    
    private static final String DB_URL = "jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:6543/postgres";
    private static final String DB_USER = "postgres.dmysuzpnstbefvvklffu";
    private static final String DB_PASSWORD = "crash3344";
    private static final String CONNECTION_PROPERTIES = "?prepareThreshold=0";

    public Connection getConnection() {
        Connection connection = null;
        try {
           
            String fullUrl = DB_URL + CONNECTION_PROPERTIES;
            connection = DriverManager.getConnection(fullUrl, DB_USER, DB_PASSWORD);

        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
           
        }
        return connection;
    }
}