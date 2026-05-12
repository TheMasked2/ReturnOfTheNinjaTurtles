package org.turtleshop.api.modules.wishlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.turtleshop.api.modules.wishlist.model.WishlistItem;
import org.turtleshop.api.modules.wishlist.service.WishlistItemService;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist-item")
@RequiredArgsConstructor
public class WishlistItemController {

    private final WishlistItemService wishlistItemService;

    @GetMapping
    public ResponseEntity<List<WishlistItem>> getAllWishlistItems() {
        return ResponseEntity.ok(wishlistItemService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WishlistItem> getWishlistItemById(@PathVariable Integer id) {
        return ResponseEntity.ok(wishlistItemService.getByWishlistItemId(id));
    }

    @GetMapping("/wishlist/{wishlistId}")
    public ResponseEntity<List<WishlistItem>> getWishlistItemsByWishlistId(@PathVariable Integer wishlistId) {
        return ResponseEntity.ok(wishlistItemService.getAllByWishlistId(wishlistId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<WishlistItem> getWishlistItemByProductId(@PathVariable Integer productId) {
        return ResponseEntity.ok(wishlistItemService.getByProductId(productId));
    }

    @PostMapping("/wishlist/{wishlistId}/product/{productId}")
    public ResponseEntity<Void> createWishlistItem(
            @PathVariable Integer wishlistId,
            @PathVariable Integer productId) {

        wishlistItemService.createWishlistItem(wishlistId, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wishlist/{wishlistId}/product/{productId}/return-id")
    public ResponseEntity<Integer> createWishlistItemAndReturnId(
            @PathVariable Integer wishlistId,
            @PathVariable Integer productId) {

        Integer newId = wishlistItemService.createWishlistItemAndReturnId(wishlistId, productId);
        return ResponseEntity.ok(newId);
    }

    @PutMapping
    public ResponseEntity<Void> updateWishlistItem(@RequestBody WishlistItem wishlistItem) {
        wishlistItemService.updateWishlistItem(wishlistItem);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishlistItem(@PathVariable Integer id) {
        wishlistItemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wishlist/{wishlistId}")
    public ResponseEntity<Void> deleteByWishlistId(@PathVariable Integer wishlistId) {
        wishlistItemService.deleteByWishlistId(wishlistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wishlist/{wishlistId}/product/{productId}")
    public ResponseEntity<Void> deleteItemFromWishlist(
            @PathVariable Integer wishlistId,
            @PathVariable Integer productId) {

        wishlistItemService.deleteItemFromWishlist(wishlistId, productId);
        return ResponseEntity.noContent().build();
    }
}