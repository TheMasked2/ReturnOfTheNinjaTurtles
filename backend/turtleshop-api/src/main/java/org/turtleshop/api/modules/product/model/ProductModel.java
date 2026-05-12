package org.turtleshop.api.modules.product.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Document(collection = "products")
public class ProductModel {

    @Id
    private int product_id;

    @Field("mongo_product_id")
    private String mongo_product_id;

    // SQL fields
    private BigDecimal base_price;
    private String     product_name;

    // MongoDB fields
    private String    description;
    private String    specs;
    private LocalDate available_since;
    private List<String> suggested_products;

    // Constructors
    public ProductModel() {}

    public ProductModel(int product_id, BigDecimal base_price, String product_name,
                        String description, String specs, LocalDate available_since, List<String> suggested_products) {
        this.product_id      = product_id;
        this.base_price      = base_price;
        this.product_name    = product_name;
        this.description     = description;
        this.specs           = specs;
        this.available_since = available_since;
        this.suggested_products = suggested_products;
    }
}