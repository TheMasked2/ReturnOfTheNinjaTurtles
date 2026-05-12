package org.turtleshop.api.modules.shipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ShipmentResponse {
    private int shipmentId;
    private int orderId;
    private String shipmentMethod;
    private String shippingAddress;
    private List<ShipmentStatusLogResponse> statusLogs;
}