package org.turtleshop.api.modules.product.controller;

import java.net.URI;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.turtleshop.api.modules.product.dto.CreateProductRequest;
import org.turtleshop.api.modules.product.dto.ProductPageResponse;
import org.turtleshop.api.modules.product.dto.ProductResponse;
import org.turtleshop.api.modules.product.dto.UpdateProductRequest;
import org.turtleshop.api.modules.product.model.ProductModel;
import org.turtleshop.api.modules.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ProductPageResponse getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer categoryId) {
        return productService.getAllProducts(page, size, search, sortBy, minPrice, maxPrice, categoryId);
    }

    @GetMapping(params = "ids")
    @PreAuthorize("permitAll()")
    public List<ProductResponse> getProductsByIds(@RequestParam("ids") List<Integer> ids) {
        return productService.getProductsByIds(ids).stream()
                .map(ProductResponse::new)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(new ProductResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE_ALL')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        ProductModel product = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + product.getProductId()))
                .body(new ProductResponse(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE_ALL')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable int id, @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request)
                .map(product -> ResponseEntity.ok(new ProductResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE_ALL')")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}