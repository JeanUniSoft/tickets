package pe.edu.utp.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.edu.utp.tickets.model.RoleName;

public record UserRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 100) String email,
        @NotNull RoleName role) {
}