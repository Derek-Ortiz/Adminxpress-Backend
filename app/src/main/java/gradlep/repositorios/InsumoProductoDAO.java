package gradlep.repositorios;

import gradlep.modelo.Insumo;
import gradlep.modelo.InsumoDTO;
import gradlep.modelo.InsumoProducto;
import java.sql.*;
import java.util.*;

public class InsumoProductoDAO {
    private final Connection conexion;

    public InsumoProductoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public List<InsumoProducto> obtenerInsumosPorProducto(int idProducto) throws SQLException {
        List<InsumoProducto> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos_productos WHERE codigo_producto = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InsumoProducto ip = new InsumoProducto(
                    rs.getInt("codigo_producto"),
                    rs.getInt("codigo_insumo"),
                    rs.getDouble("cantidad_usar")
                );
                lista.add(ip);
            }
        }
        return lista;
    }


public void agregarInsumoAProducto(InsumoProducto relacion, int codigoNegocio) throws SQLException {
    String sqlValidacion = """
        SELECT 1 
        FROM productos p
        JOIN insumos i ON i.codigo_negocio = p.codigo_negocio
        WHERE p.id_producto = ? 
        AND i.id_insumos = ?
        AND p.codigo_negocio = ?
    """;
    
    try (PreparedStatement stmt = conexion.prepareStatement(sqlValidacion)) {
        stmt.setInt(1, relacion.getCodigoProducto());
        stmt.setInt(2, relacion.getCodigoInsumo());
        stmt.setInt(3, codigoNegocio);
        
        if (!stmt.executeQuery().next()) {
            throw new SQLException("Producto o insumo no pertenecen al negocio");
        }
    }
    String sqlInsert = "INSERT INTO insumos_productos(codigo_producto, codigo_insumo, cantidad_usar) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conexion.prepareStatement(sqlInsert)) {
        stmt.setInt(1, relacion.getCodigoProducto());
        stmt.setInt(2, relacion.getCodigoInsumo());
        stmt.setDouble(3, relacion.getCantidadUsar());
        stmt.executeUpdate();
    }
}

 
    public void eliminarInsumoDeProducto(int idProducto) throws SQLException {
        String sql = "DELETE FROM insumos_productos WHERE codigo_producto = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            stmt.executeUpdate();
        }
    }
    public Map<Insumo, Double> obtenerRecetaProducto(int idProducto, int codigoNegocio) throws SQLException {
    String sql = """
        SELECT i.id_insumos, i.nom_producto, i.unidad_medida, i.min_stock, i.estado,
               ip.cantidad_usar, i.codigo_negocio  
        FROM insumos_productos as ip
        JOIN insumos as i ON i.id_insumos = ip.codigo_insumo
        WHERE ip.codigo_producto = ? AND i.codigo_negocio = ?
    """;
    
    Map<Insumo, Double> receta = new LinkedHashMap<>();
    
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idProducto);
        stmt.setInt(2, codigoNegocio);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Insumo ins = new Insumo(
                rs.getInt("id_insumos"),
                rs.getString("nom_producto"),
                rs.getString("unidad_medida"),
                rs.getDouble("min_stock"),
                rs.getString("estado"),
                rs.getInt("codigo_negocio")
            );
            receta.put(ins, rs.getDouble("cantidad_usar"));
        }
    } catch (SQLException e) {
        throw e;
    }
    return receta;
}


    public List<InsumoDTO> obtenerInsumosConDetalle(int idProducto, int codigoNegocio) throws SQLException {
    String sql = """
        SELECT i.id_insumos, i.nom_producto, i.unidad_medida, ip.cantidad_usar, i.codigo_negocio
        FROM insumos_productos as ip
        JOIN insumos as i ON ip.codigo_insumo = i.id_insumos
        WHERE ip.codigo_producto = ? 
        AND i.codigo_negocio = ? 
    """;
    
    List<InsumoDTO> resultados = new ArrayList<>();
    
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idProducto);
        stmt.setInt(2, codigoNegocio);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            resultados.add(new InsumoDTO(
                rs.getInt("id_insumos"),
                rs.getString("nom_producto"),
                rs.getString("unidad_medida"),
                rs.getDouble("cantidad_usar")
                
            ));
        }
    }
    return resultados;
}
}