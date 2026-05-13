package org.turtleshop.api.modules.wishlist.repository;

import org.turtleshop.api.modules.wishlist.model.Wishlist;
// import org.Turtleshop.api.modules.wishlist.model.WishListItem;
// import org.turtleshop.api.modules.auth.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface WishlistRepository {
    // Get
    List<Wishlist> getAll();
    Optional<Wishlist> getByWishlistId(Integer wishlistId);
    Optional<Wishlist> getByCustomerId(UUID customerId);
    // Insert
    void insert(UUID customerId);
    Integer insertAndReturnId(UUID customerId);
    // Update
    void updateCustomerId(Integer wishlistId, UUID newCustomerId);
    // Delete
    void deleteById(Integer wishlistId);
    void deleteByCustomerId(UUID customerId);
}