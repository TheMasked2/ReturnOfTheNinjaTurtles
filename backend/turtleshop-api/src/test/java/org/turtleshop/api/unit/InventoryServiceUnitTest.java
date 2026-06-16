package org.turtleshop.api.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.repository.InventoryAccess;
import org.turtleshop.api.modules.inventory.service.InventoryService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceUnitTest {

    @Mock
    private InventoryAccess inventoryAccess;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reserveStock_whenEnoughAvailableStock_shouldMoveAvailableToReserved() {
        when(inventoryAccess.findByProductId(1)).thenReturn(Optional.of(
                InventoryModel.builder().productId(1).quantityAvailable(10).quantityReserved(2).build()
        ));

        inventoryService.reserveStock(1, 4);

        verify(inventoryAccess).updateQuantitiesByProductId(1, 6, 6);
    }

    @Test
    void reserveStock_whenQuantityIsZero_shouldThrowBadRequest() {
        when(inventoryAccess.findByProductId(1)).thenReturn(Optional.of(
                InventoryModel.builder().productId(1).quantityAvailable(10).quantityReserved(2).build()
        ));

        assertThatThrownBy(() -> inventoryService.reserveStock(1, 0))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Insufficient available stock");
    }

    @Test
    void consumeStock_whenReservedStockIsEnough_shouldDecreaseReservedStock() {
        when(inventoryAccess.findByProductId(1)).thenReturn(Optional.of(
                InventoryModel.builder().productId(1).quantityAvailable(6).quantityReserved(6).build()
        ));

        inventoryService.consumeStock(1, 2);

        verify(inventoryAccess).updateQuantitiesByProductId(1, 6, 4);
    }

    @Test
    void releaseStock_whenReservedStockIsEnough_shouldMoveReservedBackToAvailable() {
        when(inventoryAccess.findByProductId(1)).thenReturn(Optional.of(
                InventoryModel.builder().productId(1).quantityAvailable(6).quantityReserved(6).build()
        ));

        inventoryService.releaseStock(1, 2);

        verify(inventoryAccess).updateQuantitiesByProductId(1, 8, 4);
    }

    @Test
    void restock_whenQuantityIsPositive_shouldIncreaseAvailableStock() {
        when(inventoryAccess.findByProductId(1)).thenReturn(Optional.of(
                InventoryModel.builder().productId(1).quantityAvailable(10).quantityReserved(2).build()
        ));

        inventoryService.restock(1, 5);

        verify(inventoryAccess).updateQuantitiesByProductId(1, 15, 2);
    }
}
