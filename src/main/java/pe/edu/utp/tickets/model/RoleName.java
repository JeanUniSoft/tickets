package pe.edu.utp.tickets.model;

// Los roles que la API acepta al crear un usuario
public enum RoleName {
    ADMIN,
    USER;

    // Nombre con que el rol se guarda en la tabla roles y lo entiende Spring Security
    public String authority() {
        return "ROLE_" + name();
    }
}