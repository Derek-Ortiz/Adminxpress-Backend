/*package gradlep.servicios;

import gradlep.modelo.Insumo;
import gradlep.modelo.InsumoProducto;
import gradlep.modelo.Negocio;
import gradlep.repositorios.ProductoDAO;
import gradlep.repositorios.InsumoDAO;
import gradlep.repositorios.InsumoProductoDAO;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ServicioStockInsumos {
    private final ProductoDAO productoDAO;
    private final InsumoDAO insumoDAO;
    private final InsumoProductoDAO insumoProductoDAO;
    private int idNegocio;



    public ServicioStockInsumos(Connection conexion) {
        this.productoDAO = new ProductoDAO(conexion);
        this.insumoDAO = new InsumoDAO(conexion);
        this.insumoProductoDAO = new InsumoProductoDAO(conexion);
    }


    public void asociarInsumosAProducto(int idProducto, Map<Integer, Double> insumosYCantidades) throws SQLException {
        insumoProductoDAO.eliminarInsumoDeProducto(idProducto);
        

        for (Map.Entry<Integer, Double> entry : insumosYCantidades.entrySet()) {
            InsumoProducto relacion = new InsumoProducto(idProducto, entry.getKey(), entry.getValue());
            
            insumoProductoDAO.agregarInsumoAProducto(relacion, idNegocio);
        }
    }

    public boolean verificarStockParaProducto(int idProducto, int cantidad) throws SQLException {
        Map<Insumo, Double> receta = insumoProductoDAO.obtenerRecetaProducto(idProducto, idNegocio);
        
        for (Map.Entry<Insumo, Double> entry : receta.entrySet()) {
            double cantidadNecesaria = entry.getValue() * cantidad;
            double stockActual = insumoDAO.obtenerStock(entry.getKey().getId());
            
            if (stockActual < cantidadNecesaria) {
                return false;
            }
        }
        return true;
    }


    public void descontarInsumosPorVenta(int idProducto, int cantidad) throws SQLException {
        if (!verificarStockParaProducto(idProducto, cantidad)) {
            throw new SQLException("Stock insuficiente para el producto ID: " + idProducto);
        }

        Map<Insumo, Double> receta = insumoProductoDAO.obtenerRecetaProducto(idProducto,idNegocio);
        
        for (Map.Entry<Insumo, Double> entry : receta.entrySet()) {
            int idInsumo = entry.getKey().getId();
            double cantidadADescontar = entry.getValue() * cantidad;
            
            insumoDAO.descontarStock(idInsumo, cantidadADescontar);
        }
        

        actualizarEstadosStock();
    }


    public void actualizarEstadosStock() throws SQLException {
        List<Insumo> insumos = insumoDAO.listarInsumos();
        
        for (Insumo insumo : insumos) {
            double stock = insumoDAO.obtenerStock(insumo.getId());
            String nuevoEstado;
            
            if (stock <= 0) {
                nuevoEstado = "Sin stock";
            } else if (stock < insumo.getMinStock()) {
                nuevoEstado = "Bajo Stock";
            } else {
                nuevoEstado = "OK";
            }
            
            insumoDAO.actualizarEstado(insumo.getId(), nuevoEstado);
        }
    }
}
    */