package pe.edu.utp.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import pe.edu.utp.tickets.model.Comment;

class CommentControllerTest extends BaseIntegrationTest {

    // Entrada:         token de admin + el id de un ticket que tiene 1 comentario de ana
    // Salida esperada: 200 y la lista de comentarios
    // Verificación:    hay 1 comentario con sus 5 atributos (id, content, author, ticketId, createdAt)
    @Test
    @DisplayName("CP-17 GET /tickets/{ticketId}/comments devuelve 200 y los comentarios del ticket")
    void findByTicket_whenTicketExists_returnsComments() throws Exception {
        Comment comment = commentRepository.save(new Comment("Ya probé con otro cable", ticket, ana));

        mockMvc.perform(get(API + "/tickets/{ticketId}/comments", ticket.getId())
                        .header("Authorization", bearer("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(asInt(comment.getId())))
                .andExpect(jsonPath("$[0].content").value("Ya probé con otro cable"))
                .andExpect(jsonPath("$[0].author").value("ana"))
                .andExpect(jsonPath("$[0].ticketId").value(asInt(ticket.getId())))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty());
    }

    // Entrada:         token de admin + el id de un ticket que no existe
    // Salida esperada: 404
    // Verificación:    el código es 404
    @Test
    @DisplayName("CP-18 GET /tickets/{ticketId}/comments de un ticket que no existe devuelve 404")
    void findByTicket_whenTicketDoesNotExist_returns404() throws Exception {
        mockMvc.perform(get(API + "/tickets/{ticketId}/comments", NON_EXISTENT_ID)
                        .header("Authorization", bearer("admin")))
                .andExpect(status().isNotFound());
    }

    // Entrada:         token de admin + un comentario con texto
    // Salida esperada: 201 y el comentario creado
    // Verificación:    el autor es el dueño del token y en la base hay 1 comentario
    @Test
    @DisplayName("CP-19 POST /tickets/{ticketId}/comments agrega el comentario y devuelve 201")
    void create_withValidContent_returns201() throws Exception {
        mockMvc.perform(post(API + "/tickets/{ticketId}/comments", ticket.getId())
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Revisaremos la fuente de poder"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Revisaremos la fuente de poder"))
                .andExpect(jsonPath("$.author").value("admin"))
                .andExpect(jsonPath("$.ticketId").value(asInt(ticket.getId())));

        assertThat(commentRepository.count()).isEqualTo(1);
    }

    // Entrada:         token de admin + un comentario con el texto vacío
    // Salida esperada: 400, la validación rechaza los datos
    // Verificación:    el código es 400 y no hay comentarios en la base
    @Test
    @DisplayName("CP-20 POST /tickets/{ticketId}/comments con texto vacío devuelve 400")
    void create_withBlankContent_returns400() throws Exception {
        mockMvc.perform(post(API + "/tickets/{ticketId}/comments", ticket.getId())
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": ""}
                                """))
                .andExpect(status().isBadRequest());

        assertThat(commentRepository.count()).isZero();
    }
}
