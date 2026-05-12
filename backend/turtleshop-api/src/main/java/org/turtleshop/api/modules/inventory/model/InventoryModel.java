package org.turtleshop.api.modules.inventory.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InventoryModel {
    private int inventoryId;
    private int productId;
    private int quantityAvailable;
    private int quantityReserved;
    private LocalDateTime version;
}