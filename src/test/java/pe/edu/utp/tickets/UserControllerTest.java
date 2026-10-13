package pe.edu.utp.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import pe.edu.utp.tickets.model.User;

class UserControllerTest extends BaseIntegrationTest {

    private static final String LUIS = """
            {
              "username": "luis",
              "password": "luis123",
              "fullName": "Luis Ramos",
              "email": "luis@tickets.pe",
              "role": "USER"
            }
            """;

    // Entrada:         token de admin + los datos del usuario "luis"
    // Salida esperada: 201 y el usuario creado (id, username, fullName, email, roles), sin contraseña
    // Verificación:    la respuesta no trae contraseña y en la base quedó guardado el hash BCrypt
    @Test
    @DisplayName("CP-03 POST /users como ADMIN crea el usuario y devuelve 201")
    void create_asAdmin_returns201() throws Exception {
        mockMvc.perform(post(API + "/users")
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LUIS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("luis"))
                .andExpect(jsonPath("$.fullName").value("Luis Ramos"))
                .andExpect(jsonPath("$.email").value("luis@tickets.pe"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User saved = userRepository.findByUsername("luis").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("luis123");
        assertThat(passwordEncoder.matches("luis123", saved.getPasswordHash())).isTrue();
    }

    // Entrada:         token de ana (ROLE_USER) + los mismos datos
    // Salida esperada: 403, porque solo un ADMIN puede crear usuarios
    // Verificación:    el código es 403 y "luis" no existe en la base
    @Test
    @DisplayName("CP-04 POST /users como USER devuelve 403 y no crea nada")
    void create_asRegularUser_returns403() throws Exception {
        mockMvc.perform(post(API + "/users")
                        .header("Authorization", bearer("ana"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LUIS))
                .andExpect(status().isForbidden());

        assertThat(userRepository.existsByUsername("luis")).isFalse();
    }
}
