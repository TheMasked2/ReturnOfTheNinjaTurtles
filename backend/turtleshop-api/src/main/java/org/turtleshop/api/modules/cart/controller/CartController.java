package org.turtleshop.api.modules.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.cart.dto.AddCartItemRequest;
import org.turtleshop.api.modules.cart.dto.CartItemResponse;
import org.turtleshop.api.modules.cart.dto.CartResponse;
import org.turtleshop.api.modules.cart.dto.UpdateCartItemQuantityRequest;
import org.turtleshop.api.modules.cart.service.CartService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
public class CartController {

    final CartService cartService;

    @PostMapping("/{customerId}")
    @PreAuthorize("hasAuthority('CART_CREATE_ALL') or " +
            "(hasAuthority('CART_CREATE_OWN') and @authorizationService.isCurrentCustomer(#customerId, authentication))")
    public ResponseEntity<CartResponse> createCart(@PathVariable UUID customerId) {
        return ResponseEntity.ok(cartService.createCart(customerId));
    }

    @PostMapping("/{customerId}/items")
    @PreAuthorize("hasAuthority('CART_UPDATE_ALL') or " +
            "(hasAuthority('CART_UPDATE_OWN') and @authorizationService.isCurrentCustomer(#customerId, authentication))")
    public ResponseEntity<CartItemResponse> addItemToCart(
            @PathVariable UUID customerId,
            @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(customerId, request));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('CART_READ_ALL') or " +
            "(hasAuthority('CART_READ_OWN') and @authorizationService.isCurrentCustomer(#customerId, authentication))")
    public ResponseEntity<CartResponse> getActiveCart(@PathVariable UUID customerId) {
        return ResponseEntity.ok(cartService.getActiveCartForUser(customerId));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('CART_READ_ALL')")
    public ResponseEntity<List<CartResponse>> getAllActiveCarts() {
        return ResponseEntity.ok(cartService.getAllExistingActiveCarts());
    }

        @PatchMapping("/items/{cartItemId}")
    @PreAuthorize("hasAuthority('CART_UPDATE_ALL') or " +
            "(hasAuthority('CART_UPDATE_OWN') and @authorizationService.isCartItemOwner(#cartItemId, authentication))")
    public ResponseEntity<String> updateCartItemQuantity(
            @PathVariable int cartItemId,
            @RequestBody UpdateCartItemQuantityRequest request
    ) {
        cartService.changeQuantityOfCartItem(cartItemId, request.getQuantity());
        return ResponseEntity.ok("Successfully updated the quantity of item in cart");
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasAuthority('CART_DELETE_ALL') or " +
            "(hasAuthority('CART_DELETE_OWN') and @authorizationService.isCartItemOwner(#cartItemId, authentication))")
    public ResponseEntity<String> removeItemFromCart(@PathVariable int cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.ok("Item is removed from the cart");
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasAuthority('CART_DELETE_ALL')")
    public ResponseEntity<String> deleteCart(@PathVariable int cartId) {
        cartService.removeCart(cartId);
        return ResponseEntity.ok("Cart is deleted");
    }
}
