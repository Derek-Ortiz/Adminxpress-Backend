package gradlep.repositorios;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import gradlep.modelo.Insumo;

import gradlep.modelo.Producto;

public class ProductoDAO {
    private Connection conexion;

     public ProductoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void agregarProducto(Producto producto) throws SQLException {
    try {
       
        String sql = "INSERT INTO productos(nombre, descripcion, costo_produccion, precio_actual, tipo, codigo_negocio, imagen) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, producto.getNombre());
            stmt.setString(2, producto.getDescripcion());
            stmt.setDouble(3, producto.getCostoProduccion());
            stmt.setDouble(4, producto.getPrecioActual());
            stmt.setString(5, producto.getTipo());
            stmt.setInt(6, producto.getCodigoNegocio());
            stmt.setString(7, producto.getImagen());

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    producto.setId(generatedKeys.getInt(1));
                }
            }
        }
    } catch (SQLException e) {
        conexion.rollback();
        throw e;
    } finally {
        conexion.setAutoCommit(true);
    }
}

    public void editarProducto(Producto producto) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, costo_produccion = ?, precio_actual = ?, tipo = ?, imagen = ? WHERE id_producto = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setString(2, producto.getDescripcion());
            stmt.setDouble(3, producto.getCostoProduccion());
            stmt.setDouble(4, producto.getPrecioActual());
            stmt.setString(5, producto.getTipo());
            stmt.setString(6, producto.getImagen());
            stmt.setInt(7, producto.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminarProducto(int idProducto) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            stmt.executeUpdate();
        }
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        String sql = "SELECT * FROM productos WHERE id_producto = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }

    public List<Producto> listarPorNegocio(int codigoNegocio) throws SQLException {
    String sql = "SELECT * FROM productos WHERE codigo_negocio = ?";
    List<Producto> productos = new ArrayList<>();
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, codigoNegocio);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            productos.add(mapearProducto(rs));
        }
    }
    return productos;
}

    public List<Producto> listarParaVentas(int codigoNegocio) throws SQLException {
    String sql = "SELECT id_producto, nombre, precio_actual, imagen, tipo, descripcion, codigo_negocio FROM productos WHERE codigo_negocio = ?";
    List<Producto> productos = new ArrayList<>();
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, codigoNegocio);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            productos.add(mapearProductoVenta(rs));
        }
    }
    return productos;
}

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("id_producto"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getDouble("costo_produccion"),
            rs.getDouble("precio_actual"),
            rs.getString("tipo"),
            rs.getInt("codigo_negocio"),
            rs.getString("imagen")
            
        );
    }

       private Producto mapearProductoVenta(ResultSet rs) throws SQLException {
        return new Producto(
            rs.getInt("id_producto"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getDouble("precio_actual"),
            rs.getString("tipo"),
            rs.getInt("codigo_negocio"),
            rs.getString("imagen")
            
        );
    }

    public Producto buscarPorIdYNegocio(int idProducto, int codigoNegocio) throws SQLException {
    String sql = "SELECT * FROM productos WHERE id_producto = ? AND codigo_negocio = ?";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idProducto);
        stmt.setInt(2, codigoNegocio);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? mapearProducto(rs) : null;
        }
    }
}

public List<Insumo> listarInsumosBasicos(int idNegocio) throws SQLException {
    List<Insumo> lista = new ArrayList<>();
    String sql = "SELECT id_insumos, nom_producto, unidad_medida FROM insumos WHERE codigo_negocio = ?";
    
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setInt(1, idNegocio);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Insumo insumo = new Insumo();
            insumo.setId(rs.getInt("id_insumos"));
            insumo.setNombre(rs.getString("nom_producto"));
            insumo.setUnidad(rs.getString("unidad_medida"));
            lista.add(insumo);
        }
    }
    return lista;
}

    public List<Producto> obtenerTopProductos(LocalDateTime desde, LocalDateTime hasta, boolean masVendidos, int idNegocio) throws SQLException {
    List<Producto> productos = new ArrayList<>();
    String orden = masVendidos ? "DESC" : "ASC";
    String sql = """
        SELECT p.id_producto, p.nombre, p.descripcion, p.costo_produccion, p.precio_actual, p.tipo, p.imagen,
            SUM(pp.cantidad) AS total
        FROM productos p
        JOIN productos_pedidos pp ON p.id_producto = pp.codigo_producto
        JOIN pedidos o ON o.id_pedidos = pp.num_pedido
        WHERE o.fech_realizacion BETWEEN ? AND ? AND p.codigo_negocio = ?
        GROUP BY p.id_producto
        ORDER BY total """ + " " + orden + " LIMIT 3";
    
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(desde));
        stmt.setTimestamp(2, Timestamp.valueOf(hasta));
        stmt.setInt(3, idNegocio);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Producto prod = new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("costo_produccion"),
                rs.getDouble("precio_actual"),
                rs.getString("tipo"),
                idNegocio, 
                rs.getString("imagen"),
                rs.getInt("total") 
            );
            productos.add(prod);
        }
    }
    return productos;
 }
}