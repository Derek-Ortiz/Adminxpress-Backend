package gradlep.servicios;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import gradlep.modelo.DetalleOrden;
import gradlep.modelo.Orden;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportarTicket {

    public static byte[] generarTicket(Orden orden, List<DetalleOrden> detalles) throws Exception {
        Document doc = new Document(PageSize.A6);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);

        doc.open();

    String nombreNegocio = orden.getNegocio() != null ? orden.getNegocio().getNombre() : "Negocio no especificado";
    Paragraph titulo = new Paragraph(
        nombreNegocio + "\n" +
        "TICKET DE COMPRA\n",
        new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)
    );
    titulo.setAlignment(Element.ALIGN_CENTER);
    doc.add(titulo);

        doc.add(new Paragraph("ID Orden: " + orden.getId()));
        doc.add(new Paragraph("Fecha: " + orden.getFecha().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        doc.add(new Paragraph("Cajero: " + orden.getNombreCajero()));
        doc.add(new Paragraph("--------------------------------------------------"));

        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new int[]{4, 2, 2, 2});
        tabla.addCell("Producto");
        tabla.addCell("Cant");
        tabla.addCell("Precio");
        tabla.addCell("Subtot");

        double total = 0;
        for (DetalleOrden d : detalles) {
            double subtotal = d.getProducto().getPrecioActual() * d.getCantidad();
            total += subtotal;

            tabla.addCell(d.getProducto().getNombre());
            tabla.addCell(String.valueOf(d.getCantidad()));
            tabla.addCell(String.format("$%.2f", d.getProducto().getPrecioActual()));
            tabla.addCell(String.format("$%.2f", subtotal));
        }

        doc.add(tabla);

        doc.add(new Paragraph("--------------------------------------------------"));
        doc.add(new Paragraph(String.format("Total a pagar: $%.2f", total), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        doc.add(new Paragraph("¡Gracias por su compra!", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));

        doc.close();
        return baos.toByteArray();
    }
}
