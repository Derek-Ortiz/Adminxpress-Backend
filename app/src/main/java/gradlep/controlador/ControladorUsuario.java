package gradlep.controlador;

import gradlep.modelo.Negocio;
import gradlep.modelo.RegistroDTO;
import gradlep.modelo.Usuario;
import gradlep.repositorios.UsuarioDAO;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ControladorUsuario {
    private final UsuarioDAO usuarioDAO;

    public ControladorUsuario(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

   public void loginAdmi(Context ctx) {
    try {
        Map<String, String> datos = ctx.bodyAsClass(Map.class);
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");

        if (usuario == null || usuario.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            ctx.status(400).json(Map.of(
                "error", "Datos incompletos",
                "detalle", "Usuario y contraseña son requeridos"
            ));
            return;
        }

        Usuario usuarioEncontrado = usuarioDAO.buscarAdmiPorUsuarioYContrasena(usuario, contrasena);
        if (usuarioEncontrado != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id_empleado", usuarioEncontrado.getId());
            response.put("usuario", usuarioEncontrado.getUsuario());
            response.put("nombre", usuarioEncontrado.getNombre());
            response.put("apellido_p", usuarioEncontrado.getApellidoPaterno());
            response.put("apellido_m", usuarioEncontrado.getApellidoMaterno());
            response.put("CURP", usuarioEncontrado.getCurp());
            response.put("cargo", usuarioEncontrado.getCargo());
            response.put("codigo_negocio", usuarioEncontrado.getCodigoNegocio());
            
            ctx.status(200).json(response);
        } else {
            ctx.status(401).json(Map.of("error", "Credenciales incorrectas"));
        }
    } catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error en la base de datos",
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
        ctx.status(400).json(Map.of(
            "error", "Error en la solicitud de login",
            "detalle", e.getMessage()
        ));
    }
}

   public void loginCajero(Context ctx) {
    try {
        Map<String, String> datos = ctx.bodyAsClass(Map.class);
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");

        if (usuario == null || usuario.isEmpty() || contrasena == null || contrasena.isEmpty()) {
            ctx.status(400).json(Map.of(
                "error", "Datos incompletos",
                "detalle", "Usuario y contraseña son requeridos"
            ));
            return;
        }

        Usuario usuarioEncontrado = usuarioDAO.buscarPorUsuarioYContrasena(usuario, contrasena);
        if (usuarioEncontrado != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id_empleado", usuarioEncontrado.getId());
            response.put("usuario", usuarioEncontrado.getUsuario());
            response.put("nombre", usuarioEncontrado.getNombre());
            response.put("apellido_p", usuarioEncontrado.getApellidoPaterno());
            response.put("apellido_m", usuarioEncontrado.getApellidoMaterno());
            response.put("CURP", usuarioEncontrado.getCurp());
            response.put("cargo", usuarioEncontrado.getCargo());
            response.put("codigo_negocio", usuarioEncontrado.getCodigoNegocio());
            
            ctx.status(200).json(response);
        } else {
            ctx.status(401).json(Map.of("error", "Credenciales incorrectas"));
        }
    } catch (SQLException e) {
        ctx.status(500).json(Map.of(
            "error", "Error en la base de datos",
            "detalle", e.getMessage()
        ));
    } catch (Exception e) {
        ctx.status(400).json(Map.of(
            "error", "Error en la solicitud de login",
            "detalle", e.getMessage()
        ));
    }
}

    public void listarUsuariosPorNegocio(Context ctx) {
    try {
        String codigoNegocio = ctx.pathParam("id_negocio");  // lectura del path param
        System.out.println("id del negocio a listar los usuarios: " + codigoNegocio);
        List<Usuario> usuarios = usuarioDAO.listarUsuariosPorNegocio(codigoNegocio);
        ctx.json(usuarios);
    } catch (SQLException e) {
        ctx.status(500).json(Map.of("error", "Error al obtener usuarios", "detalle", e.getMessage()));
    }
}

    public void listarUsuarios(Context ctx) {
    try {
        String codigo = ctx.queryParam("codigo"); 
        List<Usuario> usuarios;

        if (codigo != null) {
            usuarios = usuarioDAO.listarUsuariosPorNegocio(codigo);
        } else {
            usuarios = usuarioDAO.listarUsuarios(); 
        }

        ctx.json(usuarios);
    } catch (SQLException e) {
        ctx.status(500).json(Map.of("error", "Error al obtener usuarios", "detalle", e.getMessage()));
    }
}

    public void registrarAdmi(Context ctx) {
    try {
        RegistroDTO registro = ctx.bodyAsClass(RegistroDTO.class);
        if (registro.getCompany() == null || registro.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es requerido");
        }
        if (registro.getUsername() == null || registro.getPassword() == null) {
            throw new IllegalArgumentException("Usuario y contraseña son requeridos");
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(registro.getUsername());
        usuario.setNombre(registro.getUsername()); 
        usuario.setApellidoPaterno("");
        usuario.setApellidoMaterno("");
        usuario.setCurp("CURPDEPRUEBA123456");
        
        usuario.setCargo("Administrador");
        usuario.setContrasena(registro.getPassword());
        
        Negocio negocio = new Negocio();
        negocio.setNombre(registro.getCompany());

        usuarioDAO.agregarAdmi(usuario, negocio);
        
        ctx.status(201).json(Map.of(
            "mensaje", "Usuario registrado correctamente",
            "empresa", registro.getCompany()
        ));
    } catch (Exception e) {
        ctx.status(400).json(Map.of(
            "error", "Error al registrar usuario",
            "detalle", e.getMessage()
        ));
    }
}

    public void registrarUsuario(Context ctx) {
        try {
            Usuario usuario = ctx.bodyAsClass(Usuario.class);
            System.out.println("cargo del usuario: " + usuario.getCargo() + "codigo de negocio: "+ usuario.getCodigoNegocio());
            usuarioDAO.agregarUsuario(usuario);
            ctx.status(201).json(Map.of("mensaje", "Usuario registrado correctamente"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Error al registrar usuario", "detalle", e.getMessage()));
        }
    }

    public void actualizarUsuario(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Usuario usuario = ctx.bodyAsClass(Usuario.class);
            usuario.setId(id);
            usuarioDAO.editarUsuario(usuario);
            ctx.status(200).json(Map.of("mensaje", "Usuario actualizado correctamente"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Error al actualizar usuario", "detalle", e.getMessage()));
        }
    }

    public void borrarUsuario(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            usuarioDAO.eliminarUsuarioPorId(id);
            ctx.status(200).json(Map.of("mensaje", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", "Error al eliminar usuario", "detalle", e.getMessage()));
        }
    }
}
