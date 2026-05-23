package com.klef.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.project.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer>
{
    public List<Order> findByUserId(int userId);
}