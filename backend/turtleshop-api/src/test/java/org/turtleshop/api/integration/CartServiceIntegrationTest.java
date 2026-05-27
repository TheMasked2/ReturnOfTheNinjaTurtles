package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.cart.dto.CartResponse;
import org.turtleshop.api.modules.cart.model.CartItem;
import org.turtleshop.api.modules.cart.repository.CartItemAccess;
import org.turtleshop.api.modules.cart.service.CartService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = {
                "/db/testdata/clean-test-data.sql",
                "/db/testdata/checkout-flow-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CartServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemAccess cartItemAccess;

    @Test
    void getActiveCartForUser_shouldReturnSeededCartWithItems() {
        UUID customerId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        CartResponse cart = cartService.getActiveCartForUser(customerId);

        assertThat(cart.getCartId()).isEqualTo(1);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProductId()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void changeQuantityOfCartItem_shouldPersistNewQuantity() {
        cartService.changeQuantityOfCartItem(1, 5);

        CartItem item = cartItemAccess.getCartItemById(1).orElseThrow();

        assertThat(item.getQuantity()).isEqualTo(5);
    }
}
