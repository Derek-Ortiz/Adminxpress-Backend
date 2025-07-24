package gradlep.modelo;

public class Insumo {
    private int id;
    private String nombre;
    private int stock;
    private String unidad;
    private String caducidad;
    private double precio;
    private double minStock;
    private String estado;
    private int idNegocio;

    public Insumo() {
    }

    public Insumo(int id, String nombre,String unidad){
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
    }

    public Insumo(int id, String nombre,String unidad, double minStock, String estado, int idNegocio) {
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
        this.minStock = minStock;
        this.estado = estado;
        this.idNegocio = idNegocio;
    }

    public Insumo(int id, String nombre, String unidad, double precio,int stock, double minStock, String estado, int idNegocio) {
        this.id = id;
        this.nombre = nombre;
        this.unidad = unidad;
        this.precio = precio;
        this.stock = stock;
        this.minStock = minStock;
        this.estado = estado;
        this.idNegocio = idNegocio;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }
    public String getUnidad() { return unidad; }
    public String getCaducidad() { return caducidad; }
    public double getPrecio() { return precio; }
    public double getMinStock() { return minStock; }
    public String getEstado() { return estado; }
    public int getIdNegocio() { return idNegocio; }
    
    public void setId(int id) {this.id = id;}
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setStock(int stock) { this.stock = stock; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setCaducidad(String caducidad) { this.caducidad = caducidad; }
    public void setMinStock(double minStock) { this.minStock = minStock; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setIdNegocio(int idNegocio) { this.idNegocio = idNegocio; }

}
