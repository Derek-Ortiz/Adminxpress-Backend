package gradlep.rutas;

import gradlep.controlador.ControladorReporte;
import gradlep.controlador.ControladorReportes;

import io.javalin.Javalin;

public class ReportesRutas {
    
    private final ControladorReportes controladorReportes;
     private final ControladorReporte controladorReporte;
      

        public ReportesRutas(ControladorReportes controladorReportes, ControladorReporte controladorReporte){
            this.controladorReportes = controladorReportes;
            this.controladorReporte = controladorReporte;
        }

        public void registro(Javalin app) {
         
        app.get("/api/reportes/{idNegocio}/resumen/totalventas", controladorReportes::obtenerTotalVentas);
        app.get("/api/reportes/{idNegocio}/resumen/totalordenes", controladorReportes::obtenerTotalOrdenes);
        app.get("/api/reportes/{idNegocio}/resumen/gastos", controladorReportes::obtenerGastos);
        app.get("/api/reportes/{idNegocio}/ventas-gastos", controladorReportes::obtenerUtilidadNeta);
        app.get("/api/reportes/{idNegocio}/top-productos/masvendidos", controladorReportes::obtenerProductosMasVendidos);
        app.get("/api/reportes/{idNegocio}/top-productos/menosvendidos", controladorReportes::obtenerProductosMenosVendidos);
        app.get("/api/reportes/{idNegocio}/horapicoventas", controladorReportes::obtenerHoraPico);
        
        app.get("/api/reportes/{idNegocio}/exportar", controladorReporte::generarPDF);
        
        }
        

}
