package gradlep.controlador;

import io.javalin.http.Context;
import gradlep.modelo.Insumo;
import gradlep.repositorios.InsumoDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ControladorInsumo {
    private final InsumoDAO insumoDAO;

    public ControladorInsumo(InsumoDAO insumoDAO) {
        this.insumoDAO = insumoDAO;
    }


    public void listarInsumos(Context ctx) {
        try {
            List<Insumo> insumos = insumoDAO.listarInsumos();
            ctx.status(200).json(insumos);
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al listar insumos",
                "detalle", e.getMessage()
            ));
        }
    }

   public void listarInsumosPorNegocio(Context ctx) {
    try {
        int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        List<Insumo> insumos = insumoDAO.listarPorNegocio(idNegocio);
        
       
        for (Insumo insumo : insumos) {
            double stock = insumoDAO.obtenerStock(insumo.getId());
            insumo.setStock((int) stock);
            
            Map<String, Object> infoStock = insumoDAO.obtenerInfoStock(insumo.getId());
            if (infoStock != null) {
                insumo.setPrecio((double) infoStock.get("precio_compra"));
                insumo.setCaducidad((String) infoStock.get("caducidad"));
            }
        }
        
        ctx.status(200).json(insumos);
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of(
            "error", "ID de negocio inválido",
            "detalle", e.getMessage()
        ));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error al obtener insumos del negocio",
            "detalle", e.getMessage()
        ));
    }
}



    public void agregarInsumo(Context ctx) {
        try {
            Insumo insumo = ctx.bodyAsClass(Insumo.class);
            insumoDAO.agregarInsumo(insumo);
            ctx.status(201).json(insumo);
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al crear insumo",
                "detalle", e.getMessage()
            ));
        }
    }

    public void actualizarInsumo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Insumo insumo = ctx.bodyAsClass(Insumo.class);
            insumo.setId(id); 
            
            insumoDAO.editar(insumo);
            ctx.status(200).json(insumo);
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al actualizar insumo",
                "detalle", e.getMessage()
            ));
        }
    }


    public void buscarInsumo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Insumo insumo = insumoDAO.buscarPorId(id);
            
            if (insumo != null) {
                ctx.status(200).json(insumo);
            } else {
                ctx.status(404).json(Map.of("error", "Insumo no encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al buscar insumo",
                "detalle", e.getMessage()
            ));
        }
    }


    public void eliminarInsumo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            insumoDAO.eliminarInsumo(id);
            ctx.status(204);
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al eliminar insumo",
                "detalle", e.getMessage()
            ));
        }
    }

    public void obtenerHistorialInsumo(Context ctx) {
    try {
        int idNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        int idInsumo = Integer.parseInt(ctx.pathParam("id"));
        
        List<Map<String, Object>> historial = insumoDAO.obtenerHistorialInsumo(idInsumo, idNegocio);
        ctx.status(200).json(historial);
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "ID inválido"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error al obtener el historial",
            "detalle", e.getMessage()
        ));
    }
}

public void registrarMovimientoStock(Context ctx) {
    try {
        
        int idInsumo = Integer.parseInt(ctx.pathParam("id"));
        
        Map<String, Object> movimiento = ctx.bodyAsClass(Map.class);
        
       
        if (!movimiento.containsKey("stock") || !movimiento.containsKey("precio")) {
            ctx.status(400).json(Map.of("error", "Faltan campos requeridos (stock, precio)"));
            return;
        }
        
        float stock = Float.parseFloat(movimiento.get("stock").toString());
        float precio = Float.parseFloat(movimiento.get("precio").toString());
        String caducidad = movimiento.containsKey("caducidad") ? movimiento.get("caducidad").toString() : null;
        
       
        insumoDAO.registrarMovimientoStock(idInsumo, stock, precio, caducidad);
    
        insumoDAO.actualizarStockTotal(idInsumo);
        
        ctx.status(201).json(Map.of("message", "Movimiento registrado"));
        
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "Valores numéricos inválidos"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of("error", "Error en la base de datos"));
    }
}


}