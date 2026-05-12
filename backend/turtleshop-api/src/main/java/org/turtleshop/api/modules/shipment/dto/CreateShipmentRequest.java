package org.turtleshop.api.modules.shipment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateShipmentRequest {
    private int orderId;
    private String shipmentMethod;
    private String shippingAddress;
}