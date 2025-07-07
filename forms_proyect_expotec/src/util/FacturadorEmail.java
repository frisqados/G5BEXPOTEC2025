package util;

import java.util.List;
import modelo.Producto;
import modelo.ItemCarrito;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

public class FacturadorEmail {

    public static boolean enviarFacturaPorCorreo(
            String emailCliente,
            String nombreCliente,
            String idCompra,
            List<ItemCarrito> itemsComprados,
            double totalCompra) {

        if (emailCliente == null || emailCliente.trim().isEmpty() || !emailCliente.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            System.err.println("Correo inválido.");
            return false;
        }
        if (itemsComprados == null || itemsComprados.isEmpty()) {
            System.err.println("No hay productos en la factura.");
            return false;
        }

        String asunto = "¡Tu Factura de Compra - Pedido #" + idCompra + "!";
        String cuerpoFactura = generarCuerpoFacturaHTML(idCompra, nombreCliente, itemsComprados, totalCompra);

        return EnviarCorreoJakarta.enviarEmail(emailCliente, asunto, cuerpoFactura);
    }

    private static String generarCuerpoFacturaHTML(String idCompra, String nombreCliente, List<ItemCarrito> items, double totalCompra) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
            .append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; color: #333; }")
            .append(".container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }")
            .append("h2 { color: #0056b3; text-align: center; margin-bottom: 20px; }")
            .append("p { line-height: 1.6; }")
            .append(".invoice-details, .product-list { margin-bottom: 20px; border-top: 1px solid #eee; padding-top: 15px; }")
            .append("table { width: 100%; border-collapse: collapse; margin-top: 15px; }")
            .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
            .append("th { background-color: #f2f2f2; }")
            .append(".total-row td { font-weight: bold; background-color: #e9e9e9; }")
            .append(".thank-you { text-align: center; margin-top: 30px; font-size: 1.1em; color: #008000; }")
            .append("</style></head><body><div class='container'>")

            .append("<h2>Factura de Compra - Tu Tienda Online</h2>")
            .append("<p>Estimado/a <strong>").append(nombreCliente).append("</strong>,</p>")
            .append("<p>Gracias por tu compra. Aquí están los detalles:</p>")

            .append("<div class='invoice-details'>")
            .append("<p><strong>Número de Pedido:</strong> #").append(idCompra).append("</p>")
            .append("<p><strong>Fecha de Compra:</strong> ").append(now.format(formatter)).append("</p>")
            .append("</div>")

            .append("<div class='product-list'><h3>Productos Comprados:</h3><table>")
            .append("<thead><tr><th>Producto</th><th>Cantidad</th><th>Precio Unitario</th><th>Subtotal</th></tr></thead><tbody>");

        for (ItemCarrito item : items) {
            Producto p = item.getProducto();
            int cantidad = item.getCantidad();
            BigDecimal subtotal = p.getPrecio().multiply(new BigDecimal(cantidad));

            html.append("<tr>")
                .append("<td>").append(p.getNombre()).append("</td>")
                .append("<td>").append(cantidad).append("</td>")
                .append("<td>$").append(String.format("%.2f", p.getPrecio())).append("</td>")
                .append("<td>$").append(String.format("%.2f", subtotal)).append("</td>")
                .append("</tr>");
        }

        html.append("<tr class='total-row'><td colspan='3' style='text-align: right;'>Total:</td>")
            .append("<td><strong>$").append(String.format("%.2f", totalCompra)).append("</strong></td></tr>")

            .append("</tbody></table></div>")
            .append("<p class='thank-you'>¡Gracias por tu confianza!</p>")
            .append("<div class='footer'><p>Si tienes preguntas, contáctanos.</p></div>")
            .append("</div></body></html>");

        return html.toString();
    }
}
