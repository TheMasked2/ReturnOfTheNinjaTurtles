package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.transaction.dto.PaymentMethodCreateRequest;
import org.turtleshop.api.modules.transaction.service.PaymentMethodService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PaymentMethodE2ETest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Test
    void listPaymentMethodsEndpoint_withAdminAuthority_shouldReturnSeededPaymentMethods() throws Exception {
        mockMvc.perform(get("/api/payment-methods")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentMethodId").exists())
                .andExpect(jsonPath("$[0].provider").exists());
    }

    @Test
    void getPaymentMethodByIdEndpoint_withAdminAuthority_shouldReturnSeededPaymentMethod() throws Exception {
        mockMvc.perform(get("/api/payment-methods/{paymentMethodId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethodId").value(1))
                .andExpect(jsonPath("$.provider").value("VISA/ Mastercard/ Amex"))
                .andExpect(jsonPath("$.type").value("Credit Card"));
    }

    @Test
    void createPaymentMethodEndpoint_withAdminAuthority_shouldPersistPaymentMethod() throws Exception {
        PaymentMethodCreateRequest request = new PaymentMethodCreateRequest("E2E Pay", "Online Wallet");

        mockMvc.perform(post("/api/payment-methods")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(paymentMethodService.listPaymentMethods())
                .anySatisfy(paymentMethod -> {
                    assertThat(paymentMethod.getProvider()).isEqualTo("E2E Pay");
                    assertThat(paymentMethod.getType()).isEqualTo("Online Wallet");
                });
    }

    @Test
    void createPaymentMethodEndpoint_withoutPaymentUpdateAuthority_shouldReturnForbidden() throws Exception {
        PaymentMethodCreateRequest request = new PaymentMethodCreateRequest("Forbidden Pay", "Online Wallet");

        mockMvc.perform(post("/api/payment-methods")
                        .with(user("visitor@example.com"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
