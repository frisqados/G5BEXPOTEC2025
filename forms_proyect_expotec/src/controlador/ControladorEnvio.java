package controlador;

import modelo.HistorialEnvio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ControladorEnvio {

    public boolean registrarEventoEnvio(int idOrden, String estado, String ubicacionDetalle, String notas) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = new conexion().getConnection();
            String sql = "INSERT INTO historial_envio_orden (id_orden, estado_orden, ubicacion_actual, notas) VALUES (?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setInt(1, idOrden);
            ps.setString(2, estado);
            ps.setString(3, ubicacionDetalle);
            ps.setString(4, notas);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar evento de envío: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar conexión en registrarEventoEnvio: " + ex.getMessage());
            }
        }
    }

    public List<HistorialEnvio> obtenerHistorialEnvioPorOrden(int idOrden) {
        List<HistorialEnvio> historial = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = new conexion().getConnection();
            String sql = "SELECT id_historial_envio, id_orden, estado_orden, ubicacion_actual, fecha_hora_evento, notas " +
                         "FROM historial_envio_orden WHERE id_orden = ? ORDER BY fecha_hora_evento ASC";
            ps = con.prepareStatement(sql);
            ps.setInt(1, idOrden);
            rs = ps.executeQuery();

            while (rs.next()) {
                historial.add(new HistorialEnvio(
                    rs.getInt("id_historial_envio"),
                    rs.getInt("id_orden"),
                    rs.getString("estado_orden"),
                    rs.getString("ubicacion_actual"),
                    rs.getTimestamp("fecha_hora_evento"),
                    rs.getString("notas")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial de envío: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                System.err.println("Error al cerrar conexión en obtenerHistorialEnvioPorOrden: " + ex.getMessage());
            }
        }
        return historial;
    }
}