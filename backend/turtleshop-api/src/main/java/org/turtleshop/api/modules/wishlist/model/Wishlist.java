package org.turtleshop.api.modules.wishlist.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class Wishlist {
    private Integer wishlistId; // PK
    private UUID customerId; // FK
}
