package jorge.matias.plantilla.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jorge.matias.plantilla.controller.dto.request.account.*;
import jorge.matias.plantilla.model.entity.Account;
import jorge.matias.plantilla.model.enums.AccountRole;
import jorge.matias.plantilla.model.enums.AccountStatus;
import jorge.matias.plantilla.model.entity.AccountPrincipal;
import jorge.matias.plantilla.repository.AccountRepository;
import jorge.matias.plantilla.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("1234");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("security.jwt.refresh-cookie-name", () -> "refresh_token");
        registry.add("security.jwt.secret-key", () -> "EstaEsUnaClaveSecretaFalsaParaTestsDeIntegracion1234567890");
        registry.add("security.jwt.refresh-expiration-days", () -> "7");
        registry.add("app.security.jwt.refresh-cookie.secure", () -> "false");
        registry.add("security.jwt.refresh-path", () -> "/auth/refresh");
    }

    // --- ESTADO DEL TEST ---
    private Account testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll(); // Limpieza 

        testUser = Account.builder()
                .email("chad@test.com")
                .password(passwordEncoder.encode("oldPassword123!"))
                .roles(List.of(AccountRole.USER))
                .status(AccountStatus.ACTIVE)
                .build();
        
        testUser = accountRepository.save(testUser);

        // Usamos el Factory Method que implementaste. Magia pura.
        AccountPrincipal principal = AccountPrincipal.build(testUser);
        validToken = jwtService.generateAccessToken(principal); 
    }


    @Test
    @DisplayName("Caso 1: Happy Path - Debería cambiar la contraseña y devolver 200 OK 🗿")
    void shouldChangePasswordSuccessfully() throws Exception {
        ChangePasswordRequest payload = new ChangePasswordRequest("oldPassword123!", "SuperNewPassword456@");
        String jsonPayload = objectMapper.writeValueAsString(payload);
        System.out.println("🔥 EL JSON QUE ESTOY MANDANDO ES: " + jsonPayload);

        mockMvc.perform(put("/account/password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());
                

        // Verificación extra: Vamos a la BD a comprobar que de verdad se encriptó la nueva
        Account updatedUser = accountRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("SuperNewPassword456@", updatedUser.getPassword()));
    }

    @Test
    @DisplayName("Caso 2: Red Flag 🚩 - Sin token, patada en la boca (401 Unauthorized)")
    void shouldReturn401_WhenNoTokenProvided() throws Exception {
        ChangePasswordRequest payload = new ChangePasswordRequest("oldPassword123!", "SuperNewPassword456@");

        mockMvc.perform(put("/account/password")
                // Falta el header a propósito
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Caso 3: Troll detectado 🤡 - Contraseña actual incorrecta (400 Bad Request)")
    void shouldFail_WhenOldPasswordIsIncorrect() throws Exception {
        ChangePasswordRequest payload = new ChangePasswordRequest("claveFalsaJaja", "SuperNewPassword456@");

        mockMvc.perform(put("/account/password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // IncorrectPasswordException → 400
    }

    @Test
    @DisplayName("Caso 4: Pereza máxima 🦥 - Misma contraseña vieja y nueva (400 Bad Request)")
    void shouldFail_WhenNewPasswordIsSameAsOld() throws Exception {
        ChangePasswordRequest payload = new ChangePasswordRequest("oldPassword123!", "oldPassword123!");

        mockMvc.perform(put("/account/password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); // PasswordReuseException → 400
    }

    @Test
    @DisplayName("Caso 5: DTO Inválido 🗑️ - Contraseña nueva vacía o ridícula (400 Bad Request)")
    void shouldFail_WhenPayloadIsInvalid() throws Exception {
        // Asumiendo que pusiste @NotBlank y @Size en tu ChangePasswordRequest
        ChangePasswordRequest payload = new ChangePasswordRequest("oldPassword123!", "");

        mockMvc.perform(put("/account/password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
}