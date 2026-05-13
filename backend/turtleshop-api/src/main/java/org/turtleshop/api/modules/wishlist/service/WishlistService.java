package org.turtleshop.api.modules.wishlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.wishlist.model.Wishlist;
import org.turtleshop.api.modules.wishlist.repository.WishlistRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public List<Wishlist> getAllWishlists() {
        List<Wishlist> result = wishlistRepository.getAll();
        return result == null ? Collections.emptyList() : result;
    }

    public Wishlist getWishlistById(Integer wishlistId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }

        return wishlistRepository.getByWishlistId(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found"));
    }

    public Wishlist getWishlistByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        return wishlistRepository.getByCustomerId(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found"));
    }

    public void createWishlist(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        wishlistRepository.insert(customerId);
    }

    public Integer createWishlistAndReturnId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        return wishlistRepository.insertAndReturnId(customerId);
    }

    public void updateWishlistCustomer(Integer wishlistId, UUID newCustomerId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }
        if (newCustomerId == null) {
            throw new IllegalArgumentException("newCustomerId is required");
        }

        wishlistRepository.updateCustomerId(wishlistId, newCustomerId);
    }

    public void deleteWishlistById(Integer wishlistId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }

        wishlistRepository.deleteById(wishlistId);
    }

    public void deleteWishlistByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        wishlistRepository.deleteByCustomerId(customerId);
    }
}