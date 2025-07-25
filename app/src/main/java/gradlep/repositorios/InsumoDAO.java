package gradlep.repositorios;

import gradlep.modelo.Insumo;

import java.sql.*;
import java.util.*;
import java.sql.Date;

public class InsumoDAO {
    private final Connection conexion;

    public InsumoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public List<Insumo> listarInsumos() throws SQLException {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearInsumo(rs));
            }
        }
        return lista;
    }


    public Insumo buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM insumos WHERE id_insumos = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearInsumo(rs);
            }
        }
        return null;
    }

    public void agregarInsumo(Insumo insumo) throws SQLException {
    String sqlInsumo = "INSERT INTO insumos (nom_producto, unidad_medida, min_stock, estado, codigo_negocio) VALUES (?, ?, ?, ?, ?)";
    String sqlStock = "INSERT INTO insumos_stock ( stock, caducidad, precio_compra, codigo_insumo) VALUES ( ?, ?, ?, ?)";

    try (
        PreparedStatement stmtInsumo = conexion.prepareStatement(sqlInsumo, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement stmtStock = conexion.prepareStatement(sqlStock)
    ) {
        
        conexion.setAutoCommit(false);

        // Insertar en insumos
        stmtInsumo.setString(1, insumo.getNombre());
        stmtInsumo.setString(2, insumo.getUnidad());
        stmtInsumo.setDouble(3, insumo.getMinStock()); 
        stmtInsumo.setString(4, insumo.getEstado());
        stmtInsumo.setInt(5, insumo.getIdNegocio());
        stmtInsumo.executeUpdate();

        try (ResultSet generatedKeys = stmtInsumo.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                int idInsumo = generatedKeys.getInt(1);
                insumo.setId(idInsumo);

                stmtStock.setInt(1, insumo.getStock());
                if (insumo.getCaducidad() != null && !insumo.getCaducidad().isEmpty()) {
                    stmtStock.setDate(2, Date.valueOf(insumo.getCaducidad()));
                } else {
                    stmtStock.setNull(2, Types.DATE);
                }
                stmtStock.setDouble(3, insumo.getPrecio());
                stmtStock.setInt(4, idInsumo);
                stmtStock.executeUpdate();
            } else {
                conexion.rollback();
                throw new SQLException("No se pudo obtener el ID del insumo insertado.");
            }
        }

        conexion.commit();

    } catch (SQLException e) {

        conexion.rollback();
        throw e;
    } finally {

        conexion.setAutoCommit(true);
    }
}

    public void eliminarInsumo(int id) throws SQLException {
        String sql = "DELETE FROM insumos WHERE id_insumos = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public double obtenerStock(int idInsumo) throws SQLException {
        String sql = "SELECT SUM(stock) FROM insumos_stock WHERE codigo_insumo = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idInsumo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public void descontarStock(int idInsumo, double cantidad) throws SQLException {
        String sql = """
            INSERT INTO insumos_stock (stock, caducidad, precio_compra, codigo_insumo)
            VALUES (?, NULL, 0, ?)
        """;
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setDouble(1, -cantidad);
            stmt.setInt(2, idInsumo);
            stmt.executeUpdate();
        }
    }

    public void actualizarEstado(int idInsumo, String estado) throws SQLException {
        String sql = "UPDATE insumos SET estado = ? WHERE id_insumos = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setInt(2, idInsumo);
            stmt.executeUpdate();
        }
    }

    private Insumo mapearInsumo(ResultSet rs) throws SQLException {
        return new Insumo(
            rs.getInt("id_insumos"),
            rs.getString("nom_producto"),
            rs.getString("unidad_medida"),
            rs.getInt("min_stock"),
            rs.getString("estado"),
            rs.getInt("codigo_negocio")
        );
    }


    public List<Insumo> listarPorNegocio(int idNegocio) throws SQLException {
    List<Insumo> lista = new ArrayList<>();
    String sql = "SELECT * FROM insumos WHERE codigo_negocio = ?";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idNegocio);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            lista.add(mapearInsumo(rs));
        }
    }
    return lista;
    }


    public void editar(Insumo insumo) throws SQLException {
    String sqlInsumo = "UPDATE insumos SET nom_producto = ?, unidad_medida = ?, min_stock = ?, estado = ? WHERE id_insumos = ?";
    String sqlStock = "UPDATE insumos_stock SET stock = ?, caducidad = ?, precio_compra = ? WHERE codigo_insumo = ?";

    try (
        PreparedStatement stmtInsumo = conexion.prepareStatement(sqlInsumo);
        PreparedStatement stmtStock = conexion.prepareStatement(sqlStock)
    ) {
      
        conexion.setAutoCommit(false);

       
        stmtInsumo.setString(1, insumo.getNombre());
        stmtInsumo.setString(2, insumo.getUnidad());
        stmtInsumo.setDouble(3, insumo.getMinStock());
        stmtInsumo.setString(4, insumo.getEstado());
        stmtInsumo.setInt(5, insumo.getId());
        stmtInsumo.executeUpdate();

       
        stmtStock.setInt(1, insumo.getStock());
        stmtStock.setString(2, insumo.getCaducidad());
        stmtStock.setDouble(3, insumo.getPrecio());
        stmtStock.setInt(4, insumo.getId());
        stmtStock.executeUpdate();

       
        conexion.commit();

    } catch (SQLException e) {
       
        conexion.rollback();
        throw e;
    } finally {
        
        conexion.setAutoCommit(true);
    }
}


