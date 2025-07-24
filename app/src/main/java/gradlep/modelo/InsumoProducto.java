package gradlep.modelo;

public class InsumoProducto {
    private int codigoProducto;
    private int codigoInsumo;
    private double cantidadUsar;

    public InsumoProducto(int codigoProducto, int codigoInsumo, double cantidadUsar) {
        this.codigoProducto = codigoProducto;
        this.codigoInsumo = codigoInsumo;
        this.cantidadUsar = cantidadUsar;
    }

    public InsumoProducto(){
        
    }

    public int getCodigoProducto() { return codigoProducto; }
    public int getCodigoInsumo() { return codigoInsumo; }
    public double getCantidadUsar() { return cantidadUsar; }

    public void setCodigoProducto(int codigoProducto) { this.codigoProducto = codigoProducto; }
    public void setCodigoInsumo(int codigoInsumo) { this.codigoInsumo = codigoInsumo; }
    public void setCantidadUsar(double cantidadUsar) { this.cantidadUsar = cantidadUsar; }
}
