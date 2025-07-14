package modelo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Orden {
    private int idOrden;
    private int idUsuario;
    private String estado;
    private Timestamp fechaOrden;
    private BigDecimal totalOrden;
    private String direccionEnvio;

    public Orden(int idOrden, int idUsuario, String estado, Timestamp fechaOrden, BigDecimal totalOrden, String direccionEnvio) {
        this.idOrden = idOrden;
        this.idUsuario = idUsuario;
        this.estado = estado;
        this.fechaOrden = fechaOrden;
        this.totalOrden = totalOrden;
        this.direccionEnvio = direccionEnvio;
    }

    // Getters
    public int getIdOrden() { return idOrden; }
    public int getIdUsuario() { return idUsuario; }
    public String getEstado() { return estado; }
    public Timestamp getFechaOrden() { return fechaOrden; }
    public BigDecimal getTotalOrden() { return totalOrden; }
    public String getDireccionEnvio() { return direccionEnvio; }
}