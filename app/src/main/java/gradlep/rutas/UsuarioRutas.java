package gradlep.rutas;

import gradlep.controlador.ControladorUsuario;
import io.javalin.Javalin;

public class UsuarioRutas {
    
        private final ControladorUsuario controladorUsuario;
      

        public UsuarioRutas(ControladorUsuario controladorUsuario){
            this.controladorUsuario = controladorUsuario;
        }

        public void registro(Javalin app) {
           
        // Ruta para verificar el servidor
        app.get("/prueba", ctx -> ctx.result("API adminXpress funcionando"));

        // Ruta para login
        app.post("/api/usuarios/login/Administrador", controladorUsuario::loginAdmi);
         app.post("/api/usuarios/login/Cajero", controladorUsuario::loginCajero);
        app.post("/api/usuarios/registroAdmin", controladorUsuario::registrarAdmi);
        
        app.get("/api/negocio/{id_negocio}/usuarios", controladorUsuario::listarUsuariosPorNegocio);
        
        app.post("/api/usuarios", controladorUsuario::registrarUsuario);
        app.put("/api/usuarios/{id}", controladorUsuario::actualizarUsuario);
        app.delete("/api/usuarios/{id}", controladorUsuario::borrarUsuario);

        }

    }
    
        
    /*
        //ruta para pedidos
        app.get("/api/pedidos", PedidoController::listarPedidos);
        app.post("/api/pedidos", PedidoController::crearPedido);
        app.put("/api/pedidos/:id", PedidoController::actualizarPedido);
        app.delete("/api/pedidos/:id", PedidoController::cancelarPedido);

        //ruta para ventas
        app.get("/api/ventas", VentaController::listarVentas);
    }


}
*/