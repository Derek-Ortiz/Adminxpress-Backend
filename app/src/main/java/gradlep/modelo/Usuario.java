package gradlep.modelo;

import java.util.Objects;

public class Usuario {
    private int id;
    private String usuario;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String curp;
    private String contrasena;
    private String cargo;
    private int codigoNegocio;

    public Usuario() {
    }


    public Usuario(int id, String usuario, String nombre, String apellidoPaterno, String apellidoMaterno, String curp, String contrasena, String cargo, int codigoNegocio) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.curp = curp;
        this.contrasena = contrasena;
        this.cargo = cargo;
        this.codigoNegocio = codigoNegocio;
    }

    // Getters
    public int getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getNombre() { return nombre; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public String getCurp() { return curp; }
    public String getContrasena() { return contrasena; }
    public String getCargo() { return cargo; }
    public int getCodigoNegocio() { return codigoNegocio; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
    public void setCurp(String curp) { this.curp = curp; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setCodigoNegocio(int codigoNegocio) { this.codigoNegocio = codigoNegocio; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellidoPaterno='" + apellidoPaterno + '\'' +
                ", apellidoMaterno='" + apellidoMaterno + '\'' +
                ", curp='" + curp + '\'' +
                ", cargo='" + cargo + '\'' +
                ", codigoNegocio=" + codigoNegocio +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id &&
                codigoNegocio == usuario.codigoNegocio &&
                Objects.equals(nombre, usuario.nombre) &&
                Objects.equals(apellidoPaterno, usuario.apellidoPaterno) &&
                Objects.equals(apellidoMaterno, usuario.apellidoMaterno) &&
                Objects.equals(curp, usuario.curp) &&
                Objects.equals(contrasena, usuario.contrasena) &&
                Objects.equals(cargo, usuario.cargo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, apellidoPaterno, apellidoMaterno, curp, contrasena, cargo, codigoNegocio);
    }
}
