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

    public List<InventoryModel> listInventory(int page, int size) {
        return inventoryAccess.findAll(page * size, size);
    }

    public InventoryModel getInventoryById(int inventoryId) {
        return inventoryAccess.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory record not found: " + inventoryId));
    }

    public InventoryModel getInventoryByProductId(int productId) {
        return inventoryAccess.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory not found for product ID: " + productId));
    }

    public List<InventoryModel> getInventoryByQuantityAvailable(int quantity) {
        return inventoryAccess.findByQuantityAvailableGreaterOrEqual(quantity);
    }

    public List<InventoryModel> getByProductIds(List<Integer> productIds) {
        return inventoryAccess.findByProductIds(productIds);
    }

    public List<InventoryModel> getLowStock(int threshold) {
        return inventoryAccess.findLowStock(threshold);
    }

    public List<InventoryModel> getLastUpdatedInventory(int limit) {
        return inventoryAccess.findLastUpdated(limit);
    }

    public int createInventory(int productId, int quantityAvailable, int quantityReserved) {
        InventoryModel inventory = InventoryModel.builder()
                .productId(productId)
                .quantityAvailable(quantityAvailable)
                .quantityReserved(quantityReserved)
                .build();
        return inventoryAccess.insertInventory(inventory);
    }

    public void updateInventoryByProductId(int productId, int quantityAvailable, int quantityReserved) {
        getInventoryByProductId(productId);
        inventoryAccess.updateInventoryByProductId(productId, quantityAvailable, quantityReserved);
    }

    public void deleteInventoryByProductId(int productId) {
        inventoryAccess.deleteByProductId(productId);
    }

    public void reserveStock(int productId, int quantity) {
        InventoryModel inventory = getInventoryByProductId(productId);
        if (quantity <= 0 || inventory.getQuantityAvailable() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient available stock");
        }
        inventoryAccess.updateQuantitiesByProductId(
                productId,
                inventory.getQuantityAvailable() - quantity,
                inventory.getQuantityReserved() + quantity);
    }

    public void consumeStock(int productId, int quantity) {
        InventoryModel inventory = getInventoryByProductId(productId);
        if (quantity <= 0 || inventory.getQuantityReserved() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient reserved stock");
        }
        inventoryAccess.updateQuantitiesByProductId(
                productId,
                inventory.getQuantityAvailable(),
                inventory.getQuantityReserved() - quantity);
    }

    public void releaseStock(int productId, int quantity) {
        InventoryModel inventory = getInventoryByProductId(productId);
        if (quantity <= 0 || inventory.getQuantityReserved() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient reserved stock");
        }
        inventoryAccess.updateQuantitiesByProductId(
                productId,
                inventory.getQuantityAvailable() + quantity,
                inventory.getQuantityReserved() - quantity);
    }

    public void restock(int productId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }
        InventoryModel inventory = getInventoryByProductId(productId);
        inventoryAccess.updateQuantitiesByProductId(
                productId,
                inventory.getQuantityAvailable() + quantity,
                inventory.getQuantityReserved());
    }
}