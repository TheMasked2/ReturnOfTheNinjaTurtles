package org.turtleshop.api.modules.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.inventory.dto.InventoryResponse;
import org.turtleshop.api.modules.inventory.dto.InventoryUpdateRequest;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.service.InventoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Get inventory by product ID
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable int productId) {
        InventoryModel inventory = inventoryService.getInventoryByProductId(productId);
        InventoryResponse response = InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProductId())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .version(inventory.getVersion())
                .build();
        return ResponseEntity.ok(response);
    }

    // Get inventory by quantity available
    @GetMapping("/quantity/{quantity}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> getInventoryByQuantityAvailable(@PathVariable int quantity) {
        InventoryModel inventory = inventoryService.getInventoryByProductQuantityAvailable(quantity);
        InventoryResponse response = InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProductId())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .version(inventory.getVersion())
                .build();
        return ResponseEntity.ok(response);
    }

    // Get last updated inventory records
    @GetMapping("/last-updated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getLastUpdatedInventory(@RequestParam(defaultValue = "10") int limit) {
        List<InventoryModel> inventoryList = inventoryService.getLastUpdatedInventory(limit);
        List<InventoryResponse> responseList = inventoryList.stream()
                .map(inventory -> InventoryResponse.builder()
                        .inventoryId(inventory.getInventoryId())
                        .productId(inventory.getProductId())
                        .quantityAvailable(inventory.getQuantityAvailable())
                        .quantityReserved(inventory.getQuantityReserved())
                        .version(inventory.getVersion())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    // remove inventory by Product ID
    @DeleteMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventoryByProductId(@PathVariable int productId) {
        inventoryService.deleteInventoryByProductId(productId);
        return ResponseEntity.noContent().build();
    }

    // Update inventory quantities
    @PutMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateInventory(@PathVariable int productId,
                                                 @RequestBody InventoryUpdateRequest request) {
        inventoryService.updateInventoryByProductId(productId, request.getQuantityAvailable(), request.getQuantityReserved());
        return ResponseEntity.noContent().build();
    }

    // Create new inventory record
    @PostMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createInventory(@PathVariable int productId,
                                                 @RequestBody InventoryUpdateRequest request) {
        inventoryService.createInventory(productId, request.getQuantityAvailable(), request.getQuantityReserved());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}