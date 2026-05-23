
package com.klef.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.project.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>
{
    public List<Product> findByNameContainingIgnoreCase(String keyword);

    public List<Product> findByCategoryId(int categoryId);
}