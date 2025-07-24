package gradlep.di;

import gradlep.controlador.ControladorInsumo;
import gradlep.controlador.ControladorOrden;
import gradlep.controlador.ControladorProducto;
import gradlep.controlador.ControladorReporte;
import gradlep.controlador.ControladorReportes;
import gradlep.controlador.ControladorUsuario;
import gradlep.repositorios.InsumoDAO;
import gradlep.repositorios.InsumoProductoDAO;
import gradlep.repositorios.OrdenDAO;
import gradlep.repositorios.ProductoDAO;
import gradlep.repositorios.UsuarioDAO;
import gradlep.rutas.InsumoRutas;
import gradlep.rutas.OrdenRutas;
import gradlep.rutas.ProductoRutas;
import gradlep.rutas.ReportesRutas;
import gradlep.rutas.UsuarioRutas;
import io.javalin.Javalin;

import java.sql.Connection;

public class AppModule {

    public static UsuarioRutas initUsuario(Connection conexion) {
        UsuarioDAO dao = new UsuarioDAO(conexion);
        ControladorUsuario controller = new ControladorUsuario(dao);
        return new UsuarioRutas(controller);
    }

    public static InsumoRutas initInsumo(Connection conexion) {
        InsumoDAO dao = new InsumoDAO(conexion);
        ControladorInsumo controller = new ControladorInsumo(dao);
        return new InsumoRutas(controller);
    }

    public static ProductoRutas initProducto(Connection conexion) {
        ProductoDAO dao = new ProductoDAO(conexion);
        InsumoProductoDAO dao2 = new InsumoProductoDAO(conexion);
        ControladorProducto controller = new ControladorProducto(dao,dao2);
        return new ProductoRutas(controller);
    }

      public static OrdenRutas initOrden(Connection conexion) {
        OrdenDAO dao = new OrdenDAO(conexion);
        ControladorOrden controller = new ControladorOrden(dao);
        return new OrdenRutas(controller);
    } 

     public static ReportesRutas initReporte(Connection conexion) {
        OrdenDAO dao = new OrdenDAO(conexion);
        ProductoDAO dao2 = new ProductoDAO(conexion);
        ControladorReportes controller = new ControladorReportes(dao, dao2);
        ControladorReporte controller2 = new ControladorReporte(dao, dao2);
     return new ReportesRutas(controller, controller2);
    } 

    public static void registrarRutas(Javalin app, Connection conexion) {
        UsuarioRutas usuarioRutas = initUsuario(conexion);
        InsumoRutas insumoRutas = initInsumo(conexion);
        ProductoRutas productoRutas = initProducto(conexion);
        OrdenRutas ordenRutas = initOrden(conexion);
        ReportesRutas reportesRutas = initReporte(conexion);
        usuarioRutas.registro(app);
        insumoRutas.registro(app);
        productoRutas.registro(app);
        ordenRutas.registro(app);
        reportesRutas.registro(app);
    }
}

