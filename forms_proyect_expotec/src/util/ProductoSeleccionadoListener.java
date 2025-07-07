// util/ProductoSeleccionadoListener.java
package util;

import modelo.Producto; // Asume que Producto está en el paquete 'modelo'

public interface ProductoSeleccionadoListener {
    // Se llama cuando se hace clic en una tarjeta de producto (para ver detalles, etc.)
    void onProductoSeleccionado(Producto producto);

    // Se llama cuando un producto se elimina con éxito de la lista de deseos
    void onProductoRemovidoDeListaDeseos(Producto producto);

    // Nuevo: Se llama para regresar a la vista principal de productos
    void volverAlCatalogo();
}