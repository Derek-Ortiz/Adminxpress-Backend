package gradlep.rutas;


import gradlep.controlador.ControladorOrden;
import io.javalin.Javalin;

public class OrdenRutas {
      private final ControladorOrden controladorOrden;
      
        public OrdenRutas(ControladorOrden controladorOrden){
            this.controladorOrden = controladorOrden;
        }
        public void registro(Javalin app) {

          //ruta para pedidos
        app.get("/api/negocio/{id_negocio}/pedidos", controladorOrden::listarPedidos);
        app.post("/api/negocio/{id_negocio}/pedidos", controladorOrden::crearOrden);
        app.get("/api/negocio/{id_negocio}/pedidos/{id}", controladorOrden::generarTicketPDF);
        
        app.put("/api/negocio/{id_negocio}/pedidos/{id}", controladorOrden::actualizarPedido);
        app.delete("/api/negocio/{id_negocio}/pedidos/{id}", controladorOrden::cancelarPedido);

        //ruta para ventas
        app.get("/api/negocio/{id_negocio}/ventas", controladorOrden::listarVentas);
        
        }
}
