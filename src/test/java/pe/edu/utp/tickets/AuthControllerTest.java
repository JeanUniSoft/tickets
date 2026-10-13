package pe.edu.utp.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthControllerTest extends BaseIntegrationTest {

    // Entrada:         usuario "admin" con su contraseña correcta
    // Salida esperada: 200 y un JSON con el campo "token"
    // Verificación:    el token no está vacío y, al abrirlo, su dueño es "admin"
    @Test
    @DisplayName("CP-01 POST /auth/login con credenciales correctas devuelve 200 y un token")
    void login_withValidCredentials_returnsToken() throws Exception {
        MvcResult result = mockMvc.perform(post(API + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    // Entrada:         usuario "admin" con una contraseña equivocada
    // Salida esperada: 401 con el motivo en "detail"
    // Verificación:    el código es 401 y la respuesta no trae ningún token
    @Test
    @DisplayName("CP-02 POST /auth/login con contraseña incorrecta devuelve 401")
    void login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post(API + "/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "no-es-la-clave"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Usuario o contraseña incorrectos"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }
}
