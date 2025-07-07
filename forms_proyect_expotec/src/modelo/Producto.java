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
    private String publisherName;

    // Constructor COMPLETO con todos los campos, incluyendo publisherName
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

    // Constructor para casos donde el publisherName no es necesario (se le asigna "Desconocido")
    public Producto(int id, String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen) {
        this(id, nombre, descripcion, precio, stock, categoria, imagen, "Desconocido");
    }

    // Constructor para coincidir con la llamada en DetallesProductoFrame.main()
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

    // --- NUEVO CONSTRUCTOR para el caso del carrito/factura ---
    // Este constructor recibe solo ID, nombre, precio y stock.
    // Los demás campos se inicializan con valores por defecto.
    public Producto(int id, String nombre, BigDecimal precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.descripcion = ""; // Valor por defecto
        this.categoria = "";   // Valor por defecto
        this.imagen = null;    // Valor por defecto
        this.publisherName = "N/A"; // Valor por defecto
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
    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", categoria='" + categoria + '\'' +
                ", publisherName='" + publisherName + '\'' +
                '}';
    }
}