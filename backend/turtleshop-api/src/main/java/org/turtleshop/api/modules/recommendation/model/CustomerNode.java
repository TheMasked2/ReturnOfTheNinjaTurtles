package org.turtleshop.api.modules.recommendation.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Customer")
public class CustomerNode {

    @Id
    private UUID id;

    @Relationship(type = "BOUGHT", direction = Relationship.Direction.OUTGOING)
    private Set<ProductNode> purchasedProducts = new HashSet<>();

    public CustomerNode() {
    }

    public CustomerNode(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Set<ProductNode> getPurchasedProducts() {
        return purchasedProducts;
    }

    public void setPurchasedProducts(Set<ProductNode> purchasedProducts) {
        this.purchasedProducts = purchasedProducts;
    }
}