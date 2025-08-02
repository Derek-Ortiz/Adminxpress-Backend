package gradlep.configuracion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    
    private static final String URL = "jdbc:mariadb://13.223.132.126:3306/adminxpress";
    private static final String USER = "admindb2";
    private static final String PASSWORD = "D3r3k2oo6.@";
    /* 
    private static final String URL = "jdbc:mariadb://localhost:3306/Adminxpress";
    private static final String USER = "root";
    private static final String PASSWORD = null;
    */


    public static Connection conectar(){
        try{
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos.");
            return conn;
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }
}