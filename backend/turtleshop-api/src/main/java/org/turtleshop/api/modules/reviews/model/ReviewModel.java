package org.turtleshop.api.modules.reviews.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reviews")
public class ReviewModel {
    @Id
    private String id;

    private Integer reviewId;
    private Integer productId;
    private String customerId;
    private Integer rating;
    private String comment;
    private String createdAt;
}