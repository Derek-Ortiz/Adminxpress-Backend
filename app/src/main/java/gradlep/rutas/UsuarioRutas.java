package gradlep.rutas;

import gradlep.controlador.ControladorUsuario;
import io.javalin.Javalin;

public class UsuarioRutas {
    
        private final ControladorUsuario controladorUsuario;
      

        public UsuarioRutas(ControladorUsuario controladorUsuario){
            this.controladorUsuario = controladorUsuario;
        }

        public void registro(Javalin app) {
        
        app.get("/prueba", ctx -> ctx.result("API adminXpress funcionando"));

        app.post("/api/usuarios/login/Administrador", controladorUsuario::loginAdmi);
         app.post("/api/usuarios/login/Cajero", controladorUsuario::loginCajero);
        app.post("/api/usuarios/registroAdmin", controladorUsuario::registrarAdmi);
        
        app.get("/api/negocio/{id_negocio}/usuarios", controladorUsuario::listarUsuariosPorNegocio);
        
        app.post("/api/usuarios", controladorUsuario::registrarUsuario);
        app.put("/api/usuarios/{id}", controladorUsuario::actualizarUsuario);
        app.delete("/api/usuarios/{id}", controladorUsuario::borrarUsuario);

        }

    }
    
        