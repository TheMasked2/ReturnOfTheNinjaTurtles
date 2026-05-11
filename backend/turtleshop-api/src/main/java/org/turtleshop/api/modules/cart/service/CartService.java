package org.turtleshop.api.modules.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.cart.dto.CartItemResponse;
import org.turtleshop.api.modules.cart.dto.CartResponse;
import org.turtleshop.api.modules.cart.enums.CartStatus;
import org.turtleshop.api.modules.cart.model.Cart;
import org.turtleshop.api.modules.cart.model.CartItem;
import org.turtleshop.api.modules.cart.repository.CartAccess;
import org.turtleshop.api.modules.cart.repository.CartItemAccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartAccess cartAccess;
    private final CartItemAccess cartItemAccess;

    // Creates a Cart based on customerId
    public CartResponse createCart(UUID customerId) {
        Optional<Cart> existingCart = cartAccess.getActiveCartByCustomerId(customerId);
        if (existingCart.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already has an active cart");
        }
        int cartId = cartAccess.insertCart(customerId);
        Cart cart = cartAccess.getCartById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cart was created but could not be found"));
        List<CartItem> cartItems = cartItemAccess.getAllCartItems(cartId);
        return mapToCartResponse(cart, cartItems);
    }

    // Adds item to the cart

    // Delete item from the cart

    // Change quantity of an item in the cart

    // Change status of a Cart to converted.
    public void markCartConverted(int cartId) {
        // change status
        cartAccess.updateCartStatus(cartId, CartStatus.CONVERTED);
    }

    // Delete a cart

    // Get Active Cart

    // HELPER: Maps the Model to the Response DTO
    public CartResponse mapToCartResponse(Cart cart, List<CartItem> cartItems) {
        List<CartItemResponse> items = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .toList();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomerId())
                .orderId(cart.getOrderId())
                .status(cart.getStatus())
                .createdAt(cart.getCreatedAt())
                .items(items)
                .build();
    }

    // HELPER: Maps the Model to the Response DTO
    public CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .cartId(cartItem.getCartId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
