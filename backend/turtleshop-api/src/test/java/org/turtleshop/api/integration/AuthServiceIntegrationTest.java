package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.auth.dto.AuthResponse;
import org.turtleshop.api.modules.auth.dto.LoginRequest;
import org.turtleshop.api.modules.auth.dto.RegisterRequest;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.auth.repository.SystemRoleAccess;
import org.turtleshop.api.modules.auth.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = "/db/testdata/clean-test-data.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class AuthServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerAccess customerAccess;

    @Autowired
    private SystemRoleAccess systemRoleAccess;

    @Test
    void register_shouldPersistCustomerWithUserRoleAndAllowLogin() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("integration.leo@example.com")
                .password("Password123!")
                .firstName("Leonardo")
                .lastName("Integration")
                .phone("+31612345678")
                .build();

        AuthResponse registerResponse = authService.register(registerRequest);

        assertThat(registerResponse.getToken()).isNotBlank();
        assertThat(registerResponse.getCustomer().getEmail())
                .isEqualTo("integration.leo@example.com");

        LoginRequest loginRequest = LoginRequest.builder()
                .email("integration.leo@example.com")
                .password("Password123!")
                .build();

        AuthResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getCustomer().getEmail())
                .isEqualTo("integration.leo@example.com");
        assertThat(loginResponse.getCustomer().getRoles())
                .contains("ROLE_USER");
    }
}