public Map<String, Object> obtenerInfoStock(int idInsumo) throws SQLException {
    String sql = "SELECT precio_compra, caducidad FROM insumos_stock WHERE codigo_insumo = ? LIMIT 1";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idInsumo);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Map<String, Object> info = new HashMap<>();
            info.put("precio_compra", rs.getDouble("precio_compra"));
            info.put("caducidad", rs.getString("caducidad"));
            return info;
        }
    }
    return null;
}


public List<Map<String, Object>> obtenerHistorialInsumo(int idInsumo, int idNegocio) throws SQLException {
    List<Map<String, Object>> historial = new ArrayList<>();
    
    String sql = "SELECT s.fech_entrada as fecha, s.stock as cantidad, " +
                 "s.precio_compra as precio, s.caducidad " +
                 "FROM insumos_stock as s " +
                 "JOIN insumos as i ON s.codigo_insumo = i.id_insumos " +
                 "WHERE s.codigo_insumo = ? AND i.codigo_negocio = ? " +
                 "ORDER BY s.fech_entrada DESC";
    
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idInsumo);
        stmt.setInt(2, idNegocio);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Map<String, Object> registro = new HashMap<>();
            registro.put("fecha", rs.getTimestamp("fecha"));
            registro.put("cantidad", rs.getFloat("cantidad"));
            registro.put("precio", rs.getFloat("precio"));
            registro.put("caducidad", rs.getDate("caducidad"));
            historial.add(registro);
        }
    }
    
    return historial;
}

public void registrarMovimientoStock(int idInsumo, float stock, float precio, String caducidad) 
    throws SQLException {
    
    String sql = "INSERT INTO insumos_stock (codigo_insumo, stock, precio_compra, caducidad) " +
                 "VALUES (?, ?, ?, ?)";
    
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idInsumo);
        stmt.setFloat(2, stock);
        stmt.setFloat(3, precio);
        
        if (caducidad != null && !caducidad.isEmpty()) {
            stmt.setDate(4, Date.valueOf(caducidad));
        } else {
            stmt.setNull(4, Types.DATE);
        }
        
        stmt.executeUpdate();
    }
}

public void actualizarStockTotal(int idInsumo) throws SQLException {
    
    String sqlSuma = "SELECT SUM(stock) as total FROM insumos_stock WHERE codigo_insumo = ?";
    float stockTotal = 0;
    
    try (PreparedStatement stmt = conexion.prepareStatement(sqlSuma)) {
        stmt.setInt(1, idInsumo);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            stockTotal = rs.getFloat("total");
        }
    }
    
}


}

