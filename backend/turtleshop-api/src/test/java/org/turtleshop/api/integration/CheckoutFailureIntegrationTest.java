package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.checkout.dto.PlaceOrderRequest;
import org.turtleshop.api.modules.checkout.service.CheckoutService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Sql(
        scripts = {
                "/db/testdata/clean-test-data.sql",
                "/db/testdata/checkout-flow-test-data.sql",
                "/db/testdata/checkout-low-inventory-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CheckoutFailureIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CheckoutService checkoutService;

    @Test
    void placeOrder_whenInventoryIsTooLow_shouldRejectCheckout() {
        UUID customerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setShippingMethod("PostNL");
        request.setShippingAddress("Roffa");
        request.setPaymentMethod("VISA/ Mastercard/ Amex");

        assertThatThrownBy(() -> checkoutService.placeOrder(customerId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not enough inventory");
    }
}
