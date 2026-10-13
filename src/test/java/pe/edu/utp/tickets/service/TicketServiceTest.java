package pe.edu.utp.tickets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import pe.edu.utp.tickets.dto.ChangeStatusRequest;
import pe.edu.utp.tickets.dto.TicketRequest;
import pe.edu.utp.tickets.dto.TicketResponse;
import pe.edu.utp.tickets.exception.ConflictException;
import pe.edu.utp.tickets.exception.InvalidRequestException;
import pe.edu.utp.tickets.model.Category;
import pe.edu.utp.tickets.model.Priority;
import pe.edu.utp.tickets.model.Ticket;
import pe.edu.utp.tickets.model.TicketStatus;
import pe.edu.utp.tickets.model.User;
import pe.edu.utp.tickets.repository.CategoryRepository;
import pe.edu.utp.tickets.repository.TicketRepository;
import pe.edu.utp.tickets.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    private static final TicketRequest REQUEST =
            new TicketRequest("Sin internet", "Ninguna PC tiene red", Priority.MEDIUM, 1L);

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private final Category hardware = new Category("Hardware", "Equipos y periféricos");
    private final User ana = new User("ana", "HASH", "Ana Torres", "ana@tickets.pe");

    @Test
    @DisplayName("UT-05 create deja el ticket OPEN y a nombre del usuario recibido")
    void create_whenDataIsValid_createsOpenTicketForCreator() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(hardware));
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(ana));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.create(REQUEST, "ana");

        assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(response.creator()).isEqualTo("ana");
        assertThat(response.category()).isEqualTo("Hardware");
    }

    @Test
    @DisplayName("UT-06 create lanza InvalidRequestException cuando la categoría no existe")
    void create_whenCategoryDoesNotExist_throwsInvalidRequest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.create(REQUEST, "ana"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("No existe la categoría 1");

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("UT-07 changeStatus cambia el estado cuando la versión coincide")
    void changeStatus_whenVersionMatches_updatesStatus() {
        Ticket ticket = ticketWithVersion(3L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);

        TicketResponse response = ticketService.changeStatus(10L,
                new ChangeStatusRequest(TicketStatus.IN_PROGRESS, 3L));

        assertThat(response.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(ticketRepository).saveAndFlush(ticket);
    }

    @Test
    @DisplayName("UT-08 changeStatus lanza ConflictException cuando la versión es antigua")
    void changeStatus_whenVersionIsStale_throwsConflict() {
        Ticket ticket = ticketWithVersion(3L);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.changeStatus(10L,
                new ChangeStatusRequest(TicketStatus.CLOSED, 2L)))
                .isInstanceOf(ConflictException.class);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        verify(ticketRepository, never()).saveAndFlush(any(Ticket.class));
    }

    @Test
    @DisplayName("UT-09 findAll rechaza ordenar por un atributo fuera de la lista permitida")
    void findAll_whenSortFieldIsNotAllowed_throwsInvalidRequest() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("passwordHash"));

        assertThatThrownBy(() -> ticketService.findAll(null, pageable))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("No se puede ordenar por passwordHash");

        verifyNoInteractions(ticketRepository);
    }

    // La versión la pone Hibernate al guardar; aquí no hay base, así que se fija a mano
    private Ticket ticketWithVersion(Long version) {
        Ticket ticket = new Ticket("La PC no enciende", "No pasa nada", Priority.HIGH, hardware, ana);
        ReflectionTestUtils.setField(ticket, "version", version);
        return ticket;
    }
}