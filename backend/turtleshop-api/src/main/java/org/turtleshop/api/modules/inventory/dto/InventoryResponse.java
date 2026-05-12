package org.turtleshop.api.modules.inventory.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryResponse {
    private int inventoryId;
    private int productId;
    private int quantityAvailable;
    private int quantityReserved;
    private LocalDateTime version;
}