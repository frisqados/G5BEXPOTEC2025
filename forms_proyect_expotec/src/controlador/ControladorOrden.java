package controlador;

import modelo.Orden;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControladorOrden {

    private final conexion conexionBD = new conexion(); // Usamos tu clase

    // Obtener todas las órdenes de un usuario
    public List<Orden> obtenerOrdenesPorUsuario(int idUsuario) {
        List<Orden> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordenes WHERE id_usuario = ? ORDER BY fecha_orden DESC";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Orden orden = new Orden(
                    rs.getInt("id_orden"),
                    rs.getInt("id_usuario"),
                    rs.getString("estado"),
                    rs.getTimestamp("fecha_orden"),
                    rs.getBigDecimal("total_orden"),
                    rs.getString("direccion_envio")
                );
                lista.add(orden);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener órdenes del usuario: " + e.getMessage());
        }

        return lista;
    }

    // Obtener una orden específica por su ID
    public Orden obtenerOrdenPorId(int idOrden) {
        String sql = "SELECT * FROM ordenes WHERE id_orden = ?";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOrden);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Orden(
                    rs.getInt("id_orden"),
                    rs.getInt("id_usuario"),
                    rs.getString("estado"),
                    rs.getTimestamp("fecha_orden"),
                    rs.getBigDecimal("total_orden"),
                    rs.getString("direccion_envio")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener la orden por ID: " + e.getMessage());
        }

        return null;
    }

    // (Opcional) Insertar nueva orden
    public boolean insertarOrden(Orden orden) {
        String sql = "INSERT INTO ordenes (id_usuario, estado, fecha_orden, total_orden, direccion_envio) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orden.getIdUsuario());
            stmt.setString(2, orden.getEstado());
            stmt.setTimestamp(3, orden.getFechaOrden());
            stmt.setBigDecimal(4, orden.getTotalOrden());
            stmt.setString(5, orden.getDireccionEnvio());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar orden: " + e.getMessage());
            return false;
        }
    }
}
