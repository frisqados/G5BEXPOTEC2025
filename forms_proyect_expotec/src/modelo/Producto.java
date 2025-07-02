// modelo/Producto.java
package modelo;

import java.math.BigDecimal;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private String categoria;
    private byte[] imagen;
    private String publisherName; // Nuevo campo

    public Producto(int id, String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen, String publisherName) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
        this.publisherName = publisherName;
    }

    // Constructor original si lo necesitas (sin publisherName)
    public Producto(int id, String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen) {
        this(id, nombre, descripcion, precio, stock, categoria, imagen, "Desconocido"); // Valor por defecto
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public byte[] getImagen() { return imagen; }
    public String getPublisherName() { return publisherName; } // Nuevo getter
}