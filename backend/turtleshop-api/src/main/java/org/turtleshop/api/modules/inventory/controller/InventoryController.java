package org.turtleshop.api.modules.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.inventory.dto.InventoryAdjustmentRequest;
import org.turtleshop.api.modules.inventory.dto.InventoryResponse;
import org.turtleshop.api.modules.inventory.dto.InventoryUpdateRequest;
import org.turtleshop.api.modules.inventory.dto.InventoryCreateRequest;
import org.turtleshop.api.modules.inventory.model.InventoryModel;
import org.turtleshop.api.modules.inventory.service.InventoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponse>> listInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<InventoryModel> inventory = inventoryService.listInventory(page, size);
        return ResponseEntity.ok(toResponseList(inventory));
    }

    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable int inventoryId) {
        return ResponseEntity.ok(toResponse(inventoryService.getInventoryById(inventoryId)));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable int productId) {
        return ResponseEntity.ok(toResponse(inventoryService.getInventoryByProductId(productId)));
    }

    @GetMapping("/quantity/{quantity}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getInventoryByQuantityAvailable(@PathVariable int quantity) {
        List<InventoryModel> inventoryList = inventoryService.getInventoryByQuantityAvailable(quantity);
        return ResponseEntity.ok(toResponseList(inventoryList));
    }

    @GetMapping("/last-updated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getLastUpdatedInventory(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(toResponseList(inventoryService.getLastUpdatedInventory(limit)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getLowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(toResponseList(inventoryService.getLowStock(threshold)));
    }

    @GetMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryResponse>> getBatchByProducts(
            @RequestParam List<Integer> productIds) {
        return ResponseEntity.ok(toResponseList(inventoryService.getByProductIds(productIds)));
    }

    @PostMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createInventory(@PathVariable int productId,
                                                @RequestBody InventoryCreateRequest request) {
        inventoryService.createInventory(productId, request.getQuantityAvailable(), request.getQuantityReserved());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateInventory(@PathVariable int productId,
                                                @RequestBody InventoryUpdateRequest request) {
        inventoryService.updateInventoryByProductId(productId, request.getQuantityAvailable(), request.getQuantityReserved());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventoryByProductId(@PathVariable int productId) {
        inventoryService.deleteInventoryByProductId(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product/{productId}/reserve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reserveStock(@PathVariable int productId,
                                             @RequestBody InventoryAdjustmentRequest request) {
        inventoryService.reserveStock(productId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product/{productId}/consume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> consumeStock(@PathVariable int productId,
                                             @RequestBody InventoryAdjustmentRequest request) {
        inventoryService.consumeStock(productId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product/{productId}/release")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> releaseStock(@PathVariable int productId,
                                             @RequestBody InventoryAdjustmentRequest request) {
        inventoryService.releaseStock(productId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product/{productId}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restockInventory(@PathVariable int productId,
                                                 @RequestBody InventoryAdjustmentRequest request) {
        inventoryService.restock(productId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    private InventoryResponse toResponse(InventoryModel inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getInventoryId())
                .productId(inventory.getProductId())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .version(inventory.getVersion())
                .build();
    }

    private List<InventoryResponse> toResponseList(List<InventoryModel> inventoryList) {
        return inventoryList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}