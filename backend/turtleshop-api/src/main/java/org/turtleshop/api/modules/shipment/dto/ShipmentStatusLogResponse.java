package org.turtleshop.api.modules.shipment.dto;

import lombok.Builder;
import lombok.Getter;
import org.turtleshop.api.modules.shipment.enums.ShipmentStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShipmentStatusLogResponse {
    private int logId;
    private int shipmentId;
    private ShipmentStatus status;
    private LocalDateTime statusChangeDate;
    private String notes;
}