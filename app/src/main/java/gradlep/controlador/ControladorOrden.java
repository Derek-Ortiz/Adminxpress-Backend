package gradlep.controlador;

import io.javalin.http.Context;
import gradlep.modelo.DetalleOrden;
import gradlep.modelo.Orden;
import gradlep.repositorios.OrdenDAO;
import gradlep.servicios.ExportarTicket;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ControladorOrden {
    private final OrdenDAO ordenDAO;

    public ControladorOrden(OrdenDAO ordenDAO) {
        this.ordenDAO = ordenDAO;
    }

    public void crearOrden(Context ctx) {
        try {
            int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
         
            Orden orden = ctx.bodyAsClass(Orden.class);
            
            orden.setIdNegocio(idNegocio);
         
            if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "La orden debe contener al menos un detalle"
                ));
                return;
            }

            boolean preciosValidos = ordenDAO.validarYActualizarPrecios(orden);
            if (!preciosValidos) {
                ctx.status(400).json(Map.of(
                    "error", "Algunos productos no existen o no están disponibles"
                ));
                return;
            }

            orden.setTotal(orden.calcularTotal());
            System.out.println("total" + orden.getTotal());
   
            int idOrden = ordenDAO.guardarOrden(orden);

            if (idOrden != -1) {

                if (orden.isEstado()) {
                    ordenDAO.reducirInsumosPorOrden(orden);
                    System.out.println("entro a isEstado para reducir");
                }

                ctx.status(201).json(Map.of(
                    "idOrden", idOrden,
                    "total", orden.getTotal(),
                    "mensaje", "Orden creada exitosamente"
                ));
            } else {
                ctx.status(400).json(Map.of(
                    "error", "No se pudo crear la orden"
                ));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "error", "ID de negocio inválido"
            ));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error de base de datos al crear la orden",
                "detalle", e.getMessage()
            ));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                "error", "Error al procesar la solicitud",
                "detalle", e.getMessage()
            ));
        }
    }

    public void listarPedidos(Context ctx) {
        try {
            int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
            
            List<Orden> pedidos = ordenDAO.listarPedidosPorNegocio(idNegocio);
            
            ctx.status(200).json(Map.of(
                "pedidos", pedidos,
                "total", pedidos.size(),
                "idNegocio", idNegocio
            ));
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "error", "ID de negocio inválido"
            ));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error de base de datos al obtener pedidos",
                "detalle", e.getMessage()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "error", "Error al procesar la solicitud",
                "detalle", e.getMessage()
            ));
        }
    }

    public void actualizarPedido(Context ctx) {
    try {
     
        int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        int idPedido = Integer.parseInt(ctx.pathParam("id"));
      
        Orden ordenExistente = ordenDAO.buscarOrdenPorIdYNegocio(idPedido, idNegocio);
        
        if (ordenExistente == null) {
           
            ctx.status(404).json(Map.of(
                "error", "Pedido no encontrado o no pertenece al negocio especificado"
            ));
            return;
        }

        Orden ordenActualizada = ctx.bodyAsClass(Orden.class);
        
        System.out.println("Datos recibidos para actualización: " + ordenActualizada.toString());

        ordenActualizada.setId(idPedido);
        ordenActualizada.setIdNegocio(idNegocio);
        
        if (ordenActualizada.getDetalles() == null || ordenActualizada.getDetalles().isEmpty()) {
           
            System.out.println("Error: La orden no tiene detalles");
            ctx.status(400).json(Map.of(
                "error", "La orden debe contener al menos un detalle"
            ));
            return;
        }

        boolean preciosValidos = ordenDAO.validarYActualizarPrecios(ordenActualizada);
        
        System.out.println("Precios válidos: " + preciosValidos);
        
        if (!preciosValidos) {
       
            System.out.println("Error: Productos no existen o no disponibles");
            ctx.status(400).json(Map.of(
                "error", "Algunos productos no existen o no están disponibles"
            ));
            return;
        }
     
        boolean estadoAnterior = ordenExistente.isEstado();
        
        System.out.println("anterior: " + estadoAnterior + ", Nuevo: " + ordenActualizada.isEstado());

        double totalCalculado = ordenActualizada.calcularTotal();
        ordenActualizada.setTotal(totalCalculado);
       
        System.out.println("Total calculado: " + totalCalculado);

        boolean actualizado = ordenDAO.actualizarOrden(ordenActualizada);
        
        System.out.println("Orden actualizada en BD: " + actualizado);

        if (actualizado) {
           
            if (estadoAnterior != ordenActualizada.isEstado()) {
                if (ordenActualizada.isEstado()) {
                    ordenDAO.reducirInsumosPorOrden(ordenActualizada);
                } else if (estadoAnterior) {
                    ordenDAO.aumentarInsumosPorOrden(ordenActualizada);
                }
            }
            ctx.status(200).json(Map.of(
                "idOrden", idPedido,
                "total", ordenActualizada.getTotal(),
                "mensaje", "Pedido actualizado exitosamente"
            ));
        } else {
           
            ctx.status(400).json(Map.of(
                "error", "No se pudo actualizar el pedido"
            ));
        }
        
    } catch (NumberFormatException e) {
       
        ctx.status(400).json(Map.of(
            "error", "ID de negocio o pedido inválido"
        ));
    } catch (SQLException e) {
     
        ctx.status(500).json(Map.of(
            "error", "Error de base de datos al actualizar pedido",
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
      
        e.printStackTrace();
        ctx.status(400).json(Map.of(
            "error", "Error al procesar la solicitud",
            "detalle", e.getMessage()
        ));
    } finally {
        System.out.println("Finalizado proceso de actualización de pedido");
    }
}

public void cancelarPedido(Context ctx) {
    try {
     
        int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        int idPedido = Integer.parseInt(ctx.pathParam("id"));
        
        Orden ordenExistente = ordenDAO.buscarOrdenPorIdYNegocio(idPedido, idNegocio);
        
        if (ordenExistente == null) {
           
            ctx.status(404).json(Map.of(
                "error", "Pedido no encontrado o no pertenece al negocio especificado"
            ));
            return;
        }
        
        boolean cancelado = ordenDAO.cancelarOrden(idPedido, idNegocio);

        if (cancelado) {
            
            if (ordenExistente.isEstado()) {
                ordenDAO.aumentarInsumosPorOrden(ordenExistente);
            }
        
            ctx.status(200).json(Map.of(
                "idOrden", idPedido,
                "mensaje", "Pedido cancelado exitosamente"
            ));
        } else {
            ctx.status(400).json(Map.of(
                "error", "No se pudo cancelar el pedido"
            ));
        }
        
    } catch (NumberFormatException e) {
        
        ctx.status(400).json(Map.of(
            "error", "ID de negocio o pedido inválido"
        ));
    } catch (SQLException e) {
       
        e.printStackTrace();
        ctx.status(500).json(Map.of(
            "error", "Error de base de datos al cancelar pedido",
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
   
        e.printStackTrace();
        ctx.status(500).json(Map.of(
            "error", "Error al procesar la solicitud",
            "detalle", e.getMessage()
        ));
    } 
}

    public void listarVentas(Context ctx) {
        try {
            int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
            
            List<Orden> ventas = ordenDAO.listarVentasPorNegocio(idNegocio);
          
            double totalVentas = ventas.stream()
                .mapToDouble(Orden::getTotal)
                .sum();
            
            ctx.status(200).json(Map.of(
                "ventas", ventas,
                "totalVentas", totalVentas,
                "cantidadVentas", ventas.size(),
                "idNegocio", idNegocio
            ));
            
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "error", "ID de negocio inválido"
            ));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error de base de datos al obtener ventas",
                "detalle", e.getMessage()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                "error", "Error al procesar la solicitud",
                "detalle", e.getMessage()
            ));
        }
    }

    public void generarTicketPDF(Context ctx) {
    try {

        String idOrdenParam = ctx.pathParam("id");
        String idNegocioParam = ctx.pathParam("id_negocio");
        
        if (idOrdenParam == null || idNegocioParam == null) {
            ctx.status(400).json(Map.of(
                "status", "error",
                "message", "Parámetros faltantes",
                "required_params", List.of("id", "id_negocio")
            ));
            return;
        }

        int idOrden;
        int idNegocio;
        try {
            idOrden = Integer.parseInt(idOrdenParam);
            idNegocio = Integer.parseInt(idNegocioParam);
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                "status", "error",
                "message", "Formato inválido",
                "details", "Los parámetros deben ser números enteros"
            ));
            return;
        }

        Orden orden = ordenDAO.buscarOrdenPorIdYNegocio(idOrden, idNegocio);
        if (orden == null) {
            ctx.status(404).json(Map.of(
                "status", "not_found",
                "message", "Recurso no encontrado",
                "details", String.format("Orden %d no existe en negocio %d", idOrden, idNegocio)
            ));
            return;
        }

        List<DetalleOrden> detalles = ordenDAO.obtenerDetallesOrden(idOrden);
        byte[] pdf = ExportarTicket.generarTicket(orden, detalles);

        ctx.contentType("application/pdf")
           .header("Content-Disposition", "inline; filename=ticket_" + idOrden + ".pdf")
           .result(pdf);

    } catch (Exception e) {
        System.err.println("[ERROR] Al generar ticket: " + e.getMessage());
        e.printStackTrace();
        
        ctx.status(500).json(Map.of(
            "status", "server_error",
            "message", "Error en el servidor",
            "error", e.getClass().getSimpleName()
        ));
    }
}
}