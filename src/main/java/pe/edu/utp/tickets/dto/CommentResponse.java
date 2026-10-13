package pe.edu.utp.tickets.dto;

import java.time.Instant;

import pe.edu.utp.tickets.model.Comment;

public record CommentResponse(
        Long id,
        String content,
        String author,
        Long ticketId,
        Instant createdAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getTicket().getId(),
                comment.getCreatedAt());
    }
}