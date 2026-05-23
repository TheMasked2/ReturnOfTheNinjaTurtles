package org.turtleshop.api.modules.reviews.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "database_sequences")
public class DatabaseSequence {
    @Id
    private String id;
    private Integer seq;
}