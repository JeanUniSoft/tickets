package pe.edu.utp.tickets.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.tickets.model.Ticket;
import pe.edu.utp.tickets.model.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "creator"})
    Optional<Ticket> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"category", "creator"})
    Page<Ticket> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "creator"})
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
}