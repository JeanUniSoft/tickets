package pe.edu.utp.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CategoryControllerTest extends BaseIntegrationTest {

    // Entrada:         token de ana
    // Salida esperada: 200 y la lista de categorías
    // Verificación:    hay 1 categoría y trae sus 4 atributos (id, name, description, active)
    @Test
    @DisplayName("CP-05 GET /categories con token devuelve 200 y la lista")
    void findAll_withToken_returnsList() throws Exception {
        mockMvc.perform(get(API + "/categories").header("Authorization", bearer("ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(asInt(hardware.getId())))
                .andExpect(jsonPath("$[0].name").value("Hardware"))
                .andExpect(jsonPath("$[0].description").value("Equipos y periféricos"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    // Entrada:         ninguna cabecera Authorization
    // Salida esperada: 401
    // Verificación:    el código es 401
    @Test
    @DisplayName("CP-06 GET /categories sin token devuelve 401")
    void findAll_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(API + "/categories"))
                .andExpect(status().isUnauthorized());
    }

    // Entrada:         token de admin + categoría "Redes"
    // Salida esperada: 201 y la categoría creada, activa
    // Verificación:    la respuesta trae id y en la base ahora hay 2 categorías
    @Test
    @DisplayName("CP-07 POST /categories como ADMIN crea la categoría y devuelve 201")
    void create_asAdmin_returns201() throws Exception {
        mockMvc.perform(post(API + "/categories")
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Redes", "description": "Internet, wifi y servidores"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Redes"))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(categoryRepository.count()).isEqualTo(2);
    }

    // Entrada:         token de admin + categoría con el nombre vacío
    // Salida esperada: 400 y el campo que falló dentro de "errors"
    // Verificación:    "errors.name" existe y en la base sigue habiendo 1 categoría
    @Test
    @DisplayName("CP-08 POST /categories con nombre vacío devuelve 400")
    void create_withBlankName_returns400() throws Exception {
        mockMvc.perform(post(API + "/categories")
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "description": "Sin nombre"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        assertThat(categoryRepository.count()).isEqualTo(1);
    }
}
