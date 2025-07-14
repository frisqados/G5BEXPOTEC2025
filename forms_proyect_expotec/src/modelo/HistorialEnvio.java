package modelo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistorialEnvio {
    private int idHistorialEnvio;
    private int idOrden;
    private String estadoOrden;
    private String ubicacionActual;
    private Timestamp fechaHoraEvento;
    private String notas;

    public HistorialEnvio(int idHistorialEnvio, int idOrden, String estadoOrden, String ubicacionActual, Timestamp fechaHoraEvento, String notas) {
        this.idHistorialEnvio = idHistorialEnvio;
        this.idOrden = idOrden;
        this.estadoOrden = estadoOrden;
        this.ubicacionActual = ubicacionActual;
        this.fechaHoraEvento = fechaHoraEvento;
        this.notas = notas;
    }

    // Getters
    public int getIdHistorialEnvio() { return idHistorialEnvio; }
    public int getIdOrden() { return idOrden; }
    public String getEstadoOrden() { return estadoOrden; }
    public String getUbicacionActual() { return ubicacionActual; }
    public Timestamp getFechaHoraEvento() { return fechaHoraEvento; }
    public String getNotas() { return notas; }

    // Método de utilidad para formatear la fecha/hora
    public String getFechaHoraFormateada() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new Date(fechaHoraEvento.getTime()));
    }
}