package org.turtleshop.api.modules.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.turtleshop.api.modules.product.dto.CreateProductRequest;
import org.turtleshop.api.modules.product.dto.ProductResponse;
import org.turtleshop.api.modules.product.dto.UpdateProductRequest;
import org.turtleshop.api.modules.product.model.ProductModel;
import org.turtleshop.api.modules.product.service.ProductService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(new ProductResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        ProductModel product = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + product.getProduct_id()))
                .body(new ProductResponse(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id, @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request)
                .map(product -> ResponseEntity.ok(new ProductResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}