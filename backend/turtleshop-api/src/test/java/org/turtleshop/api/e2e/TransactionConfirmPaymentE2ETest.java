package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.order.enums.OrderStatus;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.transaction.dto.TransactionCreateRequest;
import org.turtleshop.api.modules.transaction.dto.TransactionUpdateRequest;
import org.turtleshop.api.modules.transaction.repository.TransactionAccess;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Sql(
        scripts = {
                "/db/testdata/clean-test-data.sql",
                "/db/testdata/checkout-flow-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class TransactionConfirmPaymentE2ETest extends IntegrationTestBase {

    private static final UUID LEONARDO_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderAccess orderAccess;

    @Autowired
    private TransactionAccess transactionAccess;

    @Test
    void confirmPaymentEndpoint_shouldConfirmOrderAndTransaction() throws Exception {
        PlaceOrderResponse placeOrderResponse = placeOrderForLeonardo();

        mockMvc.perform(post("/api/transactions/{transactionId}/confirm-payment", placeOrderResponse.getTransactionId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_UPDATE_ALL")
                        ))
                        .param("orderId", placeOrderResponse.getOrderId().toString()))
                .andExpect(status().isNoContent());

        assertThat(orderAccess.getOrderById(placeOrderResponse.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);

        assertThat(transactionAccess.findById(placeOrderResponse.getTransactionId()).orElseThrow().getStatus())
                .isEqualTo("SUCCESS");
    }

    @Test
    void transactionReadEndpoints_afterCheckout_shouldReturnCreatedPendingTransaction() throws Exception {
        PlaceOrderResponse placeOrderResponse = placeOrderForLeonardo();

        mockMvc.perform(get("/api/transactions")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").exists())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mockMvc.perform(get("/api/transactions/{transactionId}", placeOrderResponse.getTransactionId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(placeOrderResponse.getTransactionId()))
                .andExpect(jsonPath("$.orderId").value(placeOrderResponse.getOrderId()))
                .andExpect(jsonPath("$.paymentMethodId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/transactions/order/{orderId}", placeOrderResponse.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(placeOrderResponse.getTransactionId()));

        mockMvc.perform(get("/api/transactions/payment-method/{paymentMethodId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentMethodId").value(1));

        mockMvc.perform(get("/api/transactions/status/{status}", "PENDING")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void createUpdateAndDeleteTransactionEndpoints_withAdminAuthority_shouldPersistChanges() throws Exception {
        PlaceOrderResponse placeOrderResponse = placeOrderForLeonardo();

        TransactionCreateRequest createRequest = new TransactionCreateRequest(
                placeOrderResponse.getOrderId(),
                2,
                new BigDecimal("10.00"),
                "PENDING"
        );

        mockMvc.perform(post("/api/transactions")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions/order/{orderId}", placeOrderResponse.getOrderId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(placeOrderResponse.getOrderId()));

        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(
                new BigDecimal("12.34"),
                "FAILED",
                2
        );

        mockMvc.perform(put("/api/transactions/{transactionId}", placeOrderResponse.getTransactionId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions/{transactionId}", placeOrderResponse.getTransactionId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.paymentMethodId").value(2));

        mockMvc.perform(delete("/api/transactions/{transactionId}", placeOrderResponse.getTransactionId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_UPDATE_ALL")
                        )))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions/{transactionId}", placeOrderResponse.getTransactionId())
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("PAYMENT_READ_ALL")
                        )))
                .andExpect(status().isNotFound());
    }

    private PlaceOrderResponse placeOrderForLeonardo() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Roffa");
        request.setPaymentMethod("Credit Card");

        String placeOrderJson = mockMvc.perform(post("/api/checkout/customer/{customerId}/place-order", LEONARDO_CUSTOMER_ID)
                        .with(user("leonardo@example.com").authorities(
                                new SimpleGrantedAuthority("ORDER_CREATE_OWN")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(placeOrderJson, PlaceOrderResponse.class);
    }
}
