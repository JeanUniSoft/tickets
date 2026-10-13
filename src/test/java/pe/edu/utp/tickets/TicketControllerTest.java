package pe.edu.utp.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import pe.edu.utp.tickets.model.TicketStatus;

class TicketControllerTest extends BaseIntegrationTest {

    // ---------- GET /tickets ----------

    // Entrada:         token de ana, sin filtros ni parámetros de página
    // Salida esperada: 200 y la primera página con los valores por defecto
    // Verificación:    1 elemento (el ticket de prueba), página 0, tamaño 20, total 1
    @Test
    @DisplayName("CP-09 GET /tickets devuelve 200 y la primera página")
    void findAll_withoutFilters_returnsFirstPage() throws Exception {
        mockMvc.perform(get(API + "/tickets").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(asInt(ticket.getId())))
                .andExpect(jsonPath("$.content[0].title").value("La PC no enciende"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // Entrada:         token de ana + filtro status=CLOSED (no hay ninguno cerrado)
    // Salida esperada: 200 y una página vacía
    // Verificación:    0 elementos y total 0
    @Test
    @DisplayName("CP-10 GET /tickets?status=CLOSED devuelve 200 y una página vacía")
    void findAll_filteringByStatus_returnsEmptyPage() throws Exception {
        mockMvc.perform(get(API + "/tickets").param("status", "CLOSED").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ---------- GET /tickets/{id} ----------

    // Entrada:         token de ana + el id de un ticket que existe
    // Salida esperada: 200 y el ticket completo
    // Verificación:    llegan sus 10 atributos con los valores guardados
    @Test
    @DisplayName("CP-11 GET /tickets/{id} de un ticket que existe devuelve 200 y sus datos")
    void findById_whenTicketExists_returnsTicket() throws Exception {
        mockMvc.perform(get(API + "/tickets/{id}", ticket.getId()).header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(asInt(ticket.getId())))
                .andExpect(jsonPath("$.title").value("La PC no enciende"))
                .andExpect(jsonPath("$.description").value("Presiono el botón y no pasa nada"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.category").value("Hardware"))
                .andExpect(jsonPath("$.creator").value("ana"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    // Entrada:         token de ana + un id que no existe
    // Salida esperada: 404 con el motivo en "detail"
    // Verificación:    el código es 404 y el mensaje nombra el id buscado
    @Test
    @DisplayName("CP-12 GET /tickets/{id} de un ticket que no existe devuelve 404")
    void findById_whenTicketDoesNotExist_returns404() throws Exception {
        mockMvc.perform(get(API + "/tickets/{id}", NON_EXISTENT_ID).header("Authorization", bearer("ana")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el ticket " + NON_EXISTENT_ID));
    }

    // ---------- POST /tickets ----------

    // Entrada:         token de ana + título, descripción, prioridad y categoría válidos
    // Salida esperada: 201 y el ticket creado
    // Verificación:    nace OPEN, con versión 0, su creador es el dueño del token y la base tiene 2 tickets
    @Test
    @DisplayName("CP-13 POST /tickets con datos válidos crea el ticket y devuelve 201")
    void create_withValidData_returns201() throws Exception {
        mockMvc.perform(post(API + "/tickets")
                        .header("Authorization", bearer("ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sin internet en el aula B204",
                                  "description": "Ninguna PC del aula tiene red",
                                  "priority": "MEDIUM",
                                  "categoryId": %d
                                }
                                """.formatted(hardware.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.category").value("Hardware"))
                .andExpect(jsonPath("$.creator").value("ana"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(ticketRepository.count()).isEqualTo(2);
    }

    // Entrada:         token de ana + un ticket sin título
    // Salida esperada: 400 y el campo que falló dentro de "errors"
    // Verificación:    "errors.title" existe y la base sigue con 1 ticket
    @Test
    @DisplayName("CP-14 POST /tickets sin título devuelve 400 y no guarda nada")
    void create_withBlankTitle_returns400() throws Exception {
        mockMvc.perform(post(API + "/tickets")
                        .header("Authorization", bearer("ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "Ninguna PC del aula tiene red",
                                  "priority": "MEDIUM",
                                  "categoryId": %d
                                }
                                """.formatted(hardware.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());

        assertThat(ticketRepository.count()).isEqualTo(1);
    }

    // ---------- PUT /tickets/{id}/status ----------

    // Entrada:         token de admin + nuevo estado IN_PROGRESS + la versión actual (0)
    // Salida esperada: 200 y el ticket con el estado nuevo y la versión 1
    // Verificación:    al volver a leer el ticket de la base, su estado es IN_PROGRESS
    @Test
    @DisplayName("CP-15 PUT /tickets/{id}/status como ADMIN cambia el estado y devuelve 200")
    void changeStatus_asAdmin_returns200() throws Exception {
        mockMvc.perform(put(API + "/tickets/{id}/status", ticket.getId())
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "IN_PROGRESS", "version": 0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(asInt(ticket.getId())))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.version").value(1));

        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.IN_PROGRESS);
    }

    // Entrada:         token de ana (ROLE_USER) + nuevo estado CLOSED
    // Salida esperada: 403, porque solo un ADMIN cambia estados
    // Verificación:    el código es 403 y en la base el ticket sigue OPEN
    @Test
    @DisplayName("CP-16 PUT /tickets/{id}/status como USER devuelve 403 y no cambia nada")
    void changeStatus_asRegularUser_returns403() throws Exception {
        mockMvc.perform(put(API + "/tickets/{id}/status", ticket.getId())
                        .header("Authorization", bearer("ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CLOSED", "version": 0}
                                """))
                .andExpect(status().isForbidden());

        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.OPEN);
    }

    // Entrada:         token de admin + nuevo estado CLOSED + una versión que no es la actual (99)
    // Salida esperada: 409, el ticket cambió desde que el cliente lo leyó
    // Verificación:    el código es 409 y en la base el ticket sigue OPEN
    @Test
    @DisplayName("CP-21 PUT /tickets/{id}/status con una versión antigua devuelve 409")
    void changeStatus_withStaleVersion_returns409() throws Exception {
        mockMvc.perform(put(API + "/tickets/{id}/status", ticket.getId())
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CLOSED", "version": 99}
                                """))
                .andExpect(status().isConflict());

        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.OPEN);
    }
}
