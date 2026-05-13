package org.turtleshop.api.modules.wishlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.wishlist.model.Wishlist;
import org.turtleshop.api.modules.wishlist.service.WishlistService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // Get wishlist by id
    @GetMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<Wishlist> getWishlist(@PathVariable Integer id) {
        return ResponseEntity.ok(wishlistService.getWishlistById(id));
    }

    // Get wishlist by customer id
    @GetMapping("/customer/{customerId}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<Wishlist> getWishlistByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(wishlistService.getWishlistByCustomerId(customerId));
    }

    // Get all wishlists
    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Wishlist>> getAll() {
        return ResponseEntity.ok(wishlistService.getAllWishlists());
    }

    // Create a wishlist for a customer
    @PostMapping("/customer/{customerId}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<Integer> createWishlist(@PathVariable UUID customerId) {
        Integer newWishlistId = wishlistService.createWishlistAndReturnId(customerId);
        return ResponseEntity.ok(newWishlistId);
    }

    // Delete a wishlist by Id
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteWishlist(@PathVariable Integer id) {
        wishlistService.deleteWishlistById(id);
        return ResponseEntity.noContent().build();
    }
}