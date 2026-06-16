package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class WishlistE2ETest extends IntegrationTestBase {

    private static final UUID LEONARDO_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void wishlistReadEndpoints_withAdminAuthority_shouldReturnSeededWishlists() throws Exception {
        mockMvc.perform(get("/api/wishlist")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].wishlistId").exists())
                .andExpect(jsonPath("$[0].customerId").exists());

        mockMvc.perform(get("/api/wishlist/{id}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlistId").value(1))
                .andExpect(jsonPath("$.customerId").value(LEONARDO_CUSTOMER_ID.toString()));

        mockMvc.perform(get("/api/wishlist/customer/{customerId}", LEONARDO_CUSTOMER_ID)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlistId").value(1));
    }

    @Test
    void wishlistItemReadEndpoints_withAdminAuthority_shouldReturnSeededWishlistItems() throws Exception {
        mockMvc.perform(get("/api/wishlist-item")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].wishlistItemId").exists())
                .andExpect(jsonPath("$[0].wishlistId").exists());

        mockMvc.perform(get("/api/wishlist-item/{id}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlistItemId").value(1))
                .andExpect(jsonPath("$.wishlistId").value(1))
                .andExpect(jsonPath("$.productId").value(1));

        mockMvc.perform(get("/api/wishlist-item/wishlist/{wishlistId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].wishlistId").value(1));

        mockMvc.perform(get("/api/wishlist-item/product/{productId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void wishlistItemCreateReadDeleteFlow_withAdminAuthority_shouldPersistChanges() throws Exception {
        String newWishlistItemIdJson = mockMvc.perform(post("/api/wishlist-item/wishlist/{wishlistId}/product/{productId}/return-id", 1, 2)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_UPDATE_ALL")
                        )))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer newWishlistItemId = objectMapper.readValue(newWishlistItemIdJson, Integer.class);

        mockMvc.perform(get("/api/wishlist-item/{id}", newWishlistItemId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlistItemId").value(newWishlistItemId))
                .andExpect(jsonPath("$.wishlistId").value(1))
                .andExpect(jsonPath("$.productId").value(2));

        mockMvc.perform(delete("/api/wishlist-item/{id}", newWishlistItemId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_DELETE_ALL")
                        )))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/wishlist-item/{id}", newWishlistItemId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("WISHLIST_READ_ALL")
                        )))
                .andExpect(status().isNotFound());
    }

    @Test
    void wishlistReadEndpoint_withoutWishlistAuthority_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/wishlist/{id}", 1)
                        .with(user("visitor@example.com")))
                .andExpect(status().isForbidden());
    }
}
