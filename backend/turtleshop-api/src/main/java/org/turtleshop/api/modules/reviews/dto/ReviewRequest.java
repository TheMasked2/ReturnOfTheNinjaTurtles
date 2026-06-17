package org.turtleshop.api.modules.reviews.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 10, message = "rating must be at most 10")
    private Integer rating;

    @NotBlank(message = "comment is required")
    @Size(max = 2000, message = "comment cannot exceed 2000 characters")
    private String comment;
}