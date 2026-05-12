package org.turtleshop.api.modules.inventory.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InventoryCreateRequest {

    private Integer quantityAvailable;
    private Integer quantityReserved;

    public InventoryCreateRequest() {
    }

    @JsonCreator
    public InventoryCreateRequest(
            @JsonProperty("quantityAvailable") Integer quantityAvailable,
            @JsonProperty("quantityReserved") Integer quantityReserved) {
        this.quantityAvailable = quantityAvailable;
        this.quantityReserved = quantityReserved;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Integer getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(Integer quantityReserved) {
        this.quantityReserved = quantityReserved;
    }
}