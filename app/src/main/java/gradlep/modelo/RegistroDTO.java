package gradlep.modelo;

public class RegistroDTO {
    private String company;
    private String username;
    private String password;

    // Constructor vacío obligatorio para Javalin
    public RegistroDTO() {}

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
