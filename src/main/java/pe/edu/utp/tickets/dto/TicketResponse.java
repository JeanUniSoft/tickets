package pe.edu.utp.tickets.dto;

import java.time.Instant;

import pe.edu.utp.tickets.model.Priority;
import pe.edu.utp.tickets.model.Ticket;
import pe.edu.utp.tickets.model.TicketStatus;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        String category,
        String creator,
        Long version,
        Instant createdAt,
        Instant updatedAt) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory().getName(),
                ticket.getCreator().getUsername(),
                ticket.getVersion(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}