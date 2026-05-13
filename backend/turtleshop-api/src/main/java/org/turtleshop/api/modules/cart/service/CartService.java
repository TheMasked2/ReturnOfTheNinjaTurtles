package org.turtleshop.api.modules.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.auth.model.Customer;
import org.turtleshop.api.modules.cart.dto.AddCartItemRequest;
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
        if(existingCart.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already has an active cart");
        }
        int cartId = cartAccess.insertCart(customerId);
        Cart cart = cartAccess.getCartById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cart was created but could not be found"));
        List<CartItem> cartItems = cartItemAccess.getAllCartItems(cartId);
        return mapToCartResponse(cart, cartItems);
    }

    // Adds item to the cart
    public CartItemResponse addItemToCart(UUID customerId, AddCartItemRequest request) {
        Cart activeCart = cartAccess.getActiveCartByCustomerId(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No cart found for this customer"));
        int cartItemId = cartItemAccess.insertCartItem(activeCart.getCartId(), request.getProductId(), request.getQuantity());
        CartItem cartItem = cartItemAccess.getCartItemById(cartItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No cartitem found for this cartitemid given"));
        // Item quantity per product add it to the quantity reserved.
        return mapToCartItemResponse(cartItem);
    }

    // Delete item from the cart
    public void removeItemFromCart(int cartItemId) {
        CartItem existingCartItem = cartItemAccess.getCartItemById(cartItemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT));
        cartItemAccess.deleteCartItem(existingCartItem.getCartItemId());
    }

    // Change quantity of an item in the cart
    public void changeQuantityOfCartItem(int cartItemId, int quantity) {
        CartItem existingCartItem = cartItemAccess.getCartItemById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item does not exist"
                ));

        cartItemAccess.updateCartItemQuantity(cartItemId, quantity);
    }

    // Change status of a Cart to converted.
    public void markCartConverted(int cartId) {
        Cart existingCart = cartAccess.getCartById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Cart does not exist"));
        cartAccess.updateCartStatus(cartId, CartStatus.CONVERTED);
    }

    // Delete a cart
    public void removeCart(int cartId) {
        Cart existingCart = cartAccess.getCartById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Cart does not exist"));
        cartAccess.deleteCart(cartId);
    }

    // Get Active Cart
    public CartResponse getActiveCartForUser (UUID customerId) {
        Cart activeCart = cartAccess.getActiveCartByCustomerId(customerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "There are no active carts found for this customer"));
        List<CartItem> allCartItems = cartItemAccess.getAllCartItems(activeCart.getCartId());
        return mapToCartResponse(activeCart, allCartItems);
    }

    // Get all Active Carts that exist.
    public List<CartResponse> getAllExistingActiveCarts () {
        List<Cart> activeCarts = cartAccess.getAllActiveCarts();
        return activeCarts.stream()
                .map(cart -> {
                    List<CartItem> cartItems = cartItemAccess.getAllCartItems(cart.getCartId());
                    CartResponse response = mapToCartResponse(cart, cartItems);
                    return response;
                }).toList();
    }

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
