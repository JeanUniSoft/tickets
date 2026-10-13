package pe.edu.utp.tickets.service;

import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.tickets.dto.ChangeStatusRequest;
import pe.edu.utp.tickets.dto.PageResponse;
import pe.edu.utp.tickets.dto.TicketRequest;
import pe.edu.utp.tickets.dto.TicketResponse;
import pe.edu.utp.tickets.exception.ConflictException;
import pe.edu.utp.tickets.exception.InvalidRequestException;
import pe.edu.utp.tickets.exception.ResourceNotFoundException;
import pe.edu.utp.tickets.model.Category;
import pe.edu.utp.tickets.model.Ticket;
import pe.edu.utp.tickets.model.TicketStatus;
import pe.edu.utp.tickets.model.User;
import pe.edu.utp.tickets.repository.CategoryRepository;
import pe.edu.utp.tickets.repository.TicketRepository;
import pe.edu.utp.tickets.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class TicketService {

    // Únicos atributos por los que el cliente puede ordenar
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "updatedAt", "title", "status");

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // status == null -> todos; si no, solo los de ese estado
    public PageResponse<TicketResponse> findAll(TicketStatus status, Pageable pageable) {
        validateSort(pageable);
        Page<Ticket> tickets = (status == null)
                ? ticketRepository.findAll(pageable)
                : ticketRepository.findByStatus(status, pageable);
        return PageResponse.from(tickets.map(TicketResponse::from));
    }

    public TicketResponse findById(Long id) {
        return TicketResponse.from(findTicket(id));
    }

    @Transactional
    public TicketResponse create(TicketRequest request, String username) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new InvalidRequestException("No existe la categoría " + request.categoryId()));
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario " + username));

        Ticket ticket = new Ticket(
                request.title(),
                request.description(),
                request.priority(),
                category,
                creator);

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changeStatus(Long id, ChangeStatusRequest request) {
        Ticket ticket = findTicket(id);
        if (!Objects.equals(ticket.getVersion(), request.version())) {
            throw new ConflictException("El ticket " + id
                    + " fue modificado por otra persona. Vuelve a cargarlo e inténtalo de nuevo");
        }
        ticket.changeStatus(request.status());
        // saveAndFlush envía el UPDATE ahora: así la respuesta ya trae la versión y la fecha nuevas
        return TicketResponse.from(ticketRepository.saveAndFlush(ticket));
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el ticket " + id));
    }

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException("No se puede ordenar por " + order.getProperty());
            }
        }
    }
}