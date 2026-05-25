package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.order.repository.OrderItemAccess;

import java.util.UUID;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final CustomerAccess customerAccess;
    private final OrderAccess orderAccess;
    private final OrderItemAccess orderItemAccess;

    public boolean isCurrentCustomer(UUID customerId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        String email = authentication.getName();

        return customerAccess.findByEmail(email)
                .map(customer -> customer.getCustomerId().equals(customerId))
                .orElse(false);
    }

    public boolean isOrderOwner(int orderId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        UUID currentCustomerId = customerAccess.findByEmail(authentication.getName())
                .map(customer -> customer.getCustomerId())
                .orElse(null);

        if (currentCustomerId == null) {
            return false;
        }

        return orderAccess.getOrderById(orderId)
                .map(order -> order.getCustomerId().equals(currentCustomerId))
                .orElse(false);
    }

    public boolean isOrderItemOwner(int orderItemId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        return orderItemAccess.getOrderItemById(orderItemId)
                .map(orderItem -> isOrderOwner(orderItem.getOrderId(), authentication))
                .orElse(false);
    }
}