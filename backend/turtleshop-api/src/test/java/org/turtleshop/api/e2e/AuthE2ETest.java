package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.auth.dto.LoginRequest;
import org.turtleshop.api.modules.auth.dto.RegisterRequest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Sql(
        scripts = "/db/testdata/clean-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class AuthE2ETest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLoginEndpoints_shouldCreateCustomerAndReturnTokens() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("e2e.leo@example.com")
                .password("Password123!")
                .firstName("Leonardo")
                .lastName("E2E")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.customer.email").value("e2e.leo@example.com"));

        LoginRequest loginRequest = LoginRequest.builder()
                .email("e2e.leo@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.customer.roles[0]").value("ROLE_USER"));
    }

    @Test
    void registerEndpoint_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .password("Password123!")
                .firstName("Invalid")
                .lastName("Email")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerEndpoint_withDuplicateEmail_shouldReturnConflict() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@example.com")
                .password("Password123!")
                .firstName("Duplicate")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginEndpoint_withWrongPassword_shouldReturnUnauthorized() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("wrong.password@example.com")
                .password("Password123!")
                .firstName("Wrong")
                .lastName("Password")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrong.password@example.com")
                .password("WrongPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meAndLogoutEndpoints_withAuthenticatedUser_shouldReturnCurrentUserAndLogoutMessage() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("current.user@example.com")
                .password("Password123!")
                .firstName("Current")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me")
                        .with(user("current.user@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("current.user@example.com"))
                .andExpect(jsonPath("$.firstName").value("Current"));

        mockMvc.perform(post("/api/auth/logout")
                        .with(user("current.user@example.com")))
                .andExpect(status().isOk());
    }
}
