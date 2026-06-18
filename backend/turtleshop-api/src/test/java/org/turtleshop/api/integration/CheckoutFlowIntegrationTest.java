package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderResponse;
import org.turtleshop.api.modules.checkout.service.CheckoutService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = {
                "/db/testdata/clean-test-data.sql",
                "/db/testdata/checkout-flow-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CheckoutFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CheckoutService checkoutService;

    @Test
    void placeOrder_shouldCreateAwaitingPaymentOrder() {
        UUID customerId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Roffa");
        request.setPaymentMethod("Credit Card");

        PlaceOrderResponse response = checkoutService.placeOrder(customerId, request);

        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getShipmentId()).isNotNull();
        assertThat(response.getTransactionId()).isNotNull();
        assertThat(response.getOrderStatus()).isEqualTo("AWAITING_PAYMENT");
        assertThat(response.getTransactionStatus()).isEqualTo("PENDING");
    }
}