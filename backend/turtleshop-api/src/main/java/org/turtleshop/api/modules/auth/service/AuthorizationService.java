package org.turtleshop.api.modules.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.order.repository.OrderItemAccess;
import org.turtleshop.api.modules.wishlist.repository.WishlistRepository;
import org.turtleshop.api.modules.wishlist.repository.WishlistItemRepository;

import java.util.UUID;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final CustomerAccess customerAccess;
    private final OrderAccess orderAccess;
    private final OrderItemAccess orderItemAccess;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    public boolean isCurrentCustomer(UUID customerId, Authentication authentication) {
        if (customerId == null || authentication == null || authentication.getName() == null) {
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

    public boolean isWishlistOwner(Integer wishlistId, Authentication authentication) {
        if (wishlistId == null || authentication == null || authentication.getName() == null) {
            return false;
        }

        UUID currentCustomerId = customerAccess.findByEmail(authentication.getName())
                .map(customer -> customer.getCustomerId())
                .orElse(null);

        if (currentCustomerId == null) {
            return false;
        }

        return wishlistRepository.getByWishlistId(wishlistId)
                .map(wishlist -> wishlist.getCustomerId().equals(currentCustomerId))
                .orElse(false);
    }

    public boolean isWishlistItemOwner(Integer wishlistItemId, Authentication authentication) {
        if (wishlistItemId == null || authentication == null || authentication.getName() == null) {
            return false;
        }

        return wishlistItemRepository.getByWishlistItemId(wishlistItemId)
                .map(item -> isWishlistOwner(item.getWishlistId(), authentication))
                .orElse(false);
    }
}