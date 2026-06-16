package org.turtleshop.api.modules.wishlist.service;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.wishlist.model.WishlistItem;
import org.turtleshop.api.modules.wishlist.repository.WishlistItemRepository;
import org.turtleshop.api.modules.wishlist.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistItemService {
    private final WishlistItemRepository repository;
    private final WishlistRepository wishlistRepository;
    // private final ProductRepository productRepository;

    public List<WishlistItem> getAll() {
        List<WishlistItem> result = repository.getAll();
        return result == null ? Collections.emptyList() : result;
    }

    public List<WishlistItem> getAllByWishlistId(Integer wishlistId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }
        List<WishlistItem> result = repository.getAllByWishlistId(wishlistId);
        return result == null ? Collections.emptyList() : result;
    }

    public WishlistItem getByWishlistItemId(Integer wishlistItemId) {
        if (wishlistItemId == null) {
            throw new IllegalArgumentException("wishlistItemId is required");
        }

        return repository.getByWishlistItemId(wishlistItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WishlistItem not found"));
    }

    // Should be a list tbh? 
    public WishlistItem getByProductId(Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }

        return repository.getByProductId(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WishlistItem not found"));
    }

    public void createWishlistItem(Integer wishlistId, Integer productId) {
        if (productId == null || wishlistId == null) {
            throw new IllegalArgumentException("productId and/or wishlistId is required");
        }


        // should probably look if wishlist and product even exist by id before creating
        // Product product = productRepository.getById(productId);
        // if (product.isEmpty()) {
        //     throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        // }

        if (wishlistRepository.getByWishlistId(wishlistId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }

        if (repository.existsByWishlistIdAndProductId(wishlistId, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wishlist item already exists");
        }

        repository.insert(wishlistId, productId);
    }

    public Integer createWishlistItemAndReturnId(Integer wishlistId, Integer productId) {
        if (productId == null || wishlistId == null) {
            throw new IllegalArgumentException("productId and/or wishlistId is required");
        }


        // should probably look if wishlist and product even exist by id before creating
        // Product product = productRepository.getById(productId);
        // if (product.isEmpty()) {
        //     throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        // }

        if (wishlistRepository.getByWishlistId(wishlistId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }

        if (repository.existsByWishlistIdAndProductId(wishlistId, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wishlist item already exists");
        }

        return repository.insertAndReturnId(wishlistId, productId);
    }

    // Should probably be a DTO in future.
    public void updateWishlistItem(WishlistItem wishlistItem) {
        if (wishlistItem == null) {
            throw new IllegalArgumentException("WishlistItem is required");
        }

        repository.update(wishlistItem);
        return;
    }

    public void deleteById(Integer wishlistItemId) {
        if (wishlistItemId == null) {
            throw new IllegalArgumentException("wishlistItemId is required");
        }
    
        if (repository.getByWishlistItemId(wishlistItemId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found");
        }
    
        repository.deleteById(wishlistItemId);
    }

    public void deleteByWishlistId(Integer wishlistId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }
    
        if (wishlistRepository.getByWishlistId(wishlistId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }
    
        repository.deleteByWishlistId(wishlistId);
    }
    
    public void deleteItemFromWishlist(Integer wishlistId, Integer productId) {
        if (wishlistId == null || productId == null) {
            throw new IllegalArgumentException("wishlistId and productId are required");
        }
    
        if (wishlistRepository.getByWishlistId(wishlistId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }
    
        if (!repository.existsByWishlistIdAndProductId(wishlistId, productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found");
        }
    
        repository.deleteItemFromWishlist(wishlistId, productId);
    }
}
