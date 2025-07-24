package gradlep.modelo;

import java.util.Objects;

public class Negocio {
    private int id;
    private String nombre;

    public Negocio() {
        // Constructor vacío
    }

    public Negocio(String nombre) {
        this.nombre = nombre;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // toString
    @Override
    public String toString() {
        return "Negocio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Negocio)) return false;
        Negocio negocio = (Negocio) o;
        return id == negocio.id && Objects.equals(nombre, negocio.nombre);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }
}




