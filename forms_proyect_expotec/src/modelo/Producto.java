package modelo;

import java.math.BigDecimal;
import java.io.Serializable; // Importar Serializable si planeas guardar objetos Producto en archivos

public class Producto implements Serializable { // Implementa Serializable
    private int id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private String categoria;
    private byte[] imagen;
    private String publisherName;

    // Constructor por defecto (necesario si vas a usar serialización o algunos frameworks)
    public Producto() {
        // Inicializar con valores por defecto
        this.id = 0;
        this.nombre = "";
        this.descripcion = "";
        this.precio = BigDecimal.ZERO;
        this.stock = 0;
        this.categoria = "";
        this.imagen = null;
        this.publisherName = "";
    }

    // Constructores existentes:
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

    public Producto(int id, String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen) {
        this(id, nombre, descripcion, precio, stock, categoria, imagen, "Desconocido");
    }

    public Producto(int id, String nombre, String descripcion, int stock, String categoria, byte[] imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
        this.precio = BigDecimal.ZERO;
        this.publisherName = "Desconocido";
    }

    public Producto(int id, String nombre, BigDecimal precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = "";
        this.categoria = "";
        this.imagen = null;
        this.publisherName = "N/A";
    }

    // Constructor que necesitas AÑADIR (para CarritoPanel)
    public Producto(int id, String nombre, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = 0;
        this.descripcion = null;
        this.categoria = null;
        this.imagen = null;
        this.publisherName = null;
    }

    // Constructor que agregamos previamente
    public Producto(int id, String nombre, BigDecimal precio, int stock, byte[] imagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.imagen = imagen;
        this.descripcion = null;
        this.categoria = null;
        this.publisherName = null;
    }


    // --- Getters ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public byte[] getImagen() { return imagen; }
    public String getPublisherName() { return publisherName; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }


    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                '}';
    }

    // Opcional: Para asegurar que productos duplicados no se añadan si ya están en la lista
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return id == producto.id; // Comparamos por ID para unicidad en la lista
    }

    @Override
    public int hashCode() {
        return id;
    }
}