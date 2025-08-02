package gradlep;

import gradlep.configuracion.conexion;
import gradlep.di.AppModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        Connection conn = conexion.conectar();
        if (conn == null) {
            System.err.println("No se pudo conectar a la base de datos.");
            return;
        }

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
            staticFiles.hostedPath = "/uploads";
            staticFiles.directory = "/home/ubuntu/integrador-back2/app/src/main/java/gradlep/uploads";
            staticFiles.location = Location.EXTERNAL;
    });
        }).start("0.0.0.0",7000);

        
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
            ctx.header("Access-Control-Max-Age", "86400"); 

            System.out.println("Petición recibida: " + ctx.method() + " " + ctx.path());
        });

        app.options("/*", ctx -> {
            System.out.println(" Petición OPTIONS recibida para: " + ctx.path());
            ctx.status(200);
        });

        app.get("/", ctx -> ctx.result("API funcionando"));

        // Endpoint de prueba
        app.get("/test", ctx -> {
            ctx.json("CORS funcionando correctamente");
        });

        AppModule.registrarRutas(app, conn);

        System.out.println("Servidor iniciado en http://localhost:7000");
        System.out.println("Prueba: http://localhost:7000/");
        System.out.println("Test CORS: http://localhost:7000/test");
        System.out.println("Archivos estáticos en: http://localhost:7000/uploads/");
    }
}