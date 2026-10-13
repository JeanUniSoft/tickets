package pe.edu.utp.tickets.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.utp.tickets.config.ApiPaths;
import pe.edu.utp.tickets.dto.CommentRequest;
import pe.edu.utp.tickets.dto.CommentResponse;
import pe.edu.utp.tickets.service.CommentService;

@RestController
@RequestMapping(ApiPaths.V1 + "/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponse> findByTicket(@PathVariable Long ticketId) {
        return commentService.findByTicket(ticketId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long ticketId, @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return commentService.create(ticketId, request, authentication.getName());
    }
}