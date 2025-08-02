package gradlep.repositorios;

import gradlep.modelo.Orden;
import gradlep.modelo.DetalleOrden;
import gradlep.modelo.Insumo;
import gradlep.modelo.Negocio;
import gradlep.modelo.Producto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdenDAO {
    private final Connection conexion;
    private final InsumoProductoDAO insumoProductoDAO;

    public OrdenDAO(Connection conexion) {
        this.conexion = conexion;
        this.insumoProductoDAO = new InsumoProductoDAO(conexion);
    }

    public int guardarOrden(Orden orden) throws SQLException {
        String sqlPedido = "INSERT INTO pedidos (total, estado, codigo_usuario_realizar, codigo_negocio) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, orden.calcularTotal());
            stmt.setBoolean(2, orden.isEstado());
            stmt.setInt(3, orden.getIdUsuarioRealiza());
            stmt.setInt(4, orden.getIdNegocio());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idPedido = rs.getInt(1);
                        orden.setId(idPedido);
                        guardarDetallesOrden(idPedido, orden.getDetalles());
                        return idPedido;
                    }
                }
            }
        }
        return -1;
    }

    public boolean validarYActualizarPrecios(Orden orden) throws SQLException {
        String sql = "SELECT id_producto, precio_actual FROM productos WHERE id_producto = ? AND codigo_negocio = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            for (DetalleOrden detalle : orden.getDetalles()) {
                stmt.setInt(1, detalle.getCodigoProducto());
                stmt.setInt(2, orden.getIdNegocio());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        detalle.setPrecioVenta(rs.getDouble("precio_actual"));
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void guardarDetallesOrden(int idPedido, List<DetalleOrden> detalles) throws SQLException {
        String sqlDetalle = "INSERT INTO productos_pedidos (num_pedido, codigo_producto, precio_venta, cantidad) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sqlDetalle)) {
            for (DetalleOrden detalle : detalles) {
                
                stmt.setInt(1, idPedido);
                stmt.setInt(2, detalle.getCodigoProducto());
                stmt.setDouble(3, detalle.getPrecioVenta());
                stmt.setInt(4, detalle.getCantidad());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Orden> listarPedidosPorNegocio(int idNegocio) throws SQLException {
        String sql = """
            SELECT p.id_pedidos, p.total, p.fech_realizacion, p.codigo_usuario_realizar, p.estado,
                pr.id_producto, pr.nombre, pr.descripcion, pr.precio_actual, 
                pr.tipo, pp.cantidad, pp.precio_venta
            FROM pedidos p
            JOIN productos_pedidos pp ON p.id_pedidos = pp.num_pedido
            JOIN productos pr ON pr.id_producto = pp.codigo_producto
            WHERE p.codigo_negocio = ?
            ORDER BY p.fech_realizacion DESC
            """;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idNegocio);
            
            ResultSet rs = stmt.executeQuery();
            Map<Integer, Orden> ordenesMap = new HashMap<>();
            
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedidos");
                Orden orden = ordenesMap.get(idPedido);
                
                if (orden == null) {
                    orden = new Orden();
                    orden.setId(idPedido);
                    orden.setTotal(rs.getDouble("total"));
                    orden.setFecha(rs.getTimestamp("fech_realizacion"));
                    orden.setIdUsuarioRealiza(rs.getInt("codigo_usuario_realizar"));
                    orden.setEstado(rs.getBoolean("estado"));
                    orden.setIdNegocio(idNegocio);
                    ordenesMap.put(idPedido, orden);
                }
                
                Producto producto = new Producto();
                producto.setId(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecioActual(rs.getDouble("precio_actual"));
                producto.setTipo(rs.getString("tipo"));
                producto.setCodigoNegocio(idNegocio);
                
                DetalleOrden detalle = new DetalleOrden(
                    rs.getInt("id_producto"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("cantidad")
                );
                detalle.setProducto(producto);
                orden.agregarDetalle(detalle);
            }
            
            return new ArrayList<>(ordenesMap.values());
        }
    }

    public Orden buscarOrdenPorIdYNegocio(int idOrden, int idNegocio) throws SQLException {
        String sql = """
        SELECT p.id_pedidos, p.total, p.fech_realizacion, p.codigo_usuario_realizar, u.nombre AS nombre_cajero,
        p.estado, p.codigo_negocio, n.nombre AS nombre_negocio FROM pedidos p
        JOIN usuarios u ON p.codigo_usuario_realizar = u.id_usuario
        JOIN negocio n ON p.codigo_negocio = n.id_negocio
        WHERE p.id_pedidos = ? AND p.codigo_negocio = ?
        """;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idOrden);
            stmt.setInt(2, idNegocio);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Orden orden = new Orden();
                    orden.setId(rs.getInt("id_pedidos"));
                    orden.setTotal(rs.getDouble("total"));
                    orden.setFecha(rs.getTimestamp("fech_realizacion"));
                    orden.setIdUsuarioRealiza(rs.getInt("codigo_usuario_realizar"));
                    orden.setNombreCajero(rs.getString("nombre_cajero"));
                    orden.setEstado(rs.getBoolean("estado"));
                    orden.setIdNegocio(rs.getInt("codigo_negocio"));

                    Negocio negocio = new Negocio();
                    negocio.setId(rs.getInt("codigo_negocio"));
                    negocio.setNombre(rs.getString("nombre_negocio"));
                    orden.setNegocio(negocio);

                    List<DetalleOrden> detalles = obtenerDetallesOrden(idOrden);
                    for (DetalleOrden detalle : detalles) {
                        orden.agregarDetalle(detalle);
                    }

                    return orden;
                }
            }
        }
        return null;
    }

    public List<DetalleOrden> obtenerDetallesOrden(int idOrden) throws SQLException {
        List<DetalleOrden> detalles = new ArrayList<>();
        String sql = """
            SELECT p.id_producto, p.nombre, p.descripcion, p.precio_actual, p.tipo, p.codigo_negocio,
                   pp.cantidad, pp.precio_venta
            FROM productos p
            JOIN productos_pedidos pp ON p.id_producto = pp.codigo_producto
            WHERE pp.num_pedido = ?
        """;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idOrden);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto producto = new Producto();
                    producto.setId(rs.getInt("id_producto"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setDescripcion(rs.getString("descripcion"));
                    producto.setPrecioActual(rs.getDouble("precio_actual"));
                    producto.setTipo(rs.getString("tipo"));
                    producto.setCodigoNegocio(rs.getInt("codigo_negocio"));

                    DetalleOrden detalle = new DetalleOrden(
                        rs.getInt("id_producto"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("cantidad")
                    );
                    detalle.setProducto(producto);
                    detalles.add(detalle);
                }
            }
        }

        return detalles;
    }

    public boolean actualizarOrden(Orden orden) throws SQLException {
        try {
            conexion.setAutoCommit(false);
            String sqlPedido = "UPDATE pedidos SET total = ?, estado = ?, codigo_usuario_cancela_vende = ? WHERE id_pedidos = ? AND codigo_negocio = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sqlPedido)) {
                stmt.setDouble(1, orden.calcularTotal());
                stmt.setBoolean(2, orden.isEstado());
                stmt.setInt(3, orden.getIdUsuarioCV());
                stmt.setInt(4, orden.getId());
                stmt.setInt(5, orden.getIdNegocio());
                
                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas == 0) {
                    conexion.rollback();
                    return false;
                }
            }
            
            String sqlEliminar = "DELETE FROM productos_pedidos WHERE num_pedido = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sqlEliminar)) {
                stmt.setInt(1, orden.getId());
                stmt.executeUpdate();
            }
            
            guardarDetallesOrden(orden.getId(), orden.getDetalles());
            
            conexion.commit();
            return true;
        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(true);
        }
    }

    public boolean cancelarOrden(int idOrden, int idNegocio) throws SQLException {
        String sql = "UPDATE pedidos SET estado = false WHERE id_pedidos = ? AND codigo_negocio = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idOrden);
            stmt.setInt(2, idNegocio);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<Orden> listarVentasPorNegocio(int idNegocio) throws SQLException {
        String sql = """
            SELECT p.id_pedidos, p.total, p.fech_realizacion, p.codigo_usuario_realizar, p.estado,
                pr.id_producto, pr.nombre, pr.descripcion, pr.precio_actual, 
                pr.tipo, pp.cantidad, pp.precio_venta
            FROM pedidos p
            JOIN productos_pedidos pp ON p.id_pedidos = pp.num_pedido
            JOIN productos pr ON pr.id_producto = pp.codigo_producto
            WHERE p.codigo_negocio = ? AND p.estado = true
            ORDER BY p.fech_realizacion DESC
            """;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idNegocio);
            
            ResultSet rs = stmt.executeQuery();
            Map<Integer, Orden> ordenesMap = new HashMap<>();
            
            while (rs.next()) {
                int idPedido = rs.getInt("id_pedidos");
                Orden orden = ordenesMap.get(idPedido);
                
                if (orden == null) {
                    orden = new Orden();
                    orden.setId(idPedido);
                    orden.setTotal(rs.getDouble("total"));
                    orden.setFecha(rs.getTimestamp("fech_realizacion"));
                    orden.setIdUsuarioRealiza(rs.getInt("codigo_usuario_realizar"));
                    orden.setEstado(rs.getBoolean("estado"));
                    orden.setIdNegocio(idNegocio);
                    ordenesMap.put(idPedido, orden);
                }
                
                Producto producto = new Producto();
                producto.setId(rs.getInt("id_producto"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecioActual(rs.getDouble("precio_actual"));
                producto.setTipo(rs.getString("tipo"));
                producto.setCodigoNegocio(idNegocio);
                
                DetalleOrden detalle = new DetalleOrden(
                    rs.getInt("id_producto"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("cantidad")
                );
                detalle.setProducto(producto);
                orden.agregarDetalle(detalle);
            }
            
            return new ArrayList<>(ordenesMap.values());
        }
    }

    public String horaPicoVentas(LocalDateTime desde, LocalDateTime hasta, int idNegocio) throws SQLException {
        String sql = """
            SELECT HOUR(fech_realizacion) AS hora, COUNT(*) AS total FROM pedidos
            WHERE fech_realizacion BETWEEN ? AND ? AND codigo_negocio = ? AND estado = true
            GROUP BY hora ORDER BY total DESC LIMIT 1
        """;
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            stmt.setInt(3, idNegocio);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("hora") + ":00 hrs" : "Sin datos";
        }
    }

    public double sumarVentas(LocalDateTime desde, LocalDateTime hasta, int idNegocio) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE fech_realizacion BETWEEN ? AND ? AND codigo_negocio = ? AND estado = true";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            stmt.setInt(3, idNegocio);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int contarOrdenes(LocalDateTime desde, LocalDateTime hasta, int idNegocio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE fech_realizacion BETWEEN ? AND ? AND codigo_negocio = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            stmt.setInt(3, idNegocio);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double calcularGastos(LocalDateTime desde, LocalDateTime hasta, int idNegocio) throws SQLException {
        String sql = "SELECT SUM(ins.precio_compra * ins.stock) FROM insumos_stock AS ins,insumos AS i WHERE ins.codigo_insumo = i.id_insumos AND ins.fech_entrada BETWEEN ? AND ? AND i.codigo_negocio = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            stmt.setInt(3, idNegocio);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public Map<String, Double> calcularUtilidadNeta(LocalDateTime desde, LocalDateTime hasta, int idNegocio) throws SQLException {
        double ventas = sumarVentas(desde, hasta, idNegocio);
        double gastos = calcularGastos(desde, hasta, idNegocio);
        double utilidad = ventas - gastos;
        
        Map<String, Double> resultado = new HashMap<>();
        resultado.put("ventas_totales", ventas);
        resultado.put("gastos_totales", gastos);
        resultado.put("utilidad_neta", utilidad);
        
        return resultado;
    }

    public void reducirInsumosPorOrden(Orden orden) throws SQLException {
        try {
            for (DetalleOrden detalle : orden.getDetalles()) {
                int idProducto = detalle.getCodigoProducto();
                int cantidad = detalle.getCantidad();
                
                Map<Insumo, Double> receta = insumoProductoDAO.obtenerRecetaProducto(
                    idProducto, 
                    orden.getIdNegocio()
                );
                
                if (receta == null || receta.isEmpty()) {
                    throw new SQLException("No se encontró receta para el producto ID: " + idProducto);
                }
                
                for (Map.Entry<Insumo, Double> entrada : receta.entrySet()) {
                    Insumo insumo = entrada.getKey();
                    double cantidadUsar = entrada.getValue();
                    double cantidadReducir = cantidadUsar * cantidad;
                    
                    try {
                        actualizarStockInsumo(insumo.getId(), -cantidadReducir);
                    } catch (SQLException e) {
                        throw e; 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void actualizarStockInsumo(int idInsumo, double cantidad) throws SQLException {
        if (cantidad < 0) {
            String sqlSelect = "SELECT stock FROM insumos_stock WHERE codigo_insumo = ?";
            try (PreparedStatement stmt = conexion.prepareStatement(sqlSelect)) {
                stmt.setInt(1, idInsumo);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double stockActual = rs.getDouble("stock");
                    
                    if (stockActual + cantidad < 0) {
                        String errorMsg = "Stock insuficiente para insumo " + idInsumo + 
                                         ". Stock actual: " + stockActual + 
                                         ", Intento de reducción: " + (-cantidad);
                        throw new SQLException(errorMsg);
                    }
                } else {
                    String errorMsg = "No se encontró registro de stock para insumo: " + idInsumo;
                    throw new SQLException(errorMsg);
                }
            }
        }

        String sqlUpdate = "UPDATE insumos_stock SET stock = stock + ? WHERE codigo_insumo = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlUpdate)) {
            stmt.setDouble(1, cantidad);
            stmt.setInt(2, idInsumo);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                String errorMsg = "No se pudo actualizar stock para insumo: " + idInsumo + 
                                " (0 filas afectadas)";
                throw new SQLException(errorMsg);
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void aumentarInsumosPorOrden(Orden orden) throws SQLException {
        try {
            for (DetalleOrden detalle : orden.getDetalles()) {
                int idProducto = detalle.getCodigoProducto();
                int cantidad = detalle.getCantidad();
                
                Map<Insumo, Double> receta = insumoProductoDAO.obtenerRecetaProducto(
                    idProducto, 
                    orden.getIdNegocio()
                );
                
                for (Map.Entry<Insumo, Double> entrada : receta.entrySet()) {
                    Insumo insumo = entrada.getKey();
                    double cantidadUsar = entrada.getValue();
                    double cantidadAumentar = cantidadUsar * cantidad;
                    
                    actualizarStockInsumo(insumo.getId(), cantidadAumentar);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Error al aumentar insumos", e);
        }
    }
}