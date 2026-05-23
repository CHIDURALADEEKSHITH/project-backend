package com.klef.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.project.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer>
{
    public List<Cart> findByUserId(int userId);
}