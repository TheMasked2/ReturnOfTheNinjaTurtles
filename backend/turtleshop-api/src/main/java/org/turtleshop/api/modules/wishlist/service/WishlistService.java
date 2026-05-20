package org.turtleshop.api.modules.wishlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.wishlist.model.Wishlist;
import org.turtleshop.api.modules.wishlist.repository.WishlistRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

    @Transactional(rollbackFor = Exception.class)
    public void createWishlist(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        try {
            wishlistRepository.insert(customerId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to create wishlist due to database constraint", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer createWishlistAndReturnId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        try {
            return wishlistRepository.insertAndReturnId(customerId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to create wishlist due to database constraint", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWishlistCustomer(Integer wishlistId, UUID newCustomerId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }
        if (newCustomerId == null) {
            throw new IllegalArgumentException("newCustomerId is required");
        }

        try {
            wishlistRepository.updateCustomerId(wishlistId, newCustomerId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to update wishlist due to database constraint", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWishlistById(Integer wishlistId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }

        try {
            wishlistRepository.deleteById(wishlistId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete wishlist due to database constraint", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWishlistByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        try {
            wishlistRepository.deleteByCustomerId(customerId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete wishlist due to database constraint", ex);
        }
    }
}