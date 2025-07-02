package modelo;

import java.math.BigDecimal;

public class Producto {
    private int id; // Agregado ID para operaciones de base de datos
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private String categoria;
    private byte[] imagen;

    // Constructor para nuevos productos (el ID será auto-generado por la DB)
    public Producto(String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
    }

    // Constructor para productos obtenidos de la DB (incluye ID)
    public Producto(int id, String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen) {
        this.id = id; // <-- Este es el ID que se pasa como primer argumento
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public byte[] getImagen() { return imagen; }

    // Setter para ID (útil si se genera después de la inserción o se necesita modificar)
    public void setId(int id) {
        this.id = id;
    }
}