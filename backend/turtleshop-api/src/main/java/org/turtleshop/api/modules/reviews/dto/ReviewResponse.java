package org.turtleshop.api.modules.reviews.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ReviewResponse {
    private String id;
    private Integer reviewId;
    private Integer productId;
    private String customerId;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
}