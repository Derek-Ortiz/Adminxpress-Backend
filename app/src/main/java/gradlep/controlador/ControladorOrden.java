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

    // POST /api/negocio/{id_negocio}/pedidos - Crear orden (mantener como está)
    public void crearOrden(Context ctx) {
        try {
            // Obtener el ID del negocio de la URL
            int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
            
            // Parsear el cuerpo de la petición
            Orden orden = ctx.bodyAsClass(Orden.class);
            
            // Establecer el ID del negocio en la orden
            orden.setIdNegocio(idNegocio);
            
            // Validaciones básicas
            if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "La orden debe contener al menos un detalle"
                ));
                return;
            }

            // Validar y obtener precios actuales de los productos
            boolean preciosValidos = ordenDAO.validarYActualizarPrecios(orden);
            if (!preciosValidos) {
                ctx.status(400).json(Map.of(
                    "error", "Algunos productos no existen o no están disponibles"
                ));
                return;
            }

            // Calcular y establecer el total
            orden.setTotal(orden.calcularTotal());
            System.out.println("total" + orden.getTotal());
            
            // Guardar la orden
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

    // GET /api/negocio/{id_negocio}/pedidos - Listar pedidos
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

    // PUT /api/negocio/{id_negocio}/pedidos/:id - Actualizar pedido
    public void actualizarPedido(Context ctx) {
    try {
        // DEBUG: Mostrar inicio del proceso
        System.out.println("Iniciando actualización de pedido...");
        
        // Obtener IDs de path parameters
        int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        int idPedido = Integer.parseInt(ctx.pathParam("id"));
        
        // DEBUG: Mostrar IDs recibidos
        System.out.println("ID Negocio: " + idNegocio + ", ID Pedido: " + idPedido);

        // Verificar que el pedido existe y pertenece al negocio
        Orden ordenExistente = ordenDAO.buscarOrdenPorIdYNegocio(idPedido, idNegocio);
        
        // DEBUG: Mostrar resultado de búsqueda de orden
        System.out.println("Orden existente encontrada: " + (ordenExistente != null));
        
        if (ordenExistente == null) {
            // DEBUG: Mostrar error de orden no encontrada
            System.out.println("Error: Pedido no encontrado o no pertenece al negocio");
            ctx.status(404).json(Map.of(
                "error", "Pedido no encontrado o no pertenece al negocio especificado"
            ));
            return;
        }

        // Parsear el cuerpo de la petición
        Orden ordenActualizada = ctx.bodyAsClass(Orden.class);
        
        // DEBUG: Mostrar datos recibidos en el body
        System.out.println("Datos recibidos para actualización: " + ordenActualizada.toString());

        // Establecer IDs
        ordenActualizada.setId(idPedido);
        ordenActualizada.setIdNegocio(idNegocio);
        
        // Validaciones básicas
        if (ordenActualizada.getDetalles() == null || ordenActualizada.getDetalles().isEmpty()) {
            // DEBUG: Mostrar error de detalles vacíos
            System.out.println("Error: La orden no tiene detalles");
            ctx.status(400).json(Map.of(
                "error", "La orden debe contener al menos un detalle"
            ));
            return;
        }

        // Validar y obtener precios actuales de los productos
        boolean preciosValidos = ordenDAO.validarYActualizarPrecios(ordenActualizada);
        
        // DEBUG: Mostrar resultado de validación de precios
        System.out.println("Precios válidos: " + preciosValidos);
        
        if (!preciosValidos) {
            // DEBUG: Mostrar error de productos no disponibles
            System.out.println("Error: Productos no existen o no disponibles");
            ctx.status(400).json(Map.of(
                "error", "Algunos productos no existen o no están disponibles"
            ));
            return;
        }
        
        // Guardar estado anterior para comparación
        boolean estadoAnterior = ordenExistente.isEstado();
        
        // DEBUG: Mostrar estado anterior y nuevo estado
        System.out.println("Estado anterior: " + estadoAnterior + ", Nuevo estado: " + ordenActualizada.isEstado());

        // Calcular y establecer el total
        double totalCalculado = ordenActualizada.calcularTotal();
        ordenActualizada.setTotal(totalCalculado);
        
        // DEBUG: Mostrar total calculado
        System.out.println("Total calculado: " + totalCalculado);

        // Actualizar la orden
        boolean actualizado = ordenDAO.actualizarOrden(ordenActualizada);
        
        // DEBUG: Mostrar resultado de actualización
        System.out.println("Orden actualizada en BD: " + actualizado);

        if (actualizado) {
            // Manejar cambios de estado
            if (estadoAnterior != ordenActualizada.isEstado()) {
                // DEBUG: Mostrar cambio de estado detectado
                System.out.println("Detectado cambio de estado, procesando...");
                
                if (ordenActualizada.isEstado()) {
                    // Si cambió a completado: reducir insumos
                    System.out.println("Reduciendo insumos por orden completada...");
                    ordenDAO.reducirInsumosPorOrden(ordenActualizada);
                } else if (estadoAnterior) {
                    // Si cambió de completado a cancelado: revertir insumos
                    System.out.println("Revirtiendo insumos por orden cancelada...");
                    ordenDAO.aumentarInsumosPorOrden(ordenActualizada);
                }
            }

            // DEBUG: Mostrar éxito en la operación
            System.out.println("Pedido actualizado exitosamente");
            ctx.status(200).json(Map.of(
                "idOrden", idPedido,
                "total", ordenActualizada.getTotal(),
                "mensaje", "Pedido actualizado exitosamente"
            ));
        } else {
            // DEBUG: Mostrar fallo en actualización
            System.out.println("Error: No se pudo actualizar el pedido en BD");
            ctx.status(400).json(Map.of(
                "error", "No se pudo actualizar el pedido"
            ));
        }
        
    } catch (NumberFormatException e) {
        // DEBUG: Mostrar error de formato numérico
        System.out.println("Error de formato numérico: " + e.getMessage());
        ctx.status(400).json(Map.of(
            "error", "ID de negocio o pedido inválido"
        ));
    } catch (SQLException e) {
        // DEBUG: Mostrar error de SQL
        System.out.println("Error de SQL: " + e.getMessage());
        ctx.status(500).json(Map.of(
            "error", "Error de base de datos al actualizar pedido",
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
        // DEBUG: Mostrar error genérico
        System.out.println("Error inesperado: " + e.getMessage());
        e.printStackTrace();
        ctx.status(400).json(Map.of(
            "error", "Error al procesar la solicitud",
            "detalle", e.getMessage()
        ));
    } finally {
        // DEBUG: Mostrar fin del proceso
        System.out.println("Finalizado proceso de actualización de pedido");
    }
}
    // DELETE /api/negocio/{id_negocio}/pedidos/:id - Cancelar pedido
   
public void cancelarPedido(Context ctx) {
    try {
        System.out.println("[DEBUG] Iniciando cancelación de pedido...");
        
        // Parsear IDs
        System.out.println("[DEBUG] Parseando ID de negocio y pedido...");
        int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        int idPedido = Integer.parseInt(ctx.pathParam("id"));
        System.out.println("[DEBUG] IDs parseados - Negocio: " + idNegocio + ", Pedido: " + idPedido);
        
        // Verificar que el pedido existe y pertenece al negocio
        System.out.println("[DEBUG] Buscando orden en la base de datos...");
        Orden ordenExistente = ordenDAO.buscarOrdenPorIdYNegocio(idPedido, idNegocio);
        
        if (ordenExistente == null) {
            System.out.println("[DEBUG] Orden no encontrada o no pertenece al negocio");
            ctx.status(404).json(Map.of(
                "error", "Pedido no encontrado o no pertenece al negocio especificado"
            ));
            return;
        }
        System.out.println("[DEBUG] Orden encontrada: " + ordenExistente.toString());

        // Cancelar la orden
        System.out.println("[DEBUG] Intentando cancelar la orden...");
        boolean cancelado = ordenDAO.cancelarOrden(idPedido, idNegocio);

        if (cancelado) {
            System.out.println("[DEBUG] Orden cancelada exitosamente. Verificando estado para revertir insumos...");
            // Revertir insumos si la orden estaba activa
            if (ordenExistente.isEstado()) {
                System.out.println("[DEBUG] Orden estaba activa, revirtiendo insumos...");
                ordenDAO.aumentarInsumosPorOrden(ordenExistente);
                System.out.println("[DEBUG] Insumos revertidos exitosamente");
            }
            
            System.out.println("[DEBUG] Enviando respuesta de éxito");
            ctx.status(200).json(Map.of(
                "idOrden", idPedido,
                "mensaje", "Pedido cancelado exitosamente"
            ));
        } else {
            System.out.println("[DEBUG] Falló la cancelación en la base de datos");
            ctx.status(400).json(Map.of(
                "error", "No se pudo cancelar el pedido"
            ));
        }
        
    } catch (NumberFormatException e) {
        System.out.println("[ERROR] Error al parsear IDs: " + e.getMessage());
        ctx.status(400).json(Map.of(
            "error", "ID de negocio o pedido inválido"
        ));
    } catch (SQLException e) {
        System.out.println("[ERROR] Error de base de datos: " + e.getMessage());
        e.printStackTrace();
        ctx.status(500).json(Map.of(
            "error", "Error de base de datos al cancelar pedido",
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
        System.out.println("[ERROR] Error inesperado: " + e.getMessage());
        e.printStackTrace();
        ctx.status(500).json(Map.of(
            "error", "Error al procesar la solicitud",
            "detalle", e.getMessage()
        ));
    } finally {
        System.out.println("[DEBUG] Finalizado proceso de cancelación de pedido");
    }
}

    // GET /api/negocio/{id_negocio}/ventas - Listar ventas
    public void listarVentas(Context ctx) {
        try {
            int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
            
            List<Orden> ventas = ordenDAO.listarVentasPorNegocio(idNegocio);
            
            // Calcular totales
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
        System.out.println("[TicketPDF] Path completo: " + ctx.path());
        System.out.println("[TicketPDF] Parámetros: " + ctx.pathParamMap());

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