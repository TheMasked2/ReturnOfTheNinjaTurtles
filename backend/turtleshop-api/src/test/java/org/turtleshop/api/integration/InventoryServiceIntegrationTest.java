package org.turtleshop.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.service.InventoryService;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(
        scripts = {
                "/db/testdata/clean-test-data.sql",
                "/db/testdata/checkout-flow-test-data.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class InventoryServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private InventoryService inventoryService;

    @Test
    void reserveStock_shouldPersistAvailableAndReservedQuantityChanges() {
        inventoryService.reserveStock(1, 4);

        InventoryModel inventory = inventoryService.getInventoryByProductId(1);

        assertThat(inventory.getQuantityAvailable()).isEqualTo(16);
        assertThat(inventory.getQuantityReserved()).isEqualTo(4);
    }

    @Test
    void restock_shouldPersistAvailableQuantityIncrease() {
        inventoryService.restock(1, 5);

        InventoryModel inventory = inventoryService.getInventoryByProductId(1);

        assertThat(inventory.getQuantityAvailable()).isEqualTo(25);
        assertThat(inventory.getQuantityReserved()).isEqualTo(0);
    }
}
