package org.turtleshop.api.modules.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.repository.InventoryAccess;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryAccess inventoryAccess;

    // Delete Inventory by Product ID
    public void deleteInventoryByProductId(int productId) {
        inventoryAccess.deleteByProductId(productId);
    }

    // Update Inventory quantities
    public void updateInventoryByProductId(int productId, int quantityAvailable, int quantityReserved) {
        inventoryAccess.updateInventoryByProductId(productId, quantityAvailable, quantityReserved);
    }

     // Create new Inventory record
    public int createInventory(int productId, int quantityAvailable, int quantityReserved) {
        InventoryModel inventory = InventoryModel.builder()
                .productId(productId)
                .quantityAvailable(quantityAvailable)
                .quantityReserved(quantityReserved)
                .build();
        return inventoryAccess.insertInventory(inventory);
    }

    // Get Inventory by Product ID
    public InventoryModel getInventoryByProductId(int productId) {
        return inventoryAccess.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found for product ID: " + productId));
    }

    // Get Inventory by Quantity Available
    public InventoryModel getInventoryByProductQuantityAvailable(int quantity) {
        return inventoryAccess.findByQuantityAvailable(quantity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No inventory found with quantity available >= " + quantity));
    }

    // Find last updated inventory record
    public List<InventoryModel> getLastUpdatedInventory(int limit) {
        return inventoryAccess.findLastUpdated(limit);
    }
}