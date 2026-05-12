package org.turtleshop.api.modules.product.service;

import org.springframework.stereotype.Service;
import org.turtleshop.api.modules.product.dto.CreateProductRequest;
import org.turtleshop.api.modules.product.dto.UpdateProductRequest;
import org.turtleshop.api.modules.product.model.ProductModel;
import org.turtleshop.api.modules.product.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductModel> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<ProductModel> getProductById(String id) {
        return productRepository.findById(id);
    }

    public ProductModel createProduct(CreateProductRequest request) {
        ProductModel product = new ProductModel();
        product.setProduct_name(request.getName());
        product.setDescription(request.getDescription());
        product.setBase_price(request.getPrice());
        return productRepository.save(product);
    }

    public Optional<ProductModel> updateProduct(String id, UpdateProductRequest request) {
        return productRepository.findById(id).map(product -> {
            product.setProduct_name(request.getName());
            product.setDescription(request.getDescription());
            product.setBase_price(request.getPrice());
            return productRepository.save(product);
        });
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
