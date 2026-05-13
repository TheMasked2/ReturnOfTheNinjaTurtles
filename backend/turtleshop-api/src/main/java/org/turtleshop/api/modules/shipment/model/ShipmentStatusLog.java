package org.turtleshop.api.modules.shipment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ShipmentStatusLog {
    private int logId;
    private int shipmentId;
    private ShipmentStatus status;
    private LocalDateTime statusChangeDate;
    private String notes;
}
