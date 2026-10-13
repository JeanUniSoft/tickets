package pe.edu.utp.tickets.dto;

import jakarta.validation.constraints.NotNull;

import pe.edu.utp.tickets.model.TicketStatus;

// version: la que el cliente recibió al leer el ticket (bloqueo optimista)
public record ChangeStatusRequest(
        @NotNull TicketStatus status,
        @NotNull Long version) {
}