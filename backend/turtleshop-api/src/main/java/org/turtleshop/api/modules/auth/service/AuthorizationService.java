package org.turtleshop.api.modules.auth.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.auth.repository.CustomerAccess;
import org.turtleshop.api.modules.order.repository.OrderAccess;
import org.turtleshop.api.modules.order.repository.OrderItemAccess;
import org.turtleshop.api.modules.wishlist.repository.WishlistItemRepository;
import org.turtleshop.api.modules.wishlist.repository.WishlistRepository;
import org.turtleshop.api.modules.cart.repository.CartAccess;
import org.turtleshop.api.modules.cart.repository.CartItemAccess;

import lombok.RequiredArgsConstructor;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final CustomerAccess customerAccess;
    private final OrderAccess orderAccess;
    private final OrderItemAccess orderItemAccess;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CartAccess cartAccess;
    private final CartItemAccess cartItemAccess;

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

        public boolean isWishlistOwner(int wishlistId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
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

    public boolean isWishlistItemOwner(int wishlistItemId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        return wishlistItemRepository.getByWishlistItemId(wishlistItemId)
                .map(wishlistItem -> isWishlistOwner(wishlistItem.getWishlistId(), authentication))
                .orElse(false);
    }

     public boolean isCartOwner(int cartId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        UUID currentCustomerId = customerAccess.findByEmail(authentication.getName())
                .map(customer -> customer.getCustomerId())
                .orElse(null);

        if (currentCustomerId == null) {
            return false;
        }

        return cartAccess.getCartById(cartId)
                .map(cart -> cart.getCustomerId().equals(currentCustomerId))
                .orElse(false);
    }

    public boolean isCartItemOwner(int cartItemId, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        return cartItemAccess.getCartItemById(cartItemId)
                .map(cartItem -> isCartOwner(cartItem.getCartId(), authentication))
                .orElse(false);
    }
}