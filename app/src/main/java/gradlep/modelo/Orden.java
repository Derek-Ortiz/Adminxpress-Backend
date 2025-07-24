package gradlep.modelo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Orden {
    private int id; // Campo unificado para ID
    private int idUsuarioRealiza;
    private int idUsuarioCV; // También conocido como idCajero
    private boolean estado;
    private double total;
    private int idNegocio;
    private Timestamp fecha; // Para manejar fechas
    private List<DetalleOrden> detalles;
    private String nombreCajero;
     private Negocio negocio;

    public Orden() {
        this.detalles = new ArrayList<>();
        this.estado = true; // Por defecto, nueva orden activa
    }

    public void agregarDetalle(DetalleOrden detalle) {
        detalles.add(detalle);
    }

    public List<DetalleOrden> getDetalles() { 
        return detalles; 
    }

    public void setDetalles(List<DetalleOrden> detalles) {
        this.detalles = detalles;
    }

    public double calcularTotal() {
        return detalles.stream().mapToDouble(DetalleOrden::getSubtotal).sum();
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuarioRealiza() { return idUsuarioRealiza; }
    public void setIdUsuarioRealiza(int idUsuarioRealiza) { this.idUsuarioRealiza = idUsuarioRealiza; }

    public int getIdUsuarioCV() { return idUsuarioCV; }
    public void setIdUsuarioCV(int idUsuarioCV) { this.idUsuarioCV = idUsuarioCV; }
    
    // Alias para idUsuarioCV (para compatibilidad)
    public int getIdCajero() { return idUsuarioCV; }
    public void setIdCajero(int idCajero) { this.idUsuarioCV = idCajero; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public int getIdNegocio() { return idNegocio; }
    public void setIdNegocio(int idNegocio) { this.idNegocio = idNegocio; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getNombreCajero() { return nombreCajero; }
    public void setNombreCajero(String nombreCajero) { this.nombreCajero = nombreCajero; }

    public Negocio getNegocio() { return negocio;}
    public void setNegocio(Negocio negocio) { this.negocio = negocio; }

    @Override
    public String toString() {
        return "Orden{" +
                "id=" + id +
                ", idUsuarioRealiza=" + idUsuarioRealiza +
                ", estado=" + estado +
                ", total=" + total +
                ", idNegocio=" + idNegocio +
                ", fecha=" + fecha +
                ", cantidadDetalles=" + (detalles != null ? detalles.size() : 0) +
                '}';
    }

}