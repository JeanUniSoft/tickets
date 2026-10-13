package pe.edu.utp.tickets.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.utp.tickets.config.ApiPaths;
import pe.edu.utp.tickets.dto.ChangeStatusRequest;
import pe.edu.utp.tickets.dto.PageResponse;
import pe.edu.utp.tickets.dto.TicketRequest;
import pe.edu.utp.tickets.dto.TicketResponse;
import pe.edu.utp.tickets.model.TicketStatus;
import pe.edu.utp.tickets.service.TicketService;

@RestController
@RequestMapping(ApiPaths.V1 + "/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // GET /tickets                                -> primera página, 20 por página, los más nuevos primero
    // GET /tickets?status=OPEN&page=1&size=10     -> segunda página de los abiertos
    // GET /tickets?sort=title,asc                 -> ordenados por título
    @GetMapping
    public PageResponse<TicketResponse> findAll(
            @RequestParam(required = false) TicketStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ticketService.findAll(status, pageable);
    }

    @GetMapping("/{id}")
    public TicketResponse findById(@PathVariable Long id) {
        return ticketService.findById(id);
    }

    // El creador sale del token (lo dejó el filtro en el contexto), nunca del cuerpo
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody TicketRequest request, Authentication authentication) {
        return ticketService.create(request, authentication.getName());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public TicketResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return ticketService.changeStatus(id, request);
    }
}