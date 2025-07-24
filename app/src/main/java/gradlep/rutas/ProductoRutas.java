package gradlep.rutas;

import gradlep.controlador.ControladorProducto;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;

public class ProductoRutas {
    
    private final ControladorProducto controladorProducto;


    public ProductoRutas(ControladorProducto controladorProducto){
            
            this.controladorProducto = controladorProducto;
        }

        public void registro(Javalin app) {

        //ruta para productos

        app.get("/api/negocio/{id_negocio}/productos", controladorProducto::listarProductosPorNegocio);
        app.get("/api/negocio/{id_negocio}/productosVentas", controladorProducto::listarProductosVentas);
        app.post("/api/negocio/{id_negocio}/productos", controladorProducto::agregarProducto);
        app.get("/api/negocio/{id_negocio}/productos/{id}", controladorProducto::obtenerProductoPorId);
        app.put("/api/negocio/{id_negocio}/productos/{id}", controladorProducto::actualizarProducto);
        app.delete("/api/negocio/{id_negocio}/productos/{id}", controladorProducto::eliminarProducto);
        
        app.get("/api/negocio/{id_negocio}/insumosParaProductos", controladorProducto::listarInsumosBasicos);
            
    app.get("/api/negocio/{id_negocio}/productos/{id}/insumos", controladorProducto::obtenerInsumosProducto);
    app.post("/api/negocio/{id_negocio}/productos/{id}/insumos", controladorProducto::agregarInsumoAProducto);
    app.delete("/api/negocio/{id_negocio}/productos/{id}/insumos", controladorProducto::eliminarInsumosDeProducto);

        }
}
        /*app.get("/api/negocio/{id_negocio}/productos/{id}/insumos", controladorProducto::listarInsumosProducto);
        app.post("/api/negocio/{id_negocio}/productos/{id}/insumos", controladorProducto::agregarInsumosProducto);
        app.put("/api/negocio/{id_negocio}/productos/{id}/insumos/{id}", controladorProducto::editarInsumosProducto);
        app.delete("/api/negocio/{id_negocio}/productos/{id}/insumos/{id}", controladorProducto::borrarInsumosProducto);
        */
        


    
   

