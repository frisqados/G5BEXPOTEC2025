package modelo; // O donde tengas tus modelos, asegúrate que Producto.java esté aquí.

import controlador.conexion; // Asumo que tu clase de conexión está en 'controlador'
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public static final int MAX_RECOMENDACIONES = 5;
    private static final int MAX_CATEGORIAS_FAVORITAS = 3;

    public List<Producto> buscarRecomendaciones(int idUsuario) throws SQLException {
        List<Producto> recomendaciones = new ArrayList<>();
        List<String> categoriasFavoritas = obtenerCategoriasMasCompradas(idUsuario);

        if (!categoriasFavoritas.isEmpty()) {
            StringBuilder sqlRec = new StringBuilder("SELECT * FROM productos WHERE categoria IN (");
            for (int i = 0; i < categoriasFavoritas.size(); i++) {
                sqlRec.append("?");
                if (i < categoriasFavoritas.size() - 1) sqlRec.append(",");
            }
            sqlRec.append(")");
            sqlRec.append(" ORDER BY id_producto LIMIT ").append(MAX_RECOMENDACIONES); // Aseguramos el orden y el límite

            try (Connection connection = new conexion().getConnection();
                 PreparedStatement psRec = connection.prepareStatement(sqlRec.toString())) {

                for (int i = 0; i < categoriasFavoritas.size(); i++) {
                    psRec.setString(i + 1, categoriasFavoritas.get(i));
                }
                try (ResultSet rs = psRec.executeQuery()) {
                    while (rs.next()) {
                        recomendaciones.add(crearProductoDesdeResultSet(rs));
                    }
                }
            }
        }
        return recomendaciones;
    }

    private List<String> obtenerCategoriasMasCompradas(int idUsuario) throws SQLException {
        List<String> categorias = new ArrayList<>();
        String sqlCategorias = "SELECT p.categoria, COUNT(*) as cantidad FROM ordenes o " +
                               "JOIN detalle_ordenes d ON o.id_orden = d.id_orden " +
                               "JOIN productos p ON p.id_producto = d.id_producto " +
                               "WHERE o.id_usuario = ? GROUP BY p.categoria ORDER BY cantidad DESC LIMIT " + MAX_CATEGORIAS_FAVORITAS;

        try (Connection connection = new conexion().getConnection();
             PreparedStatement psCat = connection.prepareStatement(sqlCategorias)) {
            psCat.setInt(1, idUsuario);
            try (ResultSet rsCat = psCat.executeQuery()) {
                while (rsCat.next()) {
                    categorias.add(rsCat.getString("categoria"));
                }
            }
        }
        return categorias;
    }

    public List<Producto> buscarProductos(String searchTerm) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        StringBuilder sqlTodos = new StringBuilder("SELECT * FROM productos");
        List<String> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String[] palabras = normalizeString(searchTerm).split("\\s+");
            sqlTodos.append(" WHERE ");
            for (int i = 0; i < palabras.length; i++) {
                if (i > 0) sqlTodos.append(" AND ");
                sqlTodos.append("(");
                // Usamos LOWER y REGEXP_REPLACE (PostgreSQL) o REPLACE encadenados para manejar tildes/acentos
                // NOTA: REGEXP_REPLACE es más potente, pero REPLACE encadenado es más compatible entre DBs si no usas regex.
                // Aquí se mantiene el enfoque de REPLACE encadenado para compatibilidad y simplicidad si no hay REGEX disponible.
                String normalizedColumn = "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
                                          "%s, 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'à', 'a'), 'è', 'e'), 'ì', 'i'), 'ò', 'o'), 'ù', 'u'))";

                sqlTodos.append(String.format(normalizedColumn, "nombre")).append(" LIKE ? ");
                sqlTodos.append("OR ").append(String.format(normalizedColumn, "descripcion")).append(" LIKE ? ");
                sqlTodos.append("OR ").append(String.format(normalizedColumn, "categoria")).append(" LIKE ? ");
                sqlTodos.append(")");
                String patron = "%" + palabras[i] + "%";
                params.add(patron);
                params.add(patron);
                params.add(patron);
            }
        }

        try (Connection connection = new conexion().getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlTodos.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(crearProductoDesdeResultSet(rs));
                }
            }
        }
        return productos;
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_producto");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        BigDecimal precio = rs.getBigDecimal("precio");
        int stock = rs.getInt("stock");
        String categoria = rs.getString("categoria");
        byte[] imagen = rs.getBytes("imagen");
        // Asegúrate de que tu constructor de Producto pueda aceptar un nombre de publicador si lo necesitas.
        // Si la columna 'nombre_publicador' existe en 'productos' o la obtienes con JOIN:
        // String publisherName = rs.getString("nombre_publicador");
        return new Producto(id, nombre, descripcion, precio, stock, categoria, imagen, null); // null for publisherName for now
    }

    private String normalizeString(String text) {
        if (text == null) return null;
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }
}