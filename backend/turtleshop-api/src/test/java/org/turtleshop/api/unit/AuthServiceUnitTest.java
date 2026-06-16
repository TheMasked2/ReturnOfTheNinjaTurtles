package org.turtleshop.api.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.dto.AuthResponse;
import org.turtleshop.api.modules.auth.dto.LoginRequest;
import org.turtleshop.api.modules.auth.dto.RegisterRequest;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.auth.repository.SystemRoleAccess;
import org.turtleshop.api.modules.auth.service.AuthService;
import org.turtleshop.api.modules.auth.service.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private CustomerAccess customerAccess;

    @Mock
    private SystemRoleAccess roleAccess;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_whenEmailIsNew_shouldCreateCustomerAssignUserRoleAndReturnToken() {
        UUID customerId = UUID.randomUUID();
        RegisterRequest request = RegisterRequest.builder()
                .email("leo@example.com")
                .password("Password123!")
                .firstName("Leonardo")
                .lastName("Hamato")
                .build();

        when(customerAccess.existsByEmail("leo@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(customerAccess.insertAndReturnId(any(Customer.class))).thenReturn(customerId);
        when(roleAccess.findPermissionCodesByCustomerEmail("leo@example.com")).thenReturn(List.of("CART_READ_OWN"));
        when(jwtService.generateToken("leo@example.com", List.of("ROLE_USER"), List.of("CART_READ_OWN")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerAccess).insertAndReturnId(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getPassword()).isEqualTo("encoded-password");
        verify(customerAccess).addRoleToCustomer(customerId, "ROLE_USER");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getCustomer().getId()).isEqualTo(customerId);
        assertThat(response.getCustomer().getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void register_whenEmailAlreadyExists_shouldThrowConflictAndNotInsert() {
        RegisterRequest request = RegisterRequest.builder()
                .email("leo@example.com")
                .password("Password123!")
                .firstName("Leonardo")
                .build();

        when(customerAccess.existsByEmail("leo@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email already in use");

        verify(customerAccess, never()).insertAndReturnId(any(Customer.class));
    }

    @Test
    void login_whenPasswordMatches_shouldReturnCustomerRolesAndToken() {
        UUID customerId = UUID.randomUUID();
        LoginRequest request = LoginRequest.builder()
                .email("leo@example.com")
                .password("Password123!")
                .build();
        Customer customer = Customer.builder()
                .customerId(customerId)
                .email("leo@example.com")
                .password("encoded-password")
                .firstName("Leonardo")
                .lastName("Hamato")
                .build();

        when(customerAccess.findByEmail("leo@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("Password123!", "encoded-password")).thenReturn(true);
        when(roleAccess.findRoleNamesByCustomerEmail("leo@example.com")).thenReturn(List.of("ROLE_USER"));
        when(roleAccess.findPermissionCodesByCustomerEmail("leo@example.com")).thenReturn(List.of("ORDER_CREATE_OWN"));
        when(jwtService.generateToken("leo@example.com", List.of("ROLE_USER"), List.of("ORDER_CREATE_OWN")))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getCustomer().getEmail()).isEqualTo("leo@example.com");
        assertThat(response.getCustomer().getRoles()).containsExactly("ROLE_USER");
    }

    @Test
    void login_whenPasswordDoesNotMatch_shouldThrowUnauthorized() {
        LoginRequest request = LoginRequest.builder()
                .email("leo@example.com")
                .password("wrong")
                .build();
        Customer customer = Customer.builder()
                .email("leo@example.com")
                .password("encoded-password")
                .build();

        when(customerAccess.findByEmail("leo@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid credentials");
    }
}
