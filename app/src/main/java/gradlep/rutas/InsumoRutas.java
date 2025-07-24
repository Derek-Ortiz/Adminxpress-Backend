package gradlep.rutas;

import gradlep.controlador.ControladorInsumo;

import io.javalin.Javalin;

public class InsumoRutas {
    private final ControladorInsumo controladorInsumo;


    public InsumoRutas(ControladorInsumo controller){
            
            this.controladorInsumo = controller;
        }

        public void registro(Javalin app) {
    
            //app.get("/api/insumos", controladorInsumo::listarInsumos);
            app.post("/api/negocio/{id_negocio}/insumos", controladorInsumo::agregarInsumo);
            app.get("/api/negocio/{id_negocio}/insumos/{id}", controladorInsumo::buscarInsumo);
            app.put("/api/negocio/{id_negocio}/insumos/{id}", controladorInsumo::actualizarInsumo);
            app.delete("/api/negocio/{id_negocio}/insumos/{id}", controladorInsumo::eliminarInsumo);
            app.get("/api/negocio/{id_negocio}/insumos", controladorInsumo::listarInsumosPorNegocio);
            app.get("/api/negocio/{id_negocio}/insumos/{id}/historial", controladorInsumo::obtenerHistorialInsumo);
            app.post("/api/negocio/{id_negocio}/insumos/{id}/movimiento", controladorInsumo::registrarMovimientoStock);
        
        }

}
        