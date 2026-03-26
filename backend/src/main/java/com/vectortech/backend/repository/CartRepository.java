package com.vectortech.backend.repository;

import com.vectortech.backend.model.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.id = :id")
    Optional<Cart> findGraphById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
}
