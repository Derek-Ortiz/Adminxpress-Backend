package gradlep.servicios;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;

import gradlep.modelo.Producto;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import javax.imageio.ImageIO;

public class ExportarReporte {

        public static byte[] generarReporte(
            LocalDateTime desde,
            LocalDateTime hasta,
            double totalVentas,
            int totalOrdenes,
            String horaPico,
            double totalGastos,
            double utilidadNeta,
            List<Producto> masVendidos,
            List<Producto> menosVendidos

        )throws Exception {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph("Reporte de Ventas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Rango de fechas: " + desde.toLocalDate() + " a " + hasta.toLocalDate(), normalFont));
        document.add(new Paragraph("\n"));
 
        PdfPTable resumen = new PdfPTable(2);
        resumen.setWidthPercentage(60);
        resumen.setSpacingBefore(10);
        resumen.addCell("Total de ventas:");
        resumen.addCell("$" + String.format("%.2f", totalVentas));
        resumen.addCell("Total de órdenes:");
        resumen.addCell(String.valueOf(totalOrdenes));
        resumen.addCell("Hora pico de ventas:");
        resumen.addCell(horaPico != null ? horaPico : "N/A");
        resumen.addCell("Total de gastos:");
        resumen.addCell("$" + String.format("%.2f", totalGastos));
        resumen.addCell("Utilidad neta:");
        resumen.addCell("$" + String.format("%.2f", utilidadNeta));
        document.add(resumen);

        document.add(new Paragraph("\n"));

        Paragraph grafTitle = new Paragraph("Comparación: Ventas vs Gastos", subTitleFont);
        grafTitle.setSpacingBefore(10);
        document.add(grafTitle);

        Image chartImage = Image.getInstance(generarGraficaComparativa(totalVentas, totalGastos));
        chartImage.scaleToFit(400, 300);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        document.add(chartImage);

        Paragraph topHeader = new Paragraph("Productos Más Vendidos", subTitleFont);
        topHeader.setSpacingBefore(15);
        document.add(topHeader);

        PdfPTable tableMasVendidos = new PdfPTable(3);
        tableMasVendidos.setWidthPercentage(100);
        tableMasVendidos.addCell("Nombre");
        tableMasVendidos.addCell("Cantidad vendida");
        tableMasVendidos.addCell("Precio actual");

        for (Producto producto : masVendidos) {
            tableMasVendidos.addCell(producto.getNombre());
            tableMasVendidos.addCell(String.valueOf(producto.getTotalVendido()));
            tableMasVendidos.addCell("$" + String.format("%.2f", producto.getPrecioActual()));
        }

        document.add(tableMasVendidos);

        Paragraph lowHeader = new Paragraph("Productos Menos Vendidos", subTitleFont);
        lowHeader.setSpacingBefore(15);
        document.add(lowHeader);

        PdfPTable tableMenosVendidos = new PdfPTable(3);
        tableMenosVendidos.setWidthPercentage(100);
        tableMenosVendidos.addCell("Nombre");
        tableMenosVendidos.addCell("Cantidad vendida");
        tableMenosVendidos.addCell("Precio actual");

        for (Producto producto : menosVendidos) {
            tableMenosVendidos.addCell(producto.getNombre());
            tableMenosVendidos.addCell(String.valueOf(producto.getTotalVendido()));
            tableMenosVendidos.addCell("$" + String.format("%.2f", producto.getPrecioActual()));
        }

        document.add(tableMenosVendidos);

        document.close();
        return out.toByteArray();
    }

    private static byte[] generarGraficaComparativa(double ventas, double gastos) throws Exception {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    dataset.addValue(ventas, "Ventas", "");
    dataset.addValue(gastos, "Gastos", "");
    
    JFreeChart chart = ChartFactory.createBarChart(
        null,
        "Fechas",
        "Monto ($)",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    );
    
    chart.setBackgroundPaint(Color.WHITE);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(84, 146, 116));
    renderer.setSeriesPaint(1, new Color(255, 99, 132)); 
    renderer.setDrawBarOutline(false);
    renderer.setShadowVisible(false);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    rangeAxis.setAutoRange(true);
    
    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryMargin(0.2);
    
    BufferedImage bufferedImage = chart.createBufferedImage(500, 300, BufferedImage.TYPE_INT_RGB, null);
    ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", chartOut);
    
    return chartOut.toByteArray();
}
}
