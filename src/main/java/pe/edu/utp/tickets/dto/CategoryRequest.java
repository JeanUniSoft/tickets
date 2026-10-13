package pe.edu.utp.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 255) String description) {
}