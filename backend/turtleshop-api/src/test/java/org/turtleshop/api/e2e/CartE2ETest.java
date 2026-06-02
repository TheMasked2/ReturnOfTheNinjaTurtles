package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.cart.dto.AddCartItemRequest;
import org.turtleshop.api.modules.cart.dto.UpdateCartItemQuantityRequest;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CartE2ETest extends IntegrationTestBase {

    private static final UUID LEONARDO_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID APRIL_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getActiveCartEndpoint_withAdminAuthority_shouldReturnSeededCart() throws Exception {
        mockMvc.perform(get("/api/cart/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void getAllActiveCartsEndpoint_withAdminAuthority_shouldReturnOnlyActiveCarts() throws Exception {
        mockMvc.perform(get("/api/cart/active")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartId").exists())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void createCartEndpoint_forCustomerWithoutActiveCart_shouldCreateActiveCart() throws Exception {
        mockMvc.perform(post("/api/cart/{customerId}", APRIL_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_CREATE_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(APRIL_CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void addItemToCartEndpoint_withAdminAuthority_shouldPersistNewCartItem() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(2);
        request.setQuantity(3);

        mockMvc.perform(post("/api/cart/{customerId}/items", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.productId").value(2))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void updateCartItemQuantityEndpoint_withAdminAuthority_shouldUpdateExistingItem() throws Exception {
        UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest();
        request.setQuantity(5);

        mockMvc.perform(patch("/api/cart/items/{cartItemId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cartItemId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    void deleteCartItemEndpoint_withAdminAuthority_shouldRemoveItemFromActiveCart() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_DELETE_ALL")
                        )))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CART_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void getActiveCartEndpoint_withoutCartAuthority_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/cart/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("visitor@example.com")))
                .andExpect(status().isForbidden());
    }
}
