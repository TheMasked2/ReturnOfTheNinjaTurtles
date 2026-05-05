package org.turtleshop.api.modules.wishlist.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WishListItem {
    private Integer wishlistItemId; // PK
    private Integer wishlistId; // FK
    private Integer productId; // FK
    private LocalDateTime addedAt;
}