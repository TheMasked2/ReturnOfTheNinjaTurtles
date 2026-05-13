package org.turtleshop.api.modules.shipment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Shipment {
    private int shipmentId;
    private int orderId;
    private String shipmentMethod;
    private String shippingAddress;
}
