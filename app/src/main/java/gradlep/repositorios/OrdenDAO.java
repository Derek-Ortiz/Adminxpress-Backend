package gradlep.repositorios;

import gradlep.modelo.Orden;
import gradlep.modelo.DetalleOrden;
import gradlep.modelo.Insumo;
import gradlep.modelo.Negocio;
import gradlep.modelo.Producto;
//import gradlep.servicios.ServicioStockInsumos;

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
        System.out.println("entro en guarda orden en bd");
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
        System.out.println("entro en actualizar precios");
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
        System.out.println("entro en guardar detalles orden");
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

    // Listar pedidos por negocio
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

    // Buscar orden por ID y negocio
    public Orden buscarOrdenPorIdYNegocio(int idOrden, int idNegocio) throws SQLException {
        String sql = """
        SELECT p.id_pedidos, p.total, p.fech_realizacion, 
           p.codigo_usuario_realizar, u.nombre AS nombre_cajero,
           p.estado, p.codigo_negocio, n.nombre AS nombre_negocio
        FROM pedidos p
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

                    // Cargar detalles
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

    // Actualizar orden
    public boolean actualizarOrden(Orden orden) throws SQLException {
        try {
            conexion.setAutoCommit(false);
            System.out.println("[debbug] entro a actualizar orden para bd ");
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

    // Cancelar orden (cambiar estado a false)
    public boolean cancelarOrden(int idOrden, int idNegocio) throws SQLException {
        String sql = "UPDATE pedidos SET estado = false WHERE id_pedidos = ? AND codigo_negocio = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idOrden);
            stmt.setInt(2, idNegocio);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Listar ventas (solo pedidos completados/estado true) por negocio
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
            SELECT HOUR(fech_realizacion) AS hora, COUNT(*) AS total
            FROM pedidos
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
    String sql = "SELECT SUM(ins.precio_compra * ins.stock)  \r\n" + //
                "    FROM insumos_stock AS ins,  \r\n" + //
                "    insumos AS i \r\n" + //
                "    WHERE ins.codigo_insumo = i.id_insumos \r\n" + //
                "    AND ins.fech_entrada \r\n" + //
                "    BETWEEN ? AND ? \r\n" + //
                "    AND i.codigo_negocio = ?";
    try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setTimestamp(1, Timestamp.valueOf(desde));
        stmt.setTimestamp(2, Timestamp.valueOf(hasta));
        stmt.setInt(3, idNegocio);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getDouble(1) : 0.0;
    }
}

// ✅ NUEVO MÉTODO - Agregar este método que falta
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
    System.out.println("[DEBUG] Iniciando reducción de insumos para orden ID: " + orden.getId());
    System.out.println("[DEBUG] Negocio ID: " + orden.getIdNegocio());
    
    try {
        for (DetalleOrden detalle : orden.getDetalles()) {
            int idProducto = detalle.getCodigoProducto();
            int cantidad = detalle.getCantidad();
            
            System.out.println("[DEBUG] Procesando producto ID: " + idProducto + ", Cantidad: " + cantidad);
            
            // Obtener receta del producto
            System.out.println("[DEBUG] Obteniendo receta para producto " + idProducto);
            Map<Insumo, Double> receta = insumoProductoDAO.obtenerRecetaProducto(
                idProducto, 
                orden.getIdNegocio()
            );
            
            System.out.println("[DEBUG] Receta obtenida: " + receta);
            
            if (receta == null || receta.isEmpty()) {
                System.err.println("[ERROR] No se encontró receta para el producto ID: " + idProducto);
                throw new SQLException("No se encontró receta para el producto ID: " + idProducto);
            }
            
            // Reducir cada insumo
            for (Map.Entry<Insumo, Double> entrada : receta.entrySet()) {
                Insumo insumo = entrada.getKey();
                double cantidadUsar = entrada.getValue();
                double cantidadReducir = cantidadUsar * cantidad;
                
                System.out.println("[DEBUG] Procesando insumo ID: " + insumo.getId() + 
                                 " | Nombre: " + insumo.getNombre() + 
                                 " | Cantidad a reducir: " + cantidadReducir);
                
                try {
                    actualizarStockInsumo(insumo.getId(), -cantidadReducir);
                    System.out.println("[DEBUG] Stock actualizado con éxito para insumo " + insumo.getId());
                } catch (SQLException e) {
                    System.err.println("[ERROR CRÍTICO] Fallo al actualizar insumo " + insumo.getId());
                    System.err.println("[ERROR DETALLE] " + e.getMessage());
                    throw e; // Relanzamos la excepción
                }
            }
        }
        System.out.println("[DEBUG] Reducción de insumos completada con éxito para orden " + orden.getId());
    } catch (Exception e) {
        System.err.println("[ERROR GLOBAL] Fallo en reducirInsumosPorOrden: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
}

private void actualizarStockInsumo(int idInsumo, double cantidad) throws SQLException {
    System.out.println("[DEBUG] Actualizando stock para insumo " + idInsumo + ", Cambio: " + cantidad);
    
    // Verificar stock suficiente si estamos reduciendo
    if (cantidad < 0) {
        String sqlSelect = "SELECT stock FROM insumos_stock WHERE codigo_insumo = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlSelect)) {
            stmt.setInt(1, idInsumo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double stockActual = rs.getDouble("stock");
                System.out.println("[DEBUG] Stock actual insumo " + idInsumo + ": " + stockActual);
                
                if (stockActual + cantidad < 0) {
                    String errorMsg = "Stock insuficiente para insumo " + idInsumo + 
                                     ". Stock actual: " + stockActual + 
                                     ", Intento de reducción: " + (-cantidad);
                    System.err.println("[ERROR] " + errorMsg);
                    throw new SQLException(errorMsg);
                }
            } else {
                String errorMsg = "No se encontró registro de stock para insumo: " + idInsumo;
                System.err.println("[ERROR] " + errorMsg);
                throw new SQLException(errorMsg);
            }
        }
    }

    // Actualizar el stock
    String sqlUpdate = "UPDATE insumos_stock SET stock = stock + ? WHERE codigo_insumo = ?";
    try (PreparedStatement stmt = conexion.prepareStatement(sqlUpdate)) {
        stmt.setDouble(1, cantidad);
        stmt.setInt(2, idInsumo);
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows == 0) {
            String errorMsg = "No se pudo actualizar stock para insumo: " + idInsumo + 
                            " (0 filas afectadas)";
            System.err.println("[ERROR] " + errorMsg);
            throw new SQLException(errorMsg);
        }
        
        System.out.println("[DEBUG] Stock actualizado exitosamente para insumo " + idInsumo);
    } catch (SQLException e) {
        System.err.println("[ERROR SQL] Error al ejecutar update: " + e.getMessage());
        System.err.println("[DEBUG] SQL: " + sqlUpdate);
        System.err.println("[DEBUG] Parámetros: " + cantidad + ", " + idInsumo);
        throw e;
    }
    
    // Mostrar nuevo stock
    try {
        String sqlCheck = "SELECT stock FROM insumos_stock WHERE codigo_insumo = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sqlCheck)) {
            stmt.setInt(1, idInsumo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("[DEBUG] Nuevo stock insumo " + idInsumo + ": " + rs.getDouble("stock"));
            }
        }
    } catch (SQLException e) {
        System.err.println("[DEBUG] No se pudo verificar el nuevo stock: " + e.getMessage());
    }
}

   public void aumentarInsumosPorOrden(Orden orden) throws SQLException {
    System.out.println("[DEBUG] Iniciando aumento de insumos para la orden ID: " + orden.getId());
    
    try {
        System.out.println("[DEBUG] Número de detalles en la orden: " + orden.getDetalles().size());
        int detalleCount = 1;
        
        for (DetalleOrden detalle : orden.getDetalles()) {
            System.out.println("[DEBUG] Procesando detalle #" + detalleCount++);
            
            int idProducto = detalle.getCodigoProducto();
            int cantidad = detalle.getCantidad();
            System.out.println("[DEBUG] Producto ID: " + idProducto + ", Cantidad: " + cantidad);
            
            System.out.println("[DEBUG] Obteniendo receta para producto " + idProducto + " en negocio " + orden.getIdNegocio());
            Map<Insumo, Double> receta = insumoProductoDAO.obtenerRecetaProducto(
                idProducto, 
                orden.getIdNegocio()
            );
            
            System.out.println("[DEBUG] Receta obtenida con " + receta.size() + " insumos");
            int insumoCount = 1;
            
            for (Map.Entry<Insumo, Double> entrada : receta.entrySet()) {
                System.out.println("[DEBUG] Procesando insumo #" + insumoCount++ + " del detalle");
                
                Insumo insumo = entrada.getKey();
                double cantidadUsar = entrada.getValue();
                double cantidadAumentar = cantidadUsar * cantidad;
                
                System.out.println(String.format(
                    "[DEBUG] Insumo ID: %d, Nombre: %s, Cantidad a usar por unidad: %.2f, Total a aumentar: %.2f",
                    insumo.getId(),
                    insumo.getNombre(),
                    cantidadUsar,
                    cantidadAumentar
                ));
                
                System.out.println("[DEBUG] Actualizando stock del insumo " + insumo.getId());
                actualizarStockInsumo(insumo.getId(), cantidadAumentar);
                System.out.println("[DEBUG] Stock actualizado correctamente");
            }
        }
        
        System.out.println("[DEBUG] Proceso de aumento de insumos completado exitosamente");
    } catch (SQLException e) {
        System.out.println("[ERROR] SQLException en aumentarInsumosPorOrden: " + e.getMessage());
        e.printStackTrace();
        throw e;
    } catch (Exception e) {
        System.out.println("[ERROR] Error inesperado en aumentarInsumosPorOrden: " + e.getMessage());
        e.printStackTrace();
        throw new SQLException("Error al aumentar insumos", e);
    }
}
}
  /*  private void actualizarStockInsumo(int idInsumo, double cantidad) throws SQLException {
        String sql = "UPDATE insumos_stock SET stock = stock + ? WHERE codigo_insumo = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setDouble(1, cantidad);
            stmt.setInt(2, idInsumo);
            stmt.executeUpdate();
        }
    }
}
*/


