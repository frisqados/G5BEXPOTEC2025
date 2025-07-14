
package util;

import modelo.Producto;

public interface CartCardActionListener extends ProductoSeleccionadoListener {
    // Este método se llamará cuando la cantidad de un producto cambie en el JSpinner
    void onQuantityChanged(int productId, int newQuantity);

    // Este método se hereda de ProductoSeleccionadoListener
    // void onProductoSeleccionado(Producto producto);
    // Se usará para la eliminación de la tarjeta (botón 'X')
}