package com.example.product_service.repositories;

import com.example.product_service.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByOwnerId(Long ownerId);
    Optional<Product> findById(Long id);
}
