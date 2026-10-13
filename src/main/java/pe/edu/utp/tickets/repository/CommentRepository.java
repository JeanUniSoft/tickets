package pe.edu.utp.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.tickets.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}