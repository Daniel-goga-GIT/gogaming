package com.gogaming.controllers;

import com.gogaming.TestKeys;
import com.gogaming.entities.Role;
import com.gogaming.entities.User;
import com.gogaming.services.EmailService;
import com.gogaming.services.UserService;
import com.gogaming.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Login en dos pasos, sin contexto de Spring (MockMvc standalone con dependencias simuladas). */
class AuthenticationControllerTest {

    private static final String EMAIL = "ana@example.com";

    private final AuthenticationManager authManager = mock(AuthenticationManager.class);
    private final UserService userService = mock(UserService.class);
    private final EmailService emailService = mock(EmailService.class);
    private final JwtUtil jwtUtil = new JwtUtil(TestKeys.keyPair());
    private MockMvc mvc;
    private User usuario;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new AuthenticationController(authManager, jwtUtil, userService, emailService)).build();

        usuario = new User();
        usuario.setId(1L);
        usuario.setEmail(EMAIL);
        Role rol = new Role();
        rol.setName("ROLE_USER");
        usuario.setRoles(Set.of(rol));

        when(userService.getIdByEmail(EMAIL)).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(usuario);
        // saveCode y clear2FactorCode actúan sobre el usuario simulado, como haría la BD.
        doAnswer(inv -> { usuario.setCode(inv.getArgument(0)); return null; }).when(userService).saveCode(anyString(), eq(1L));
        doAnswer(inv -> { usuario.setCode(null); return null; }).when(userService).clear2FactorCode(1L);
    }

    private ResultActions login(String password) throws Exception {
        return mvc.perform(post("/api/v1/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + password + "\"}"));
    }

    private String tokenPendiente() throws Exception {
        when(authManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(
                EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        String json = login("Correcta1").andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private ResultActions verificar(String token, String codigo) throws Exception {
        return mvc.perform(post("/api/v1/twofactor").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":" + (codigo == null ? "null" : "\"" + codigo + "\"") + "}"));
    }

    @Test
    void elPrimerPasoDevuelveUnTokenPendienteYEnviaUnCodigoDeSeisCifras() throws Exception {
        String token = tokenPendiente();

        assertThat(jwtUtil.isPendingTwoFactor(jwtUtil.extractAllClaims(token))).isTrue();
        assertThat(usuario.getCode()).matches("\\d{6}");
        verify(emailService).sendTwoFactorCodeEmail(EMAIL, usuario.getCode());
    }

    @Test
    void conElCodigoCorrectoSeObtieneElTokenCompletoYElCodigoNoSePuedeReutilizar() throws Exception {
        String token = tokenPendiente();
        String codigo = usuario.getCode();

        String json = verificar(token, codigo).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String completo = json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        assertThat(jwtUtil.isPendingTwoFactor(jwtUtil.extractAllClaims(completo))).isFalse();

        verificar(token, codigo).andExpect(status().isUnauthorized());
    }

    @Test
    void unCodigoNuloOMalFormadoEsUn400() throws Exception {
        String token = tokenPendiente();

        verificar(token, null).andExpect(status().isBadRequest());
        verificar(token, "12ab").andExpect(status().isBadRequest());
    }

    @Test
    void trasCincoCodigosErroneosElCodigoQuedaInvalidado() throws Exception {
        String token = tokenPendiente();
        String bueno = usuario.getCode();
        String malo = bueno.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 4; i++) {
            verificar(token, malo).andExpect(status().isUnauthorized());
        }
        verificar(token, malo).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Demasiados")));
        verificar(token, bueno).andExpect(status().isUnauthorized());
        assertThat(usuario.getCode()).isNull();
    }

    @Test
    void unTokenCompletoNoSirveParaElSegundoPaso() throws Exception {
        String completo = jwtUtil.generateToken(EMAIL, List.of("ROLE_USER"), 1L, false);

        verificar(completo, "123456").andExpect(status().isBadRequest());
    }

    @Test
    void trasDiezContrasenasErroneasLaCuentaSeBloqueaTemporalmente() throws Exception {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));

        for (int i = 0; i < 10; i++) {
            login("Mala" + i).andExpect(status().isUnauthorized());
        }
        login("Correcta1").andExpect(status().isTooManyRequests());
    }
}
