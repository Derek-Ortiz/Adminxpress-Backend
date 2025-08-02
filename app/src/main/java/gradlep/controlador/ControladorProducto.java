package gradlep.controlador;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import gradlep.repositorios.InsumoProductoDAO;
import gradlep.repositorios.ProductoDAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gradlep.modelo.Insumo;
import gradlep.modelo.InsumoDTO;
import gradlep.modelo.InsumoProducto;
import gradlep.modelo.Producto;

public class ControladorProducto {

    private final ProductoDAO productoDAO;
    private final InsumoProductoDAO insumoProductoDAO;

    public ControladorProducto(ProductoDAO productoDAO, InsumoProductoDAO insumoProductoDAO) {
    this.productoDAO = productoDAO;
    this.insumoProductoDAO = insumoProductoDAO;
}

public void agregarProducto(Context ctx) {
    try {
       
        String productoJson = ctx.formParam("producto");
        Producto producto;
        
        if (productoJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            producto = mapper.readValue(productoJson, Producto.class);
        } else {
           
            producto = new Producto();
            producto.setNombre(ctx.formParam("nombre"));
            producto.setDescripcion(ctx.formParam("descripcion"));
            producto.setPrecioActual(Double.parseDouble(ctx.formParam("precio")));
            producto.setTipo(ctx.formParam("categoria"));
        }

        UploadedFile imagen = ctx.uploadedFile("imagen");
        
        if (imagen != null) {
            String carpetaImagenes = "/home/ubuntu/integrador-back2/app/src/main/java/gradlep/uploads/";
            Files.createDirectories(Paths.get(carpetaImagenes));
            String nombreArchivo = System.currentTimeMillis() + "_" + imagen.filename();
            Path rutaDestino = Paths.get(carpetaImagenes, nombreArchivo);
            Files.copy(imagen.content(), rutaDestino);

            producto.setImagen("uploads/" + nombreArchivo);
            System.out.println("Imagen: " + producto.getImagen());
        } else {
            producto.setImagen(null); 
        }

        productoDAO.agregarProducto(producto);
        ctx.status(201).json(producto);

    } catch (Exception e) {
        e.printStackTrace();
        ctx.status(500).json(Map.of(
                "error", "Error interno del servidor",
                "detalle", e.getMessage()
        ));
    }
}

public void actualizarProducto(Context ctx) {
    try {
        int id = Integer.parseInt(ctx.pathParam("id"));
      
        String productoJson = ctx.formParam("producto");
        if (productoJson == null) {
            ctx.status(400).json(Map.of("error", "Datos del producto requeridos"));
            return;
        }
   
        ObjectMapper mapper = new ObjectMapper();
        Producto producto = mapper.readValue(productoJson, Producto.class);
        producto.setId(id);

        UploadedFile imagen = ctx.uploadedFile("imagen");
        if (imagen != null) {
    
            String carpetaImagenes = "/home/ubuntu/integrador-back2/app/src/main/java/gradlep/uploads/";
            Files.createDirectories(Paths.get(carpetaImagenes));
            String nombreArchivo = System.currentTimeMillis() + "_" + imagen.filename();
            Path rutaDestino = Paths.get(carpetaImagenes, nombreArchivo);
            Files.copy(imagen.content(), rutaDestino);

            producto.setImagen("uploads/" + nombreArchivo);
        } else {
            producto.setImagen(null);
        }

        productoDAO.editarProducto(producto);
        ctx.status(200).json(producto);
        
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
    } catch (JsonParseException e) {
        ctx.status(400).json(Map.of(
            "error", "JSON malformado", 
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
        ctx.status(500).json(Map.of(
            "error", "Error al actualizar producto",
            "detalle", e.getMessage()
        ));
    }
}

    public void eliminarProducto(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            productoDAO.eliminarProducto(id);
            ctx.status(204);
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al eliminar producto",
                "detalle", e.getMessage()
            ));
        }
    }

    public void obtenerProductoPorId(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Producto producto = productoDAO.buscarPorId(id);
            
            if (producto != null) {
                ctx.status(200).json(producto);
            } else {
                ctx.status(404).json(Map.of("error", "Producto no encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al buscar producto",
                "detalle", e.getMessage()
            ));
        }
    }

    public void listarProductosPorNegocio(Context ctx) {
    try {
        int codigoNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        List<Producto> productos = productoDAO.listarPorNegocio(codigoNegocio);
        ctx.json(productos);
        
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "Código de negocio inválido"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of("error", "Error en base de datos"));
    }
}
    public void listarProductosVentas(Context ctx) {
    try {
        int codigoNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        
        List<Producto> productos = productoDAO.listarParaVentas(codigoNegocio);
        ctx.json(productos);
        
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "Código de negocio inválido"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of("error", "Error en base de datos"));
    }
}

 public void listarInsumosBasicos(Context ctx) {
    
        try {
            int codigoNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
            List<Insumo> insumos = productoDAO.listarInsumosBasicos(codigoNegocio);
            ctx.json(insumos);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Código de negocio inválido");
        }catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error al obtener insumos del negocio",
            "detalle", e.getMessage()
        ));
    }
    }


public void obtenerInsumosProducto(Context ctx) {
    try {
        int idProducto = Integer.parseInt(ctx.pathParam("id"));
        int codigoNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        
        Producto producto = productoDAO.buscarPorIdYNegocio(idProducto, codigoNegocio);
        if (producto == null) {
            ctx.status(403).json(Map.of("error", "Acceso denegado: producto no pertenece al negocio"));
            return;
        }

        List<InsumoDTO> insumos = insumoProductoDAO.obtenerInsumosConDetalle(idProducto, codigoNegocio);
        ctx.json(insumos);
        
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of("error", "Error en base de datos"));
    }
}

public void agregarInsumoAProducto(Context ctx) {
    try {
        int idProducto = Integer.parseInt(ctx.pathParam("id"));
        int codigoNegocio = Integer.parseInt(ctx.pathParam("id_negocio"));
        InsumoProducto receta = ctx.bodyAsClass(InsumoProducto.class);
        receta.setCodigoProducto(idProducto);

        if (receta.getCantidadUsar() <= 0) {
            ctx.status(400).json(Map.of("error", "La cantidad debe ser mayor a cero"));
            return;
        }
        
        insumoProductoDAO.agregarInsumoAProducto(receta, codigoNegocio);
        ctx.status(201).json(receta);
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error al agregar insumo al producto",
            "detalle", e.getMessage()
        ));
    }
}

public void eliminarInsumosDeProducto(Context ctx) {
    try {
        int idProducto = Integer.parseInt(ctx.pathParam("id"));
        insumoProductoDAO.eliminarInsumoDeProducto(idProducto);
        ctx.status(204);
    } catch (NumberFormatException e) {
        ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
    } catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error al eliminar insumos del producto",
            "detalle", e.getMessage()
        ));
    }
}

}