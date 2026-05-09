package jorge.matias.plantilla.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    // 1. Levantamos el contenedor de Postgres
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    
    // 2. Le inyectamos las credenciales del contenedor a Spring Boot en tiempo de ejecución
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("security.jwt.refresh-cookie-name", () -> "refresh_token");
        registry.add("security.jwt.secret-key", () -> "EstaEsUnaClaveSecretaFalsaParaTestsDeIntegracion1234567890");
        registry.add("security.jwt.refresh-expiration-days", () -> "7");
        registry.add("app.security.jwt.refresh-cookie.secure", () -> "false"); // false para tests locales
        registry.add("security.jwt.refresh-path", () -> "/auth/refresh");
    }

    private String randomEmail;

    @BeforeEach
    void setUp() {
        // Generamos un email aleatorio para cada test y evitar colisiones en BBDD
        randomEmail = "testuser_" + UUID.randomUUID().toString() + "@example.com";
    }

    @Test
    @DisplayName("Debe registrar un usuario correctamente (HTTP 201)")
    void shouldRegisterUserSuccessfully() throws Exception {
        Map<String, String> registerRequest = Map.of(
            "email", randomEmail,
            "password", "Password123!"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Login WEB: Debe devolver AccessToken en body y RefreshToken en Cookie HttpOnly")
    void shouldLoginWebAndReturnCookie() throws Exception {
        // Primero registramos al usuario
        registerUser(randomEmail, "Password123!");

        Map<String, String> loginRequest = Map.of(
            "email", randomEmail,
            "password", "Password123!"
        );

        mockMvc.perform(post("/auth/login")
                .header("X-Client-Type", "WEB")
                .header("X-Device-ID", "device-web-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist()) // El body no debe tener el refresh token
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    @DisplayName("Login MOBILE: Debe devolver ambos tokens en el Body")
    void shouldLoginMobileAndReturnTokensInBody() throws Exception {
        registerUser(randomEmail, "Password123!");

        Map<String, String> loginRequest = Map.of(
            "email", randomEmail,
            "password", "Password123!"
        );

        mockMvc.perform(post("/auth/login")
                .header("X-Client-Type", "MOBILE")
                .header("X-Device-ID", "device-mob-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists()) // Aquí SÍ debe estar
                .andExpect(cookie().doesNotExist("refresh_token"));
    }

    @Test
    @DisplayName("Refresh WEB: Debe fallar si no se envía la cookie (404 NOT_FOUND)")
    void shouldFailRefreshWebWithoutCookie() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                .header("X-Client-Type", "WEB")
                .header("X-Device-ID", "device-web-1"))
                .andExpect(status().isNotFound()); // RefreshTokenNotFoundException → 404
    }

    @Test
    @DisplayName("Refresh WEB: Debe rotar los tokens enviando la cookie correcta")
    void shouldRefreshWebTokensSuccessfully() throws Exception {
        registerUser(randomEmail, "Password123!");

        // 1. Hacemos login para obtener la cookie inicial
        Map<String, String> loginRequest = Map.of(
            "email", randomEmail,
            "password", "Password123!"
        );

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .header("X-Client-Type", "WEB")
                .header("X-Device-ID", "device-web-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");

        // 2. Hacemos el refresh usando la cookie obtenida
        mockMvc.perform(post("/auth/refresh")
                .header("X-Client-Type", "WEB")
                .header("X-Device-ID", "device-web-1")
                .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refresh_token")); // Nos tiene que devolver una cookie nueva
    }

    // --- Helper para registrar usuarios rápido en los tests ---
    private void registerUser(String email, String password) throws Exception {
        Map<String, String> registerRequest = Map.of(
            "email", email,
            "password", password
        );
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Seguridad: Si se intenta reusar un token ya rotado, debe fallar (401)")
    void shouldFailIfTokenIsReused() throws Exception {
        registerUser(randomEmail, "Password123!");
        
        // 1. Login para obtener el primer refresh token
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .header("X-Client-Type", "WEB").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", randomEmail, "password", "Password123!"))))
                .andReturn();
        Cookie firstCookie = loginResult.getResponse().getCookie("refresh_token");

        // 2. Primer Refresh (Legítimo): El token 1 se gasta, nos dan el token 2
        mockMvc.perform(post("/auth/refresh").header("X-Client-Type", "WEB").cookie(firstCookie))
                .andExpect(status().isOk());

        // 3. Intento de Reúso (Ataque): Intentamos usar el token 1 otra vez
        mockMvc.perform(post("/auth/refresh").header("X-Client-Type", "WEB").cookie(firstCookie))
                .andExpect(status().isUnauthorized()); // RefreshTokenCompromisedException → 401
    }

    @Test
    @DisplayName("Login: Debe fallar con contraseña incorrecta")
    void shouldFailLoginWithWrongPassword() throws Exception {
        registerUser(randomEmail, "Password123!");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", randomEmail, "password", "wrong-pass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sesiones: Los tokens de diferentes dispositivos deben ser independientes")
    void shouldMaintainIndependentSessions() throws Exception {
        registerUser(randomEmail, "Password123!");

        // Login en WEB
        mockMvc.perform(post("/auth/login")
                .header("X-Client-Type", "WEB").header("X-Device-ID", "web-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", randomEmail, "password", "Password123!"))))
                .andExpect(cookie().exists("refresh_token"));

        // Login en MOBILE (Misma cuenta, distinto dispositivo)
        mockMvc.perform(post("/auth/login")
                .header("X-Client-Type", "MOBILE").header("X-Device-ID", "mobile-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", randomEmail, "password", "Password123!"))))
                .andExpect(jsonPath("$.refreshToken").exists());
        
    }
}