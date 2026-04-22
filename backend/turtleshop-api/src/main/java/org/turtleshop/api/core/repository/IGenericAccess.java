package org.turtleshop.api.core.repository;

import java.util.List;
import java.util.Optional;

public interface IGenericAccess<T, K> {
    Optional<T> getById(K id);
    List<T> getAll();
    void delete(K id);
    void insert(T item);
    void update(T item);
    boolean testConnection();
}