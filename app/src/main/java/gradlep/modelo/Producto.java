package gradlep.modelo;

public class Producto {
    private int id;
    private String nombre;
    private String descripcion;
    private double costoProduccion;
    private double precioActual;
    private String tipo;
    private int codigoNegocio;
    private String imagen;
    private int totalVendido;

    public Producto(int id, String nombre, String descripcion, double costoProduccion, double precioActual, String tipo,int codigoNegocio, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoProduccion = costoProduccion;
        this.precioActual = precioActual;
        this.tipo = tipo;
        this.codigoNegocio = codigoNegocio;
        this.imagen = imagen;
    }

    
    public Producto(int id, String nombre, String descripcion,  double precioActual, String tipo,int codigoNegocio, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioActual = precioActual;
        this.tipo = tipo;
        this.codigoNegocio = codigoNegocio;
        this.imagen = imagen;
        
    }

    public Producto(int id, String nombre, String descripcion, double costoProduccion, double precioActual, String tipo, int codigoNegocio) {
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.costoProduccion = costoProduccion;
    this.precioActual = precioActual;
    this.tipo = tipo;
    this.codigoNegocio = codigoNegocio;
}

    public Producto(int id, String nombre, String descripcion, double costoProduccion, double precioActual, String tipo, int codigoNegocio,String imagen, int totalVendido) {
    this.id = id;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.costoProduccion = costoProduccion;
    this.precioActual = precioActual;
    this.tipo = tipo;
    this.codigoNegocio = codigoNegocio;
    this.imagen = imagen;
     this.totalVendido = totalVendido;
}

public Producto() {
    }


    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getCostoProduccion() { return costoProduccion; }
    public double getPrecioActual() { return precioActual; }
    public String getTipo() { return tipo; }
    public int getCodigoNegocio() { return codigoNegocio; }
    public String getImagen() { return imagen; }
    public int getTotalVendido() { return totalVendido; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCostoProduccion(double costoProduccion) { this.costoProduccion = costoProduccion; }
    public void setPrecioActual(double precioActual) { this.precioActual = precioActual; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setCodigoNegocio(int codigoNegocio) { this.codigoNegocio = codigoNegocio; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setTotalVendido(int totalVendido) { this.totalVendido = totalVendido; }
}