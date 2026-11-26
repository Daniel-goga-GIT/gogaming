package com.gogaming;

import com.gogaming.entities.Role;
import com.gogaming.entities.User;
import com.gogaming.repositories.RoleRepository;
import com.gogaming.repositories.UserRepository;
import com.gogaming.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La API completa (seguridad incluida) contra MariaDB con el catálogo de data.sql. */
class ApiIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;

    private String admin;
    private String cliente;

    @BeforeEach
    void usuarios() {
        admin = token("admin@test.local", "ROLE_ADMIN", false);
        cliente = token("cliente@test.local", "ROLE_USER", false);
    }

    private String token(String email, String rol, boolean pendiente2fa) {
        User user = users.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFirstName("Test");
            u.setLastName("Test");
            u.setPassword("{noop}no-se-usa");
            u.setEnabled(true);
            Role r = roles.findByName(rol).orElseThrow();
            u.setRoles(Set.of(r));
            return users.save(u);
        });
        return "Bearer " + jwtUtil.generateToken(email, List.of(rol), user.getId(), pendiente2fa);
    }

    private static final String RATON = """
            {"nombre":"Test Mouse","marca":"QA","precio":49.99,"stock":3,"image":"https://img/t.png",
             "tipoAgarre":"Claw","medidas":"118x60x38mm","sensor":"PAW3395","peso":52,"switches":"Optical"}""";

    @Test
    void elCatalogoEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos/raton").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", greaterThan(5)));
        mvc.perform(get("/api/v1/productos/home")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)));
        mvc.perform(get("/api/v1/productos/teclado/39")).andExpect(status().isOk()).andExpect(jsonPath("$.tipoProducto").value("Teclado"));
        mvc.perform(get("/api/v1/productos/resenas/resumen").param("ids", "1,2")).andExpect(status().isOk());
    }

    @Test
    void unTokenCaducadoOInventadoNoRompeElCatalogo() throws Exception {
        mvc.perform(get("/api/v1/productos/home").header("Authorization", "Bearer token.inventado.x"))
                .andExpect(status().isOk());
    }

    @Test
    void lasRutasDesconocidasDan404() throws Exception {
        mvc.perform(get("/api/v1/productos/patinete")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/productos/raton/39")).andExpect(status().isNotFound()); // el 39 es un teclado
    }

    @Test
    void escribirEnElCatalogoExigeSerAdmin() throws Exception {
        mvc.perform(post("/api/v1/productos/raton").contentType(MediaType.APPLICATION_JSON).content(RATON))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/productos/raton").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON).content(RATON))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/productos/raton/1").header("Authorization", cliente))
                .andExpect(status().isForbidden());
    }

    @Test
    void unTokenPendienteDe2faNoSirveNiSiendoAdmin() throws Exception {
        String pendiente = token("admin@test.local", "ROLE_ADMIN", true);

        mvc.perform(post("/api/v1/productos/raton").header("Authorization", pendiente).contentType(MediaType.APPLICATION_JSON).content(RATON))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/me").header("Authorization", pendiente))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elAdminPuedeCrearEditarYBorrarProductos() throws Exception {
        String json = mvc.perform(post("/api/v1/productos/raton").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(RATON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoProducto").value("Raton"))
                .andReturn().getResponse().getContentAsString();
        String id = json.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(put("/api/v1/productos/raton/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(RATON.replace("Test Mouse", "Test Mouse 2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test Mouse 2"));
        mvc.perform(get("/api/v1/productos/raton").param("nombre", "test mouse"))
                .andExpect(jsonPath("$.content[*].nombre", hasItem("Test Mouse 2")));

        mvc.perform(delete("/api/v1/productos/raton/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/productos/raton/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void losDatosInvalidosDevuelven400ConLosMotivos() throws Exception {
        mvc.perform(post("/api/v1/productos/raton").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\",\"precio\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem("Sensor es obligatorio")));
    }

    @Test
    void elRegistroValidaLosDatosEnElServidor() throws Exception {
        mvc.perform(multipart("/api/v1/users/register")
                        .param("firstName", "Ana").param("lastName", "López")
                        .param("email", "no-es-un-email").param("password", "Segura123"))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/v1/users/register")
                        .param("firstName", "Ana").param("lastName", "López")
                        .param("email", "ana@test.local").param("password", "debil"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reseñarExigeLoginYSoloUnaVezPorProducto() throws Exception {
        String body = "{\"calificacion\":5,\"contenido\":\"Muy bueno\"}";
        mvc.perform(post("/api/v1/productos/2/resenas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/productos/2/resenas").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/productos/2/resenas").header("Authorization", cliente).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/productos/resenas/resumen").param("ids", "2"))
                .andExpect(jsonPath("$['2'].totalReviews").value(1))
                .andExpect(jsonPath("$['2'].averageRating").value(5.0));
    }
}
