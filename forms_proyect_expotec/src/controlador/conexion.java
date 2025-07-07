package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {

    // Guarda tus datos de conexión como constantes estáticas.
    // Es mejor leer esto de un archivo de configuración en una aplicación real,
    // pero para este ejemplo, las dejamos aquí.
    private static final String DB_URL = "jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:6543/postgres";
    private static final String DB_USER = "postgres.dmysuzpnstbefvvklffu";
    private static final String DB_PASSWORD = "crash3344";
    private static final String CONNECTION_PROPERTIES = "?prepareThreshold=0";

    /**
     * Obtiene y devuelve una nueva conexión a la base de datos.
     * Es responsabilidad del código que llama a este método cerrar la conexión
     * en un bloque finally.
     * @return Una nueva instancia de Connection, o null si falla la conexión.
     */
    public Connection getConnection() {
        Connection connection = null;
        try {
            // Carga el driver de PostgreSQL. En JDBC 4.0+ esto suele ser automático,
            // pero incluirlo es una buena práctica y no hace daño.
            // Class.forName("org.postgresql.Driver"); // Descomentar si tienes problemas de ClassNotFoundException

            String fullUrl = DB_URL + CONNECTION_PROPERTIES;
            connection = DriverManager.getConnection(fullUrl, DB_USER, DB_PASSWORD);
            // System.out.println("Nueva conexión a la base de datos establecida."); // Comenta esto para evitar spam en consola
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            // No imprimimos el stack trace completo aquí. El error se manejará
            // en el lugar donde se intente usar esta conexión nula.
        }
        return connection;
    }
}