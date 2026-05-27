package org.turtleshop.api.modules.wishlist.service;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.turtleshop.api.modules.wishlist.model.WishlistItem;
import org.turtleshop.api.modules.wishlist.model.Wishlist;
import org.turtleshop.api.modules.wishlist.repository.WishlistItemRepository;
import org.turtleshop.api.modules.wishlist.repository.WishlistRepository;
// import org.turtleshop.api.modules.product.repository.ProductRepository;



import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional (readOnly = true)
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

    // This is highkey useless, should be get itemS by product id so like a list of all items that have that product id?
    public WishlistItem getByProductId(Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }

        return repository.getByProductId(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "WishlistItem not found"));
    }

    @Transactional(rollbackFor = Exception.class)
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

        // if (repository.existsByWishlistIdAndProductId(wishlistId, productId)) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT, "Wishlist item already exists");
        // }

        try {
            repository.insert(wishlistId, productId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wishlist item already exists", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
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

        // if (repository.existsByWishlistIdAndProductId(wishlistId, productId)) {
        //     throw new ResponseStatusException(HttpStatus.CONFLICT, "Wishlist item already exists");
        // }

        try {
            return repository.insertAndReturnId(wishlistId, productId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to insert wishlist item due to database constraint ", ex);
        }
    }

    // Should probably be a DTO in future.
    @Transactional(rollbackFor = Exception.class)
    public void updateWishlistItem(WishlistItem wishlistItem) {
        if (wishlistItem == null) {
            throw new IllegalArgumentException("WishlistItem is required");
        }

        try {
            repository.update(wishlistItem);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to update wishlist item due to database constraint", ex);
        }
        return;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer wishlistItemId) {
        if (wishlistItemId == null) {
            throw new IllegalArgumentException("wishlistItemId is required");
        }
    
        if (repository.getByWishlistItemId(wishlistItemId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist item not found");
        }
    
        try {
            repository.deleteById(wishlistItemId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete wishlist item due to database constraint", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByWishlistId(Integer wishlistId) {
        if (wishlistId == null) {
            throw new IllegalArgumentException("wishlistId is required");
        }
    
        if (wishlistRepository.getByWishlistId(wishlistId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wishlist not found");
        }
    
        try {
            repository.deleteByWishlistId(wishlistId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete wishlist items due to database constraint", ex);
        }
    }
    
    @Transactional(rollbackFor = Exception.class)
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
    
        try {
            repository.deleteItemFromWishlist(wishlistId, productId);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete wishlist item due to database constraint", ex);
        }
    }
}
