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
    public ResponseEntity<CartResponse> createCart(@PathVariable UUID customerId) {
        return ResponseEntity.ok(cartService.createCart(customerId));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartItemResponse> addItemToCart(@PathVariable UUID customerId, @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(customerId, request));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponse> getActiveCart(@PathVariable UUID customerId) {
        return ResponseEntity.ok(cartService.getActiveCartForUser(customerId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CartResponse>> getAllActiveCarts() {
        return ResponseEntity.ok(cartService.getAllExistingActiveCarts());
    }

    @PatchMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateCartItemQuantity(@PathVariable int cartItemId, @RequestBody UpdateCartItemQuantityRequest request) {
        cartService.changeQuantityOfCartItem(cartItemId, request.getQuantity());
        return ResponseEntity.ok("Succesfully updated the quantity of item in cart");
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeItemFromCart(@PathVariable int cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.ok("Item is removed from the cart");
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCart(@PathVariable int cartId) {
        cartService.removeCart(cartId);
        return ResponseEntity.ok("Cart is deleted");
    }
}
