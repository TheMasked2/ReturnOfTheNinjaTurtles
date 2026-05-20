package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.order.enums.OrderStatus;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.transaction.repository.TransactionAccess;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderAccess orderAccess;

    @Autowired
    private TransactionAccess transactionAccess;

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmPaymentEndpoint_shouldConfirmOrderAndTransaction() throws Exception {
        UUID customerId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Roffa");
        request.setPaymentMethod("Visa");

        String placeOrderJson = mockMvc.perform(post("/api/checkout/customer/{customerId}/place-order", customerId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PlaceOrderResponse placeOrderResponse =
                objectMapper.readValue(placeOrderJson, PlaceOrderResponse.class);

        mockMvc.perform(post("/api/transactions/{transactionId}/confirm-payment", placeOrderResponse.getTransactionId())
                        .param("orderId", placeOrderResponse.getOrderId().toString()))
                .andExpect(status().isNoContent());

        assertThat(orderAccess.getOrderById(placeOrderResponse.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);

        assertThat(transactionAccess.findById(placeOrderResponse.getTransactionId()).orElseThrow().getStatus())
                .isEqualTo("SUCCESS");
    }
}