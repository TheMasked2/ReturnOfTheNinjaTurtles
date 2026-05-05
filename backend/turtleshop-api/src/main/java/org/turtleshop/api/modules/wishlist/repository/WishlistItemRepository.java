package org.turtleshop.api.modules.wishlist.repository;

import org.turtleshop.api.modules.wishlist.model.WishlistItem;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository {
    // Get
    List<WishlistItem> getAll();
    List<WishlistItem> getAllByWishlistId(Integer wishlistId);
    Optional<WishlistItem> getByWishlistItemId(Integer wishlistItemId);
    Optional<WishlistItem> getByWishlistId(Integer wishlistId);
    Optional<WishlistItem> getByProductId(Integer productId);
    boolean existsByWishlistIdAndProductId(Integer wishlistId, Integer productId);
    // Insert
    void insert(Integer wishlistId, Integer productId);
    Integer insertAndReturnId(Integer wishlistId, Integer productId); 
    // Update
    // should prolly be an wishlistitemdto huh?
    // void update(WishlistItemDto dto);
    void updateProductId(Integer wishlistItemId, Integer newProductId);
    // Delete
    void deleteById(Integer wishlistItemId);
    void deleteByWishlistId(Integer wishlistId);
    void deleteItemFromWishlist(Integer wishlistId, Integer productId);
}
