package pe.edu.utp.tickets.dto;

import java.util.List;

import pe.edu.utp.tickets.model.Role;
import pe.edu.utp.tickets.model.User;

// No tiene campo password: así es imposible que el hash salga en una respuesta
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        List<String> roles) {

    public static UserResponse from(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), roles);
    }
}