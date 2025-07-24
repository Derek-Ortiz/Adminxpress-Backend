package gradlep.modelo;

public class InsumoDTO {
    
    private int idInsumo;
    private String nombre;
    private String unidadMedida;
    private double cantidadUsar;

    public InsumoDTO(int idInsumo, String nombre, String unidadMedida, double cantidadUsar) {
        this.idInsumo = idInsumo;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.cantidadUsar = cantidadUsar;
    }

    // Getters (los setters solo si son necesarios)
    public int getIdInsumo() { return idInsumo; }
    public String getNombre() { return nombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getCantidadUsar() { return cantidadUsar; }
}
