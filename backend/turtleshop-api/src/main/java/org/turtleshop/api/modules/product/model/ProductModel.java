package org.turtleshop.api.modules.product.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "products")
public class ProductModel {

    @Field("product_id")
    private int productId;

    @Id
    @Field("mongo_product_id")
    private String mongoProductId;

    @Field("base_price")
    private BigDecimal basePrice;

    @Field("product_name")
    private String productName;

    @Field("description")
    private String description;

    @Field("specs")
    private String specs;

    @Field("available_since")
    private LocalDate availableSince;

    @Field("suggested_products")
    private List<String> suggestedProducts;
}