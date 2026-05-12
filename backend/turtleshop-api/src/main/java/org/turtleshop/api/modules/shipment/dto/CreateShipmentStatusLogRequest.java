package org.turtleshop.api.modules.shipment.dto;

import lombok.Getter;
import lombok.Setter;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;

@Getter
@Setter
public class CreateShipmentStatusLogRequest {
    private ShipmentStatus status;
    private String notes;
}