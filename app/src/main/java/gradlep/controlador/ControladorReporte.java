package gradlep.controlador;

import io.javalin.http.Context;
import gradlep.modelo.DetalleOrden;
import gradlep.modelo.Producto;
import gradlep.repositorios.OrdenDAO;
import gradlep.repositorios.ProductoDAO;
import gradlep.servicios.ExportarReporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ControladorReporte {

    private final OrdenDAO ordenDAO;
    private final ProductoDAO productoDAO;

    public ControladorReporte(OrdenDAO ordenDAO, ProductoDAO productoDAO) {
        this.ordenDAO = ordenDAO;
        this.productoDAO = productoDAO;
    }


    
    private int obtenerIdNegocio(Context ctx) {
        try {
                return Integer.parseInt(ctx.pathParam("idNegocio"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de negocio inválido");
            }
        }

    public void generarPDF(Context ctx) {
        try {

            System.out.println("Path completo: " + ctx.path());
        System.out.println("Path params: " + ctx.pathParamMap());


            String desdeStr = ctx.queryParam("desde");
            String hastaStr = ctx.queryParam("hasta");
              int idNegocio = obtenerIdNegocio(ctx);

               
        System.out.println("idNegocioStr: " + idNegocio);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime desde = LocalDate.parse(desdeStr, formatter).atStartOfDay();
            LocalDateTime hasta = LocalDate.parse(hastaStr, formatter).atTime(23, 59, 59);

            double totalVentas = ordenDAO.sumarVentas(desde, hasta, idNegocio);
            int totalOrdenes = ordenDAO.contarOrdenes(desde, hasta, idNegocio);
            String horaPico = ordenDAO.horaPicoVentas(desde, hasta, idNegocio);
            double totalGastos = ordenDAO.calcularGastos(desde, hasta, idNegocio);
            double utilidadNeta = totalVentas - totalGastos;
            List<Producto> masVendidos = productoDAO.obtenerTopProductos(desde, hasta, true, idNegocio);
            List<Producto> menosVendidos = productoDAO.obtenerTopProductos(desde, hasta, false, idNegocio);

            byte[] pdf = ExportarReporte.generarReporte(
                    desde, hasta,
                    totalVentas,
                    totalOrdenes,
                    horaPico,
                    totalGastos,
                    utilidadNeta,
                    masVendidos,
                    menosVendidos
            );

            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "attachment; filename=reporte.pdf");
            ctx.result(pdf);

        } catch (Exception e) {
            ctx.status(500).result("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }
}