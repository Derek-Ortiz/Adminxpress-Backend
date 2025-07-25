package gradlep.modelo;

import java.util.Objects;

public class Negocio {
    private int id;
    private String nombre;

    public Negocio() {
        
    }

    public Negocio(String nombre) {
        this.nombre = nombre;
    }

  
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

  
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

   
    @Override
    public String toString() {
        return "Negocio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Negocio)) return false;
        Negocio negocio = (Negocio) o;
        return id == negocio.id && Objects.equals(nombre, negocio.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}




