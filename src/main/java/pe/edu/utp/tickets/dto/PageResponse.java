package pe.edu.utp.tickets.dto;

import java.util.List;

import org.springframework.data.domain.Page;

// Forma estable de una página en el JSON, independiente de las clases internas de Spring
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
