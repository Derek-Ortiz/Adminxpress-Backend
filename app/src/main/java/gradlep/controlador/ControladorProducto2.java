/*package gradlep.controlador;

import io.javalin.http.Context; 
import gradlep.repositorios.ProductoDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import gradlep.modelo.Producto;
import io.javalin.http.UploadedFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ControladorProducto2 {

    private final ProductoDAO productoDAO;

     public ControladorProducto(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }


    public void agregarProducto(Context ctx) {
        try {
            Producto producto = ctx.bodyAsClass(Producto.class);

            // Manejo de imagen
            UploadedFile imagen = ctx.uploadedFile("imagen");
            if (imagen != null) {
                String carpetaImagenes = "app/src/main/java/gradlep/imagenes/";
                Files.createDirectories(Paths.get(carpetaImagenes));
                String nombreArchivo = System.currentTimeMillis() + "_" + imagen.filename();
                Path rutaDestino = Paths.get(carpetaImagenes, nombreArchivo);
                Files.copy(imagen.content(), rutaDestino);

                // Guardar la ruta relativa en el producto
                producto.setImagen(rutaDestino.toString());
            }

            productoDAO.agregarProducto(producto);
            ctx.status(201).json(producto);
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                    "error", "Error al crear producto",
                    "detalle", e.getMessage()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "error", "Error al guardar imagen",
                    "detalle", e.getMessage()
            ));
        }
    }


    public void actualizarProducto(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Producto producto = ctx.bodyAsClass(Producto.class);
            producto.setId(id);

            productoDAO.editarProducto(producto);
            ctx.status(200).json(producto);
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "ID debe ser numérico"));
        } catch (SQLException e) {
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


    public void listarProductos(Context ctx) {
        try {
            List<Producto> productos = productoDAO.listarProductos();
            ctx.status(200).json(productos);
        } catch (SQLException e) {
            ctx.status(500).json(Map.of(
                "error", "Error al listar productos",
                "detalle", e.getMessage()
            ));
        }
    }

}*/