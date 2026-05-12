package org.turtleshop.api.modules.inventory.dto;

public class InventoryUpdateRequest {
    private int quantityAvailable;
    private int quantityReserved;

    // Getters and Setters
    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(int quantityReserved) {
        this.quantityReserved = quantityReserved;
    }
}
