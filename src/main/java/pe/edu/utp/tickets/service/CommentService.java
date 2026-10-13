package pe.edu.utp.tickets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.tickets.dto.CommentRequest;
import pe.edu.utp.tickets.dto.CommentResponse;
import pe.edu.utp.tickets.exception.ResourceNotFoundException;
import pe.edu.utp.tickets.model.Comment;
import pe.edu.utp.tickets.model.Ticket;
import pe.edu.utp.tickets.model.User;
import pe.edu.utp.tickets.repository.CommentRepository;
import pe.edu.utp.tickets.repository.TicketRepository;
import pe.edu.utp.tickets.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public List<CommentResponse> findByTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("No existe el ticket " + ticketId);
        }
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(Long ticketId, CommentRequest request, String username) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el ticket " + ticketId));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + username));

        Comment comment = commentRepository.save(new Comment(request.content(), ticket, author));
        return CommentResponse.from(comment);
    }
}