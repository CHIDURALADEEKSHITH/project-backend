package com.klef.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.project.entity.StockHistory;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Integer>
{
    List<StockHistory> findByProductIdAndChangeTypeOrderByExpiryDateAsc(
            int productId,
            String changeType
    );

    List<StockHistory> findByProductIdAndChangeTypeAndExpiryDate(
            int productId,
            String changeType,
            java.time.LocalDate expiryDate
    );
}