package org.turtleshop.api.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.inventory.dto.InventoryAdjustmentRequest;
import org.turtleshop.api.modules.inventory.dto.InventoryUpdateRequest;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.service.InventoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
class InventoryE2ETest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void inventoryReadEndpoints_withInventoryAuthority_shouldReturnSeededInventory() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("page", "0")
                        .param("size", "5")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").exists())
                .andExpect(jsonPath("$[0].productId").exists());

        mockMvc.perform(get("/api/inventory/{inventoryId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryId").value(1))
                .andExpect(jsonPath("$.productId").value(1));

        mockMvc.perform(get("/api/inventory/product/{productId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantityAvailable").value(20));
    }

    @Test
    void inventoryFilterEndpoints_withInventoryAuthority_shouldReturnMatchingInventory() throws Exception {
        mockMvc.perform(get("/api/inventory/quantity/{quantity}", 20)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").exists());

        mockMvc.perform(get("/api/inventory/low-stock")
                        .param("threshold", "20")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantityAvailable").exists());

        mockMvc.perform(get("/api/inventory/batch")
                        .param("productIds", "1", "2")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_READ_ALL")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").exists());
    }

    @Test
    void reserveStockEndpoint_withInventoryAuthority_shouldPersistQuantityChanges() throws Exception {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest();
        request.setQuantity(3);

        mockMvc.perform(post("/api/inventory/product/{productId}/reserve", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        InventoryModel inventory = inventoryService.getInventoryByProductId(1);
        assertThat(inventory.getQuantityAvailable()).isEqualTo(17);
        assertThat(inventory.getQuantityReserved()).isEqualTo(3);
    }

    @Test
    void reserveReleaseConsumeAndRestockEndpoints_shouldKeepInventoryQuantitiesConsistent() throws Exception {
        InventoryAdjustmentRequest reserveRequest = new InventoryAdjustmentRequest();
        reserveRequest.setQuantity(3);

        mockMvc.perform(post("/api/inventory/product/{productId}/reserve", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isNoContent());

        InventoryAdjustmentRequest releaseRequest = new InventoryAdjustmentRequest();
        releaseRequest.setQuantity(2);

        mockMvc.perform(post("/api/inventory/product/{productId}/release", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(releaseRequest)))
                .andExpect(status().isNoContent());

        InventoryAdjustmentRequest consumeRequest = new InventoryAdjustmentRequest();
        consumeRequest.setQuantity(1);

        mockMvc.perform(post("/api/inventory/product/{productId}/consume", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(consumeRequest)))
                .andExpect(status().isNoContent());

        InventoryAdjustmentRequest restockRequest = new InventoryAdjustmentRequest();
        restockRequest.setQuantity(5);

        mockMvc.perform(post("/api/inventory/product/{productId}/restock", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(restockRequest)))
                .andExpect(status().isNoContent());

        InventoryModel inventory = inventoryService.getInventoryByProductId(1);
        assertThat(inventory.getQuantityAvailable()).isEqualTo(24);
        assertThat(inventory.getQuantityReserved()).isEqualTo(0);
    }

    @Test
    void updateInventoryEndpoint_withInventoryAuthority_shouldPersistNewQuantities() throws Exception {
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setQuantityAvailable(11);
        request.setQuantityReserved(4);

        mockMvc.perform(put("/api/inventory/product/{productId}", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        InventoryModel inventory = inventoryService.getInventoryByProductId(1);
        assertThat(inventory.getQuantityAvailable()).isEqualTo(11);
        assertThat(inventory.getQuantityReserved()).isEqualTo(4);
    }

    @Test
    void reserveStockEndpoint_whenQuantityIsTooHigh_shouldReturnBadRequest() throws Exception {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest();
        request.setQuantity(999);

        mockMvc.perform(post("/api/inventory/product/{productId}/reserve", 1)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("INVENTORY_RESERVE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listInventoryEndpoint_withoutInventoryAuthority_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .with(user("visitor@example.com")))
                .andExpect(status().isForbidden());
    }
}
