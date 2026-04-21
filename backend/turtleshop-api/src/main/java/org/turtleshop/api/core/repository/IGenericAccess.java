package org.turtleshop.api.core.repository;

import java.util.List;
import java.util.Optional;

public interface IGenericAccess<T, K> {
    Optional<T> getByIdAsync(K id);
    List<T> getAllAsync();
    void deleteAsync(K id);
    void insertAsync(T item);
    void updateAsync(T item);
    boolean testConnection();
}