package gradlep.controlador;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import gradlep.modelo.Producto;
import gradlep.repositorios.OrdenDAO;
import gradlep.repositorios.ProductoDAO;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ControladorReportes {
    private final OrdenDAO ordenDAO;
    private final ProductoDAO productoDAO;

    public ControladorReportes(OrdenDAO ordenDAO, ProductoDAO productoDAO) {
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

   public void obtenerTotalVentas(Context ctx) {
    try {
        int idNegocio = obtenerIdNegocio(ctx);
        LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
        LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
        validarFechas(desde, hasta);

        double totalVentas = ordenDAO.sumarVentas(desde, hasta, idNegocio);
        ctx.status(HttpStatus.OK).json(Map.of("total_ventas", totalVentas));

    } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
    } catch (DateTimeParseException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
            "error", "Formato de fecha inválido",
            "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
        ));
    } catch (SQLException e) {
        manejarErrorSQL(ctx, "Error al obtener total de ventas", e);
    }
}

    public void obtenerTotalOrdenes(Context ctx) {
    try {
        int idNegocio = obtenerIdNegocio(ctx);
        LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
        LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
        validarFechas(desde, hasta);

        int totalOrdenes = ordenDAO.contarOrdenes(desde, hasta, idNegocio);
        ctx.status(HttpStatus.OK).json(Map.of("total_ordenes", totalOrdenes));

    } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
    } catch (DateTimeParseException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
            "error", "Formato de fecha inválido",
            "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
        ));
    } catch (SQLException e) {
        manejarErrorSQL(ctx, "Error al contar órdenes", e);
    }
}

    public void obtenerHoraPico(Context ctx) {
        try {
            int idNegocio = obtenerIdNegocio(ctx);
            LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
            LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
            validarFechas(desde, hasta);

            String horaPico = ordenDAO.horaPicoVentas(desde, hasta, idNegocio);
            ctx.status(HttpStatus.OK).json(Map.of("hora_pico_ventas", horaPico));

        } catch (DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                "error", "Formato de fecha inválido",
                "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
            ));
        } catch (SQLException e) {
            manejarErrorSQL(ctx, "Error al determinar hora pico", e);
        }
    }

    public void obtenerProductosMasVendidos(Context ctx) {
    try {
        int idNegocio = obtenerIdNegocio(ctx);
        LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
        LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
        validarFechas(desde, hasta);

        List<Producto> productos = productoDAO.obtenerTopProductos(desde, hasta, true, idNegocio);
        ctx.status(HttpStatus.OK).json(productos);

    } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
    } catch (DateTimeParseException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
            "error", "Formato de fecha inválido",
            "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
        ));
    } catch (SQLException e) {
        manejarErrorSQL(ctx, "Error al obtener productos más vendidos", e);
    }
}

    public void obtenerProductosMenosVendidos(Context ctx) {
    try {
        int idNegocio = obtenerIdNegocio(ctx);
        LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
        LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
        validarFechas(desde, hasta);

        List<Producto> productos = productoDAO.obtenerTopProductos(desde, hasta, false, idNegocio);
        ctx.status(HttpStatus.OK).json(productos);

    } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
    } catch (DateTimeParseException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
            "error", "Formato de fecha inválido",
            "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
        ));
    } catch (SQLException e) {
        manejarErrorSQL(ctx, "Error al obtener productos menos vendidos", e);
    }
}

    public void compararVentasVsGastos(Context ctx) {
        try {
            int idNegocio = obtenerIdNegocio(ctx);
            LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
            LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
            validarFechas(desde, hasta);

            double ventas = ordenDAO.sumarVentas(desde, hasta, idNegocio);
            double gastos = ordenDAO.calcularGastos(desde, hasta, idNegocio);
            
            Map<String, Double> resultado = new HashMap<>();
            resultado.put("ventas_totales", ventas);
            resultado.put("gastos_totales", gastos);
            resultado.put("diferencia", ventas - gastos);
            
            ctx.status(HttpStatus.OK).json(resultado);

        } catch (DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                "error", "Formato de fecha inválido",
                "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
            ));
        } catch (SQLException e) {
            manejarErrorSQL(ctx, "Error al comparar ventas y gastos", e);
        }
    }

public void obtenerGastos(Context ctx) {
    try {
        int idNegocio = obtenerIdNegocio(ctx);
        LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
        LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
        validarFechas(desde, hasta);

        double gastos = ordenDAO.calcularGastos(desde, hasta, idNegocio);
        ctx.status(HttpStatus.OK).json(Map.of("gastos_totales", gastos)); 

    } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
    } catch (DateTimeParseException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
            "error", "Formato de fecha inválido",
            "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
        ));
    } catch (SQLException e) {
        manejarErrorSQL(ctx, "Error al obtener gastos", e);
    }
}

    public void obtenerUtilidadNeta(Context ctx) {
    try {
        int idNegocio = obtenerIdNegocio(ctx);
        LocalDateTime desde = parseDateTime(ctx.queryParam("desde"));
        LocalDateTime hasta = parseDateTime(ctx.queryParam("hasta"));
        validarFechas(desde, hasta);

        Map<String, Double> reporte = ordenDAO.calcularUtilidadNeta(desde, hasta, idNegocio);
        ctx.status(HttpStatus.OK).json(reporte);

    } catch (IllegalArgumentException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
    } catch (DateTimeParseException e) {
        ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
            "error", "Formato de fecha inválido",
            "formato_esperado", "YYYY-MM-DDTHH:MM:SS"
        ));
    } catch (SQLException e) {
        manejarErrorSQL(ctx, "Error al calcular utilidad neta", e);
    }
}


    private LocalDateTime parseDateTime(String fecha) throws DateTimeParseException {
        if (fecha == null || fecha.isEmpty()) {
            throw new DateTimeParseException("Fecha vacía", fecha, 0);
        }
        return LocalDateTime.parse(fecha);
    }

    private void validarFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la inicial");
        }
    }

    private void manejarErrorSQL(Context ctx, String mensaje, SQLException e) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of(
            "error", mensaje,
            "detalle", e.getMessage()
        ));
    }
}