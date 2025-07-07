package modelo;

// Importa Producto para poder hacer referencia a él
import java.math.BigDecimal;
import modelo.Producto;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    public ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    // --- Getters ---
    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    // --- Setters (si son necesarios, por ejemplo, para ajustar la cantidad en el carrito) ---
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // Puedes añadir un método para calcular el subtotal de este ítem
    public BigDecimal getSubtotal() {
        // Multiplica el precio del producto por la cantidad.
        // Asegúrate de que precio sea BigDecimal para evitar errores de precisión.
        return producto.getPrecio().multiply(new BigDecimal(cantidad));
    }
}