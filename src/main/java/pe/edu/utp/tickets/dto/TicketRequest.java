package pe.edu.utp.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.edu.utp.tickets.model.Priority;

public record TicketRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull Priority priority,
        @NotNull Long categoryId) {
}