package gradlep.repositorios;

import gradlep.modelo.Negocio;
import gradlep.modelo.Usuario;

import java.sql.*;
import java.util.*;

public class UsuarioDAO {
    private Connection conexion;

    public UsuarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public List<Usuario> listarUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Statement stmt = conexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("usuario"),
                    rs.getString("nombre"),
                    rs.getString("apellido_p"),
                    rs.getString("apellido_m"),
                    rs.getString("CURP"),
                    rs.getString("contrasenia"),
                    rs.getString("cargo"),
                    rs.getInt("codigo_negocio")
                );
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }


    public List<Usuario> listarUsuariosPorNegocio(String codigoNegocio) throws SQLException {
    List<Usuario> usuarios = new ArrayList<>();
    String sql = "SELECT * FROM usuarios WHERE codigo_negocio = ?";

    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setString(1, codigoNegocio);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("usuario"),
                    rs.getString("nombre"),
                    rs.getString("apellido_p"),
                    rs.getString("apellido_m"),
                    rs.getString("curp"),
                    rs.getString("contrasenia"),
                    rs.getString("cargo"),
                    rs.getInt("codigo_negocio")
                );
                usuarios.add(usuario);
            }
        }
    }

    return usuarios;
}


    public void agregarAdmi(Usuario usuario, Negocio negocio) throws SQLException {
    boolean originalAutoCommit = conexion.getAutoCommit();
    try {
        conexion.setAutoCommit(false); // Iniciar transacción

        // 1. Insertar negocio y obtener ID
        String sqlNegocio = "INSERT INTO negocio (nombre) VALUES (?)";
        try (PreparedStatement stmtNegocio = conexion.prepareStatement(
                sqlNegocio, Statement.RETURN_GENERATED_KEYS)) {
            
            stmtNegocio.setString(1, negocio.getNombre());
            int affectedRows = stmtNegocio.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el negocio");
            }

            // Obtener el ID generado
            try (ResultSet generatedKeys = stmtNegocio.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int nuevoIdNegocio = generatedKeys.getInt(1);
                    System.out.println("ID de negocio generado: " + nuevoIdNegocio);
                    
                    // 2. Insertar usuario con el nuevo ID - CORRECCIÓN AQUÍ
                    String sqlUsuario = "INSERT INTO usuarios (usuario, nombre, apellido_p, apellido_m, curp, cargo, contrasenia, codigo_negocio) "
                                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmtUsuario = conexion.prepareStatement(sqlUsuario)) {
                        stmtUsuario.setString(1, usuario.getUsuario());
                        stmtUsuario.setString(2, usuario.getNombre());
                        stmtUsuario.setString(3, usuario.getApellidoPaterno());
                        stmtUsuario.setString(4, usuario.getApellidoMaterno());
                        stmtUsuario.setString(5, usuario.getCurp());
                        stmtUsuario.setString(6, usuario.getCargo());
                        stmtUsuario.setString(7, usuario.getContrasena()); // El getter puede mantenerese como getContrasena()
                        stmtUsuario.setInt(8, nuevoIdNegocio);
                        
                        stmtUsuario.executeUpdate();
                        System.out.println("Usuario insertado con código_negocio: " + nuevoIdNegocio);
                    }
                } else {
                    throw new SQLException("No se obtuvo el ID generado para el negocio");
                }
            }
        }
        conexion.commit();
    } catch (SQLException e) {
        conexion.rollback();
        System.err.println("Error en transacción: " + e.getMessage());
        throw e;
    } finally {
        conexion.setAutoCommit(originalAutoCommit);
    }
}

    public void agregarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (usuario, nombre, apellido_p, apellido_m, curp, cargo, contrasenia, codigo_negocio) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsuario());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellidoPaterno());
            stmt.setString(4, usuario.getApellidoMaterno());
            stmt.setString(5, usuario.getCurp());
            stmt.setString(6, usuario.getCargo());
            stmt.setString(7, usuario.getContrasena());
            stmt.setInt(8, usuario.getCodigoNegocio());
            stmt.executeUpdate();
        }
    }


    public Usuario buscarAdmiPorUsuarioYContrasena(String usuario, String contrasena) throws SQLException {
    String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contrasenia = ? AND cargo = 'administrador' ";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setString(1, usuario);
        stmt.setString(2, contrasena);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("usuario"),
                    rs.getString("nombre"),
                    rs.getString("apellido_p"),
                    rs.getString("apellido_m"),
                    rs.getString("CURP"),
                    rs.getString("contrasenia"),
                    rs.getString("cargo"),
                    rs.getInt("codigo_negocio")
                );  
            } else {
                return null;

            }
            
        }
        
        
    }
}

 public Usuario buscarPorUsuarioYContrasena(String usuario, String contrasena) throws SQLException {
    String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contrasenia = ? ";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setString(1, usuario);
        stmt.setString(2, contrasena);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("usuario"),
                    rs.getString("nombre"),
                    rs.getString("apellido_p"),
                    rs.getString("apellido_m"),
                    rs.getString("CURP"),
                    rs.getString("contrasenia"),
                    rs.getString("cargo"),
                    rs.getInt("codigo_negocio")
                );  
            } else {
                return null;

            }
            
        }
        
        
    }
}



    public void editarUsuario(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET usuario=?, nombre=?, apellido_p=?, apellido_m=?, curp=?, contrasenia=?, cargo=? WHERE id_usuario=?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario.getUsuario());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellidoPaterno());
            stmt.setString(4, usuario.getApellidoMaterno());
            stmt.setString(5, usuario.getCurp());
            stmt.setString(6, usuario.getContrasena());
            stmt.setString(7, usuario.getCargo());
            stmt.setInt(8, usuario.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminarUsuarioPorId(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
