package modelo;

import java.math.BigDecimal; // Necesario para el tipo de dato BigDecimal

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio; // El precio debe ser BigDecimal
    private int stock;         // El stock es un entero
    private String categoria;
    private byte[] imagen;     // La imagen es un array de bytes
    private String publisherName; // Nuevo campo

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
    // Asegúrate de que este constructor coincida con cualquier otro lugar donde crees Producto sin publisherName
    public Producto(int id, String nombre, String descripcion, BigDecimal precio, int stock, String categoria, byte[] imagen) {
        this(id, nombre, descripcion, precio, stock, categoria, imagen, "Desconocido"); // Llama al constructor completo con un valor por defecto
    }

    // NUEVO CONSTRUCTOR para coincidir con la llamada en DetallesProductoFrame.main()
    // Si necesitas crear un Producto solo con estos 6 campos, este constructor lo permite.
    // Se asignarán valores por defecto para 'precio' y 'publisherName'.
    public Producto(int id, String nombre, String descripcion, int stock, String categoria, byte[] imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock = stock;
        this.categoria = categoria;
        this.imagen = imagen;
        this.precio = BigDecimal.ZERO; // Valor por defecto, ajusta si necesitas otro.
        this.publisherName = "Desconocido"; // Valor por defecto.
    }


    // --- Getters ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public byte[] getImagen() { return imagen; }
    public String getPublisherName() { return publisherName; } // Nuevo getter

    // --- Setters ---
    // Este setter es CRUCIAL para que DetallesProductoFrame pueda actualizar el stock.
    public void setStock(int stock) {
        this.stock = stock;
    }

    // Puedes añadir setters para otros campos si los necesitas en el futuro, por ejemplo:
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
               // No imprimir la imagen en toString por ser un array de bytes grande
               '}';
    }
}